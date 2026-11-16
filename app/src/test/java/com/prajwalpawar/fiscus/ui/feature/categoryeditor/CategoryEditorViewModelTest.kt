@file:OptIn(ExperimentalCoroutinesApi::class) // advanceUntilIdle

package com.prajwalpawar.fiscus.ui.feature.categoryeditor

import com.prajwalpawar.fiscus.R
import com.prajwalpawar.fiscus.model.CategoryKind
import com.prajwalpawar.fiscus.model.Money
import com.prajwalpawar.fiscus.model.Transaction
import com.prajwalpawar.fiscus.model.TransactionType
import com.prajwalpawar.fiscus.testing.CashAccount
import com.prajwalpawar.fiscus.testing.FakeCategoryRepository
import com.prajwalpawar.fiscus.testing.FakeTransactionRepository
import com.prajwalpawar.fiscus.testing.FoodCategory
import com.prajwalpawar.fiscus.testing.MainDispatcherRule
import com.prajwalpawar.fiscus.ui.navigation.CategoryEditorNavKey
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import kotlin.time.Instant

class CategoryEditorViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val transactions = FakeTransactionRepository()
    private val categories = FakeCategoryRepository().apply {
        this.categories.value = listOf(FoodCategory)
    }

    private fun TestScope.viewModel(key: CategoryEditorNavKey) = CategoryEditorViewModel(
        navKey = key,
        categoryRepository = categories,
        transactionRepository = transactions,
    ).also { viewModel ->
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        advanceUntilIdle()
    }

    private suspend fun useFood() = transactions.save(
        Transaction(
            type = TransactionType.EXPENSE,
            title = "Lunch",
            amount = Money(10_000, "INR"),
            accountId = CashAccount.id,
            categoryId = FoodCategory.id,
            occurredAt = Instant.parse("2026-11-15T03:00:00Z"),
            createdAt = Instant.parse("2026-11-15T03:00:00Z"),
            updatedAt = Instant.parse("2026-11-15T03:00:00Z"),
        ),
    )

    @Test
    fun `adds a trimmed category of the preset kind`() = runTest(mainDispatcherRule.testDispatcher) {
        val viewModel = viewModel(CategoryEditorNavKey(kind = CategoryKind.INCOME))
        assertFalse(viewModel.uiState.value.canSave)
        viewModel.onEvent(CategoryEditorEvent.NameChanged("  Freelance "))
        viewModel.onEvent(CategoryEditorEvent.IconChanged("storefront"))
        viewModel.onEvent(CategoryEditorEvent.Save)
        advanceUntilIdle()

        val saved = categories.categories.value.single { it.name == "Freelance" }
        assertEquals(CategoryKind.INCOME, saved.kind)
        assertEquals("storefront", saved.iconKey)
        assertTrue(viewModel.uiState.value.isFinished)
        assertEquals(R.string.category_result_added, viewModel.uiState.value.resultMessage)
    }

    @Test
    fun `an unused category can change kind and be deleted`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val viewModel = viewModel(CategoryEditorNavKey(categoryId = FoodCategory.id))
            assertFalse(viewModel.uiState.value.isKindLocked)
            assertTrue(viewModel.uiState.value.canDelete)

            viewModel.onEvent(CategoryEditorEvent.DeleteRequested)
            advanceUntilIdle()
            assertTrue(viewModel.uiState.value.showDeleteConfirmation)
            viewModel.onEvent(CategoryEditorEvent.DeleteConfirmed)
            advanceUntilIdle()

            assertTrue(categories.categories.value.isEmpty())
            assertTrue(viewModel.uiState.value.isFinished)
            assertEquals(R.string.category_result_deleted, viewModel.uiState.value.resultMessage)
        }

    @Test
    fun `a used category locks its kind, archives instead of deleting`() =
        runTest(mainDispatcherRule.testDispatcher) {
            useFood()
            val viewModel = viewModel(CategoryEditorNavKey(categoryId = FoodCategory.id))
            assertTrue(viewModel.uiState.value.isKindLocked)
            assertFalse(viewModel.uiState.value.canDelete)

            viewModel.onEvent(CategoryEditorEvent.KindChanged(CategoryKind.INCOME))
            advanceUntilIdle()
            assertEquals(CategoryKind.EXPENSE, viewModel.uiState.value.kind)
            viewModel.onEvent(CategoryEditorEvent.DeleteRequested)
            advanceUntilIdle()
            assertFalse(viewModel.uiState.value.showDeleteConfirmation)

            viewModel.onEvent(CategoryEditorEvent.ArchiveToggled)
            advanceUntilIdle()
            assertTrue(categories.categories.value.single().archived)
            assertTrue(viewModel.uiState.value.archived)
            assertEquals(R.string.category_result_archived, viewModel.uiState.value.userMessage)

            viewModel.onEvent(CategoryEditorEvent.UserMessageShown)
            advanceUntilIdle()
            assertNull(viewModel.uiState.value.userMessage)
        }

    @Test
    fun `editing keeps the archived flag`() = runTest(mainDispatcherRule.testDispatcher) {
        categories.categories.value = listOf(FoodCategory.copy(archived = true, isSeeded = true))
        val viewModel = viewModel(CategoryEditorNavKey(categoryId = FoodCategory.id))
        assertEquals("restaurant", viewModel.uiState.value.iconKey)
        viewModel.onEvent(CategoryEditorEvent.NameChanged("Dining out"))
        viewModel.onEvent(CategoryEditorEvent.Save)
        advanceUntilIdle()
        val saved = categories.categories.value.single()
        assertEquals("Dining out", saved.name)
        assertTrue(saved.archived)
        assertTrue(saved.isSeeded)
    }
}
