@file:OptIn(ExperimentalCoroutinesApi::class) // advanceUntilIdle

package com.prajwalpawar.fiscus.ui.feature.categorydetail

import com.prajwalpawar.fiscus.R
import com.prajwalpawar.fiscus.model.Money
import com.prajwalpawar.fiscus.model.Transaction
import com.prajwalpawar.fiscus.model.TransactionType
import com.prajwalpawar.fiscus.testing.CashAccount
import com.prajwalpawar.fiscus.testing.FakeCategoryRepository
import com.prajwalpawar.fiscus.testing.FakeTransactionRepository
import com.prajwalpawar.fiscus.testing.FoodCategory
import com.prajwalpawar.fiscus.testing.MainDispatcherRule
import com.prajwalpawar.fiscus.ui.navigation.CategoryDetailNavKey
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

class CategoryDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val transactions = FakeTransactionRepository()
    private val categories = FakeCategoryRepository(transactions).apply {
        this.categories.value = listOf(FoodCategory)
    }

    private fun TestScope.viewModel() = CategoryDetailViewModel(
        navKey = CategoryDetailNavKey(FoodCategory.id),
        categoryRepository = categories,
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
    fun `an unused category is deleted after confirming`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val viewModel = viewModel()
            assertTrue(viewModel.uiState.value.canDelete)
            viewModel.onEvent(CategoryDetailEvent.DeleteRequested)
            advanceUntilIdle()
            assertTrue(viewModel.uiState.value.showDeleteConfirmation)

            viewModel.onEvent(CategoryDetailEvent.DeleteConfirmed)
            advanceUntilIdle()
            assertTrue(categories.categories.value.isEmpty())
            assertTrue(viewModel.uiState.value.isFinished)
            assertEquals(R.string.category_result_deleted, viewModel.uiState.value.resultMessage)
        }

    @Test
    fun `a used category can't be deleted but archives and unarchives`() =
        runTest(mainDispatcherRule.testDispatcher) {
            useFood()
            val viewModel = viewModel()
            assertEquals(1, viewModel.uiState.value.category?.transactionCount)
            assertFalse(viewModel.uiState.value.canDelete)
            viewModel.onEvent(CategoryDetailEvent.DeleteRequested)
            advanceUntilIdle()
            assertFalse(viewModel.uiState.value.showDeleteConfirmation)

            viewModel.onEvent(CategoryDetailEvent.ArchiveToggled)
            advanceUntilIdle()
            assertTrue(categories.categories.value.single().archived)
            assertEquals(R.string.category_result_archived, viewModel.uiState.value.userMessage)
            viewModel.onEvent(CategoryDetailEvent.UserMessageShown)
            advanceUntilIdle()
            assertNull(viewModel.uiState.value.userMessage)

            viewModel.onEvent(CategoryDetailEvent.ArchiveToggled)
            advanceUntilIdle()
            assertFalse(categories.categories.value.single().archived)
            assertEquals(R.string.category_result_unarchived, viewModel.uiState.value.userMessage)
        }

    @Test
    fun `a missing category finishes quietly`() = runTest(mainDispatcherRule.testDispatcher) {
        categories.categories.value = emptyList()
        val viewModel = viewModel()
        assertTrue(viewModel.uiState.value.isFinished)
        assertNull(viewModel.uiState.value.resultMessage)
    }
}
