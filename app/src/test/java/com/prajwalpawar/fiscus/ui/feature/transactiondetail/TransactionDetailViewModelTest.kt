@file:OptIn(ExperimentalCoroutinesApi::class) // advanceUntilIdle

package com.prajwalpawar.fiscus.ui.feature.transactiondetail

import com.prajwalpawar.fiscus.R
import com.prajwalpawar.fiscus.model.Money
import com.prajwalpawar.fiscus.model.Transaction
import com.prajwalpawar.fiscus.model.TransactionType
import com.prajwalpawar.fiscus.testing.BankAccount
import com.prajwalpawar.fiscus.testing.CashAccount
import com.prajwalpawar.fiscus.testing.FakeAccountRepository
import com.prajwalpawar.fiscus.testing.FakeCategoryRepository
import com.prajwalpawar.fiscus.testing.FakeTransactionRepository
import com.prajwalpawar.fiscus.testing.FixedClock
import com.prajwalpawar.fiscus.testing.FoodCategory
import com.prajwalpawar.fiscus.testing.MainDispatcherRule
import com.prajwalpawar.fiscus.testing.TestNow
import com.prajwalpawar.fiscus.ui.navigation.TransactionDetailNavKey
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class TransactionDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val transactions = FakeTransactionRepository()
    private val accounts = FakeAccountRepository(transactions).apply {
        this.accounts.value = listOf(CashAccount, BankAccount)
    }
    private val categories = FakeCategoryRepository().apply {
        this.categories.value = listOf(FoodCategory)
    }

    private val expense = Transaction(
        type = TransactionType.EXPENSE,
        title = "Lunch",
        amount = Money(1999, "INR"),
        accountId = CashAccount.id,
        categoryId = FoodCategory.id,
        occurredAt = TestNow,
        createdAt = TestNow,
        updatedAt = TestNow,
    )

    private fun TestScope.viewModel(id: Long = 1) = TransactionDetailViewModel(
        navKey = TransactionDetailNavKey(id),
        transactionRepository = transactions,
        accountRepository = accounts,
        categoryRepository = categories,
        clock = FixedClock(),
        timeZone = TimeZone.UTC,
    ).also { viewModel ->
        // stateIn(WhileSubscribed) only runs while collected, like the screen does.
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect() }
        advanceUntilIdle()
    }

    @Test
    fun `resolves the account and category names`() = runTest(mainDispatcherRule.testDispatcher) {
        transactions.save(expense)
        val state = viewModel().uiState.value
        assertFalse(state.isLoading)
        assertEquals(1L, state.transaction?.id)
        assertEquals(CashAccount, state.account)
        assertEquals(FoodCategory, state.category)
        assertEquals(LocalDateTime(2026, 11, 15, 10, 30), state.occurredAt)
        assertEquals(LocalDate(2026, 11, 15), state.today)
    }

    @Test
    fun `reflects edits made elsewhere`() = runTest(mainDispatcherRule.testDispatcher) {
        transactions.save(expense)
        val viewModel = viewModel()
        transactions.save(expense.copy(id = 1, amount = Money(5000, "INR")))
        advanceUntilIdle()
        assertEquals(Money(5000, "INR"), viewModel.uiState.value.transaction?.amount)
    }

    @Test
    fun `delete asks for confirmation, then removes and finishes`() =
        runTest(mainDispatcherRule.testDispatcher) {
            transactions.save(expense)
            val viewModel = viewModel()

            viewModel.onEvent(TransactionDetailEvent.DeleteRequested)
            advanceUntilIdle()
            assertTrue(viewModel.uiState.value.showDeleteConfirmation)
            viewModel.onEvent(TransactionDetailEvent.DeleteDismissed)
            advanceUntilIdle()
            assertFalse(viewModel.uiState.value.showDeleteConfirmation)
            assertEquals(1, transactions.transactions.value.size)

            viewModel.onEvent(TransactionDetailEvent.DeleteRequested)
            viewModel.onEvent(TransactionDetailEvent.DeleteConfirmed)
            advanceUntilIdle()

            assertTrue(transactions.transactions.value.isEmpty())
            assertTrue(viewModel.uiState.value.isFinished)
            assertEquals(R.string.detail_result_deleted, viewModel.uiState.value.resultMessage)
        }

    @Test
    fun `missing transaction finishes`() = runTest(mainDispatcherRule.testDispatcher) {
        val state = viewModel(id = 99).uiState.value
        assertTrue(state.isFinished)
        assertNull(state.resultMessage)
    }
}
