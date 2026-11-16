@file:OptIn(ExperimentalCoroutinesApi::class) // advanceUntilIdle

package com.prajwalpawar.fiscus.ui.feature.accountdetail

import com.prajwalpawar.fiscus.R
import com.prajwalpawar.fiscus.model.Money
import com.prajwalpawar.fiscus.model.Transaction
import com.prajwalpawar.fiscus.model.TransactionType
import com.prajwalpawar.fiscus.ui.navigation.AccountDetailNavKey
import com.prajwalpawar.fiscus.testing.BankAccount
import com.prajwalpawar.fiscus.testing.CashAccount
import com.prajwalpawar.fiscus.testing.FakeAccountRepository
import com.prajwalpawar.fiscus.testing.FakeTransactionRepository
import com.prajwalpawar.fiscus.testing.FoodCategory
import com.prajwalpawar.fiscus.testing.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import kotlin.time.Instant

class AccountDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val transactions = FakeTransactionRepository()
    private val accounts = FakeAccountRepository(transactions).apply {
        this.accounts.value = listOf(CashAccount, BankAccount)
    }

    private fun TestScope.viewModel(id: Long = CashAccount.id) = AccountDetailViewModel(
        navKey = AccountDetailNavKey(id),
        accountRepository = accounts,
        transactionRepository = transactions,
    ).also { viewModel ->
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        advanceUntilIdle()
    }

    private suspend fun addLunch() = transactions.save(
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
    fun `an unused account can be deleted, then the page closes`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val viewModel = viewModel()
            assertTrue(viewModel.uiState.value.canDelete)

            viewModel.onEvent(AccountDetailEvent.DeleteRequested)
            advanceUntilIdle()
            assertTrue(viewModel.uiState.value.showDeleteConfirmation)
            viewModel.onEvent(AccountDetailEvent.DeleteConfirmed)
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertTrue(state.isFinished)
            assertEquals(R.string.account_result_deleted, state.resultMessage)
            assertEquals(listOf("Bank"), accounts.accounts.value.map { it.name })
        }

    @Test
    fun `a used account shows its transaction count and can't be deleted`() =
        runTest(mainDispatcherRule.testDispatcher) {
            addLunch()
            val viewModel = viewModel()

            val state = viewModel.uiState.value
            assertFalse(state.canDelete)
            assertEquals(1, state.transactionCount)
            assertEquals(Money(-10_000, "INR"), state.account?.balance)

            viewModel.onEvent(AccountDetailEvent.DeleteRequested)
            advanceUntilIdle()
            assertFalse(viewModel.uiState.value.showDeleteConfirmation)
        }

    @Test
    fun `archive and unarchive confirm on the page`() = runTest(mainDispatcherRule.testDispatcher) {
        accounts.accounts.value = listOf(CashAccount.copy(isDefault = true), BankAccount)
        addLunch()
        val viewModel = viewModel()

        viewModel.onEvent(AccountDetailEvent.ArchiveToggled)
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.account?.account?.archived == true)
        // An archived account can't stay the default (ADR-037).
        assertFalse(viewModel.uiState.value.account?.account?.isDefault == true)
        assertEquals(R.string.account_result_archived, viewModel.uiState.value.userMessage)

        viewModel.onEvent(AccountDetailEvent.UserMessageShown)
        viewModel.onEvent(AccountDetailEvent.ArchiveToggled)
        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.account?.account?.archived == true)
        assertEquals(R.string.account_result_unarchived, viewModel.uiState.value.userMessage)
    }

    @Test
    fun `a missing account finishes without a message`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val state = viewModel(id = 99).uiState.value
            assertTrue(state.isFinished)
            assertEquals(null, state.resultMessage)
        }
}
