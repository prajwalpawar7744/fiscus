@file:OptIn(ExperimentalCoroutinesApi::class) // advanceUntilIdle

package com.prajwalpawar.fiscus.ui.feature.categories

import com.prajwalpawar.fiscus.model.CategoryKind
import com.prajwalpawar.fiscus.model.Money
import com.prajwalpawar.fiscus.model.Transaction
import com.prajwalpawar.fiscus.model.TransactionType
import com.prajwalpawar.fiscus.testing.CashAccount
import com.prajwalpawar.fiscus.testing.FakeTransactionRepository
import com.prajwalpawar.fiscus.testing.FakeCategoryRepository
import com.prajwalpawar.fiscus.testing.FoodCategory
import com.prajwalpawar.fiscus.testing.MainDispatcherRule
import com.prajwalpawar.fiscus.testing.SalaryCategory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import kotlin.time.Instant

class CategoriesViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val transactions = FakeTransactionRepository()
    private val categories = FakeCategoryRepository(transactions).apply {
        this.categories.value = listOf(
            FoodCategory,
            FoodCategory.copy(id = 3, name = "Bills", iconKey = "receipt_long"),
            FoodCategory.copy(id = 4, name = "Gym", iconKey = "fitness_center", archived = true),
            SalaryCategory,
        )
    }

    private fun TestScope.viewModel() = CategoriesViewModel(categories).also { viewModel ->
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        advanceUntilIdle()
    }

    @Test
    fun `shows expense categories by name, active then archived`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val state = viewModel().uiState.value
            assertEquals(CategoryKind.EXPENSE, state.kind)
            assertEquals(listOf("Bills", "Food"), state.active.map { it.category.name })
            assertEquals(listOf("Gym"), state.archived.map { it.category.name })
        }

    @Test
    fun `counts each category's transactions`() = runTest(mainDispatcherRule.testDispatcher) {
        val at = Instant.parse("2026-11-15T03:00:00Z")
        repeat(2) {
            transactions.save(
                Transaction(
                    type = TransactionType.EXPENSE,
                    title = "Lunch",
                    amount = Money(10_000, "INR"),
                    accountId = CashAccount.id,
                    categoryId = FoodCategory.id,
                    occurredAt = at,
                    createdAt = at,
                    updatedAt = at,
                ),
            )
        }
        val counts = viewModel().uiState.value.active.associate { it.category.name to it.transactionCount }
        assertEquals(mapOf("Bills" to 0, "Food" to 2), counts)
    }

    @Test
    fun `selecting income shows income categories`() = runTest(mainDispatcherRule.testDispatcher) {
        val viewModel = viewModel()
        viewModel.onEvent(CategoriesEvent.KindSelected(CategoryKind.INCOME))
        advanceUntilIdle()
        assertEquals(listOf("Salary"), viewModel.uiState.value.active.map { it.category.name })
        assertEquals(emptyList<Any>(), viewModel.uiState.value.archived)
    }
}
