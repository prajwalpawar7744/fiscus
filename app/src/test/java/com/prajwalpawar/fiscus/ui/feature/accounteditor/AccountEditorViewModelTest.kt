@file:OptIn(ExperimentalCoroutinesApi::class) // advanceUntilIdle

package com.prajwalpawar.fiscus.ui.feature.accounteditor

import com.prajwalpawar.fiscus.R
import com.prajwalpawar.fiscus.model.AccountType
import com.prajwalpawar.fiscus.model.AccountWithBalance
import com.prajwalpawar.fiscus.model.Money
import com.prajwalpawar.fiscus.model.Transaction
import com.prajwalpawar.fiscus.model.TransactionType
import com.prajwalpawar.fiscus.testing.CashAccount
import com.prajwalpawar.fiscus.testing.FakeAccountRepository
import com.prajwalpawar.fiscus.testing.FakeTransactionRepository
import com.prajwalpawar.fiscus.testing.FoodCategory
import com.prajwalpawar.fiscus.testing.MainDispatcherRule
import com.prajwalpawar.fiscus.ui.navigation.AccountEditorNavKey
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

class AccountEditorViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val transactions = FakeTransactionRepository()
    private val accounts = FakeAccountRepository(transactions)

    private fun inr(minor: Long) = Money(minor, "INR")

    private fun TestScope.viewModel(id: Long? = null) = AccountEditorViewModel(
        navKey = AccountEditorNavKey(id),
        accountRepository = accounts,
    ).also { viewModel ->
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        advanceUntilIdle()
    }

    private suspend fun spend(minor: Long) = transactions.save(
        Transaction(
            type = TransactionType.EXPENSE,
            title = "Lunch",
            amount = inr(minor),
            accountId = CashAccount.id,
            categoryId = FoodCategory.id,
            occurredAt = Instant.parse("2026-11-15T03:00:00Z"),
            createdAt = Instant.parse("2026-11-15T03:00:00Z"),
            updatedAt = Instant.parse("2026-11-15T03:00:00Z"),
        ),
    )

    @Test
    fun `adds a card that starts in debt, typed then saved with no idle`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val viewModel = viewModel()
            viewModel.onEvent(AccountEditorEvent.TypeChanged(AccountType.CARD))
            viewModel.onEvent(AccountEditorEvent.InDebtChanged(true))
            viewModel.onEvent(AccountEditorEvent.NameChanged("  Credit card "))
            viewModel.onEvent(AccountEditorEvent.AmountChanged("5000"))
            viewModel.onEvent(AccountEditorEvent.Save)
            advanceUntilIdle()

            val saved = accounts.accounts.value.single()
            assertEquals("Credit card", saved.name)
            assertEquals(AccountType.CARD, saved.type)
            assertEquals(inr(-500_000), saved.openingBalance)
            assertEquals(R.string.account_result_added, viewModel.uiState.value.resultMessage)
        }

    @Test
    fun `a blank balance saves as zero`() = runTest(mainDispatcherRule.testDispatcher) {
        val viewModel = viewModel()
        viewModel.onEvent(AccountEditorEvent.NameChanged("Wallet"))
        viewModel.onEvent(AccountEditorEvent.Save)
        advanceUntilIdle()

        assertEquals(inr(0), accounts.accounts.value.single().openingBalance)
    }

    @Test
    fun `saving a default account makes it the only default`() =
        runTest(mainDispatcherRule.testDispatcher) {
            accounts.accounts.value = listOf(CashAccount.copy(isDefault = true))
            val viewModel = viewModel()
            viewModel.onEvent(AccountEditorEvent.NameChanged("Bank"))
            viewModel.onEvent(AccountEditorEvent.DefaultChanged(true))
            viewModel.onEvent(AccountEditorEvent.Save)
            advanceUntilIdle()

            val defaults = accounts.accounts.value.filter { it.isDefault }.map { it.name }
            assertEquals(listOf("Bank"), defaults)
        }

    @Test
    fun `an edited account keeps its currency and default flag`() =
        runTest(mainDispatcherRule.testDispatcher) {
            accounts.accounts.value = listOf(
                CashAccount.copy(
                    currencyCode = "USD",
                    openingBalance = Money(0, "USD"),
                    isDefault = true,
                ),
            )
            val viewModel = viewModel(CashAccount.id)

            assertTrue(viewModel.uiState.value.isDefault)
            assertEquals("USD", viewModel.uiState.value.currencyCode)
            viewModel.onEvent(AccountEditorEvent.Save)
            advanceUntilIdle()
            assertEquals("USD", accounts.accounts.value.single().currencyCode)
        }

    @Test
    fun `reconciling to the current balance back-solves the opening balance`() =
        runTest(mainDispatcherRule.testDispatcher) {
            accounts.accounts.value = listOf(CashAccount.copy(openingBalance = inr(100_000)))
            spend(30_000) // balance ₹700
            val viewModel = viewModel(CashAccount.id)

            viewModel.onEvent(AccountEditorEvent.BalanceModeChanged(BalanceMode.CURRENT))
            advanceUntilIdle()
            assertEquals("700", viewModel.uiState.value.amountText)

            viewModel.onEvent(AccountEditorEvent.AmountChanged("500"))
            advanceUntilIdle()
            assertEquals(inr(80_000), viewModel.uiState.value.openingPreview)

            viewModel.onEvent(AccountEditorEvent.Save)
            advanceUntilIdle()
            assertEquals(inr(80_000), accounts.accounts.value.single().openingBalance)
            assertEquals(R.string.account_result_updated, viewModel.uiState.value.resultMessage)
        }

    @Test
    fun `reconcile can go below zero and overflow is an error`() {
        val original = AccountWithBalance(CashAccount.copy(openingBalance = inr(0)), inr(-10_000))
        assertEquals(
            inr(-40_000),
            openingBalanceFor(BalanceMode.CURRENT, inr(-50_000), original),
        )
        val huge = AccountWithBalance(
            CashAccount.copy(openingBalance = inr(0)),
            inr(Long.MIN_VALUE + 1),
        )
        assertNull(openingBalanceFor(BalanceMode.CURRENT, inr(Long.MAX_VALUE), huge))
        assertEquals(inr(7), openingBalanceFor(BalanceMode.OPENING, inr(7), huge))
    }

    @Test
    fun `a malformed amount blocks saving`() = runTest(mainDispatcherRule.testDispatcher) {
        val viewModel = viewModel()
        viewModel.onEvent(AccountEditorEvent.NameChanged("Cash"))
        viewModel.onEvent(AccountEditorEvent.AmountChanged("1.234"))
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(R.string.account_amount_error, state.amountError)
        assertFalse(state.canSave)
    }
}
