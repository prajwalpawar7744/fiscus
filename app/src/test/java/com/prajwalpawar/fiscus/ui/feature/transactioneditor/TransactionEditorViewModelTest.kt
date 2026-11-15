@file:OptIn(ExperimentalCoroutinesApi::class) // advanceUntilIdle

package com.prajwalpawar.fiscus.ui.feature.transactioneditor

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
import com.prajwalpawar.fiscus.testing.SalaryCategory
import com.prajwalpawar.fiscus.testing.TestNow
import com.prajwalpawar.fiscus.ui.navigation.TransactionEditorNavKey
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import kotlin.time.Instant

class TransactionEditorViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val transactions = FakeTransactionRepository()
    private val accounts = FakeAccountRepository(transactions).apply {
        this.accounts.value = listOf(CashAccount, BankAccount)
    }
    private val categories = FakeCategoryRepository().apply {
        this.categories.value = listOf(FoodCategory, SalaryCategory)
    }

    private fun TestScope.viewModel(navKey: TransactionEditorNavKey = TransactionEditorNavKey()) =
        TransactionEditorViewModel(
            navKey = navKey,
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
    fun `new expense starts on today with the first account and expense categories`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val state = viewModel().uiState.value
            assertFalse(state.isLoading)
            assertTrue(state.isNew)
            assertEquals(TransactionType.EXPENSE, state.type)
            assertEquals(LocalDate(2026, 11, 15), state.date)
            assertEquals(CashAccount.id, state.accountId)
            assertEquals(listOf(FoodCategory), state.categories)
            assertNull(state.categoryId)
            assertFalse(state.canSave)
        }

    @Test
    fun `saving an expense stores it in minor units and finishes`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val viewModel = viewModel()
            viewModel.onEvent(TransactionEditorEvent.AmountChanged("250.50"))
            viewModel.onEvent(TransactionEditorEvent.TitleChanged("  Team lunch "))
            viewModel.onEvent(TransactionEditorEvent.CategorySelected(FoodCategory.id))
            viewModel.onEvent(TransactionEditorEvent.NoteChanged("  Lunch "))
            advanceUntilIdle()
            assertTrue(viewModel.uiState.value.canSave)

            viewModel.onEvent(TransactionEditorEvent.Save)
            advanceUntilIdle()

            val saved = transactions.transactions.value.single()
            assertEquals(Money(25_050, "INR"), saved.amount)
            assertEquals(TransactionType.EXPENSE, saved.type)
            assertEquals(CashAccount.id, saved.accountId)
            assertEquals(FoodCategory.id, saved.categoryId)
            assertEquals("Lunch", saved.note)
            assertEquals("Team lunch", saved.title)
            assertEquals(TestNow, saved.occurredAt)
            assertEquals(TestNow, saved.createdAt)
            assertTrue(viewModel.uiState.value.isFinished)
            assertEquals(R.string.editor_result_expense_added, viewModel.uiState.value.resultMessage)
        }

    @Test
    fun `invalid amount shows an error and saves nothing`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val viewModel = viewModel()
            viewModel.onEvent(TransactionEditorEvent.AmountChanged("1.999"))
            viewModel.onEvent(TransactionEditorEvent.CategorySelected(FoodCategory.id))
            viewModel.onEvent(TransactionEditorEvent.Save)
            advanceUntilIdle()

            assertEquals(R.string.editor_amount_error, viewModel.uiState.value.amountError)
            assertTrue(transactions.transactions.value.isEmpty())
        }

    @Test
    fun `typing ignores characters that are not part of an amount`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val viewModel = viewModel()
            viewModel.onEvent(TransactionEditorEvent.AmountChanged("12"))
            viewModel.onEvent(TransactionEditorEvent.AmountChanged("12a"))
            advanceUntilIdle()
            assertEquals("12", viewModel.uiState.value.amountText)
        }

    @Test
    fun `switching type clears the category and loads matching categories`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val viewModel = viewModel()
            viewModel.onEvent(TransactionEditorEvent.CategorySelected(FoodCategory.id))
            viewModel.onEvent(TransactionEditorEvent.TypeChanged(TransactionType.INCOME))
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertNull(state.categoryId)
            assertEquals(listOf(SalaryCategory), state.categories)
        }

    @Test
    fun `transfer picks a different destination account and has no category`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val viewModel = viewModel(TransactionEditorNavKey(type = TransactionType.TRANSFER))
            val state = viewModel.uiState.value
            assertEquals(CashAccount.id, state.accountId)
            assertEquals(BankAccount.id, state.toAccountId)
            assertTrue(state.categories.isEmpty())

            viewModel.onEvent(TransactionEditorEvent.AmountChanged("100"))
            viewModel.onEvent(TransactionEditorEvent.TitleChanged("Savings"))
            viewModel.onEvent(TransactionEditorEvent.Save)
            advanceUntilIdle()

            val saved = transactions.transactions.value.single()
            assertEquals(TransactionType.TRANSFER, saved.type)
            assertEquals(BankAccount.id, saved.toAccountId)
            assertNull(saved.categoryId)
        }

    @Test
    fun `transfer cannot be saved with a single account`() =
        runTest(mainDispatcherRule.testDispatcher) {
            accounts.accounts.value = listOf(CashAccount)
            val viewModel = viewModel(TransactionEditorNavKey(type = TransactionType.TRANSFER))
            viewModel.onEvent(TransactionEditorEvent.AmountChanged("100"))
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertTrue(state.toAccounts.isEmpty())
            assertFalse(state.canSave)
        }

    @Test
    fun `editing loads the transaction and keeps createdAt and time of day`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val created = Instant.parse("2026-11-01T08:15:00Z")
            transactions.save(
                Transaction(
                    type = TransactionType.EXPENSE,
                    title = "Coffee",
                    amount = Money(1999, "INR"),
                    accountId = CashAccount.id,
                    categoryId = FoodCategory.id,
                    note = "Coffee",
                    occurredAt = created,
                    createdAt = created,
                    updatedAt = created,
                ),
            )
            val viewModel = viewModel(TransactionEditorNavKey(transactionId = 1))
            val state = viewModel.uiState.value
            assertFalse(state.isNew)
            assertEquals("19.99", state.amountText)
            assertEquals(FoodCategory.id, state.categoryId)
            assertEquals(LocalDate(2026, 11, 1), state.date)
            assertEquals("Coffee", state.note)
            assertEquals("Coffee", state.title)

            viewModel.onEvent(TransactionEditorEvent.AmountChanged("25"))
            viewModel.onEvent(TransactionEditorEvent.DateSelected(LocalDate(2026, 11, 3)))
            viewModel.onEvent(TransactionEditorEvent.Save)
            advanceUntilIdle()

            val saved = transactions.transactions.value.single()
            assertEquals(1L, saved.id)
            assertEquals(Money(2500, "INR"), saved.amount)
            assertEquals(Instant.parse("2026-11-03T08:15:00Z"), saved.occurredAt)
            assertEquals(created, saved.createdAt)
            assertEquals(TestNow, saved.updatedAt)
            assertEquals(R.string.editor_result_updated, viewModel.uiState.value.resultMessage)
        }

    @Test
    fun `editing a missing transaction finishes immediately`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val viewModel = viewModel(TransactionEditorNavKey(transactionId = 42))
            assertTrue(viewModel.uiState.value.isFinished)
            assertNull(viewModel.uiState.value.resultMessage)
        }

    @Test
    fun `save hint says what is missing, in order, until the form is complete`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val viewModel = viewModel()
            // Blank amount: the hero prompts for it, so the Save hint stays quiet.
            assertNull(viewModel.uiState.value.saveHint)

            viewModel.onEvent(TransactionEditorEvent.AmountChanged("0"))
            advanceUntilIdle()
            assertEquals(R.string.editor_hint_amount, viewModel.uiState.value.saveHint)

            viewModel.onEvent(TransactionEditorEvent.AmountChanged("10"))
            advanceUntilIdle()
            assertEquals(R.string.editor_hint_title, viewModel.uiState.value.saveHint)

            viewModel.onEvent(TransactionEditorEvent.TitleChanged("Lunch"))
            advanceUntilIdle()
            assertEquals(R.string.editor_hint_category, viewModel.uiState.value.saveHint)

            viewModel.onEvent(TransactionEditorEvent.CategorySelected(FoodCategory.id))
            advanceUntilIdle()
            assertNull(viewModel.uiState.value.saveHint)
            assertTrue(viewModel.uiState.value.canSave)
        }

    @Test
    fun `save hint asks for a destination when a transfer has none`() =
        runTest(mainDispatcherRule.testDispatcher) {
            accounts.accounts.value = listOf(CashAccount)
            val viewModel = viewModel(TransactionEditorNavKey(type = TransactionType.TRANSFER))
            viewModel.onEvent(TransactionEditorEvent.AmountChanged("10"))
            viewModel.onEvent(TransactionEditorEvent.TitleChanged("Savings"))
            advanceUntilIdle()
            assertEquals(R.string.editor_hint_to_account, viewModel.uiState.value.saveHint)
        }


    @Test
    fun `a blank title blocks saving`() = runTest(mainDispatcherRule.testDispatcher) {
        val viewModel = viewModel()
        viewModel.onEvent(TransactionEditorEvent.AmountChanged("10"))
        viewModel.onEvent(TransactionEditorEvent.CategorySelected(FoodCategory.id))
        viewModel.onEvent(TransactionEditorEvent.TitleChanged("   "))
        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.canSave)

        viewModel.onEvent(TransactionEditorEvent.Save)
        advanceUntilIdle()
        assertTrue(transactions.transactions.value.isEmpty())
    }

}
