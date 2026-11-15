@file:OptIn(ExperimentalCoroutinesApi::class) // advanceUntilIdle

package com.prajwalpawar.fiscus.ui.feature.transactions

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

class TransactionsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val transactions = FakeTransactionRepository()
    private val accounts = FakeAccountRepository(transactions).apply {
        this.accounts.value = listOf(CashAccount, BankAccount)
    }
    private val categories = FakeCategoryRepository().apply {
        this.categories.value = listOf(FoodCategory, SalaryCategory)
    }

    /** India is UTC+5:30, so 20:00Z is already the next local day. */
    private val india = TimeZone.of("Asia/Kolkata")

    private fun transaction(
        title: String,
        at: String,
        type: TransactionType = TransactionType.EXPENSE,
        amount: Money = Money(10_000, "INR"),
        note: String = "",
    ) = Transaction(
        type = type,
        title = title,
        amount = amount,
        accountId = CashAccount.id,
        toAccountId = if (type == TransactionType.TRANSFER) BankAccount.id else null,
        categoryId = when (type) {
            TransactionType.EXPENSE -> FoodCategory.id
            TransactionType.INCOME -> SalaryCategory.id
            TransactionType.TRANSFER -> null
        },
        note = note,
        occurredAt = Instant.parse(at),
        createdAt = Instant.parse(at),
        updatedAt = Instant.parse(at),
    )

    private fun TestScope.viewModel() = TransactionsViewModel(
        transactionRepository = transactions,
        accountRepository = accounts,
        categoryRepository = categories,
        clock = FixedClock(),
        timeZone = india,
    ).also { viewModel ->
        // stateIn(WhileSubscribed) only runs while collected, like the screen does.
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        advanceUntilIdle()
    }

    private fun TransactionsUiState.titles() =
        sections.flatMap { section -> section.items.map { it.transaction.title } }

    @Test
    fun `groups by local day, newest first, and resolves names`() =
        runTest(mainDispatcherRule.testDispatcher) {
            transactions.save(transaction("Breakfast", "2026-11-15T03:00:00Z"))
            transactions.save(transaction("Late snack", "2026-11-14T20:00:00Z")) // 15 Nov in India
            transactions.save(transaction("Dinner", "2026-11-14T10:00:00Z"))

            val state = viewModel().uiState.value

            assertFalse(state.isLoading)
            assertEquals(LocalDate(2026, 11, 15), state.today)
            assertEquals(
                listOf(LocalDate(2026, 11, 15), LocalDate(2026, 11, 14)),
                state.sections.map { it.date },
            )
            assertEquals(
                listOf("Breakfast", "Late snack"),
                state.sections[0].items.map { it.transaction.title },
            )
            val entry = state.sections[0].items[0]
            assertEquals(CashAccount, entry.account)
            assertEquals(FoodCategory, entry.category)
        }

    @Test
    fun `day net is income minus expense and ignores transfers`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val noon = "2026-11-15T06:00:00Z"
            val inr = { minor: Long -> Money(minor, "INR") }
            transactions.save(transaction("Salary", noon, TransactionType.INCOME, inr(50_000)))
            transactions.save(transaction("Lunch", noon, amount = Money(20_000, "INR")))
            transactions.save(transaction("Savings", noon, TransactionType.TRANSFER, inr(99_900)))

            assertEquals(Money(30_000, "INR"), viewModel().uiState.value.sections.single().net)
        }

    @Test
    fun `summary totals income and expense of what the list shows`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val noon = "2026-11-15T06:00:00Z"
            val inr = { minor: Long -> Money(minor, "INR") }
            transactions.save(transaction("Salary", noon, TransactionType.INCOME, inr(50_000)))
            transactions.save(transaction("Lunch", noon, amount = inr(20_000)))
            transactions.save(transaction("Cab", "2026-11-10T06:00:00Z", amount = inr(5_000)))
            transactions.save(transaction("Savings", noon, TransactionType.TRANSFER, inr(99_900)))
            val viewModel = viewModel()

            assertEquals(PeriodSummary(inr(50_000), inr(25_000)), viewModel.uiState.value.summary)

            viewModel.onEvent(TransactionsEvent.TypeToggled(TransactionType.EXPENSE))
            advanceUntilIdle()
            val filtered = viewModel.uiState.value.summary
            assertEquals(PeriodSummary(inr(0), inr(25_000)), filtered)
            assertEquals(inr(-25_000), filtered?.net)
        }

    @Test
    fun `clearing one sheet keeps the other filters`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val viewModel = viewModel()
            viewModel.onEvent(TransactionsEvent.TypeToggled(TransactionType.EXPENSE))
            viewModel.onEvent(TransactionsEvent.AccountToggled(CashAccount.id))
            viewModel.onEvent(TransactionsEvent.FilterCleared(FilterPicker.TYPE))
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertTrue(state.selectedTypes.isEmpty())
            assertEquals(setOf(CashAccount.id), state.selectedAccountIds)
        }

    @Test
    fun `day net is null for transfer-only and mixed-currency days`() {
        val transfer = transaction("Savings", "2026-11-15T06:00:00Z", TransactionType.TRANSFER)
        assertNull(dayNet(listOf(transfer)))
        val inr = transaction("Lunch", "2026-11-15T05:00:00Z")
        val usd = transaction("Coffee", "2026-11-15T05:30:00Z", amount = Money(500, "USD"))
        assertNull(dayNet(listOf(inr, usd)))
    }

    @Test
    fun `type filter toggles and clear removes every chip filter`() =
        runTest(mainDispatcherRule.testDispatcher) {
            transactions.save(transaction("Lunch", "2026-11-15T05:00:00Z"))
            transactions.save(transaction("Salary", "2026-11-15T04:00:00Z", TransactionType.INCOME))
            val viewModel = viewModel()

            viewModel.onEvent(TransactionsEvent.TypeToggled(TransactionType.INCOME))
            advanceUntilIdle()
            assertEquals(listOf("Salary"), viewModel.uiState.value.titles())
            assertTrue(viewModel.uiState.value.hasChipFilters)

            viewModel.onEvent(TransactionsEvent.FiltersCleared)
            advanceUntilIdle()
            assertEquals(listOf("Lunch", "Salary"), viewModel.uiState.value.titles())
            assertFalse(viewModel.uiState.value.hasChipFilters)
        }

    @Test
    fun `account filter includes transfers into the account`() =
        runTest(mainDispatcherRule.testDispatcher) {
            transactions.save(transaction("Lunch", "2026-11-15T05:00:00Z"))
            val transfer = transaction("Savings", "2026-11-15T06:00:00Z", TransactionType.TRANSFER)
            transactions.save(transfer)
            val viewModel = viewModel()

            viewModel.onEvent(TransactionsEvent.AccountToggled(BankAccount.id))
            advanceUntilIdle()

            assertEquals(listOf("Savings"), viewModel.uiState.value.titles())
        }

    @Test
    fun `category filter keeps matching categories only`() =
        runTest(mainDispatcherRule.testDispatcher) {
            transactions.save(transaction("Lunch", "2026-11-15T05:00:00Z"))
            transactions.save(transaction("Salary", "2026-11-15T04:00:00Z", TransactionType.INCOME))
            val viewModel = viewModel()

            viewModel.onEvent(TransactionsEvent.CategoryToggled(FoodCategory.id))
            advanceUntilIdle()

            assertEquals(listOf("Lunch"), viewModel.uiState.value.titles())
        }

    @Test
    fun `date range covers whole local days, inclusive`() =
        runTest(mainDispatcherRule.testDispatcher) {
            transactions.save(transaction("Before", "2026-11-12T18:00:00Z")) // 12 Nov 23:30 local
            transactions.save(transaction("First", "2026-11-12T18:30:00Z")) // 13 Nov 00:00 local
            transactions.save(transaction("Last", "2026-11-14T18:29:00Z")) // 14 Nov 23:59 local
            transactions.save(transaction("After", "2026-11-14T18:30:00Z")) // 15 Nov 00:00 local
            val viewModel = viewModel()

            viewModel.onEvent(TransactionsEvent.PickerOpened(FilterPicker.DATE))
            viewModel.onEvent(
                TransactionsEvent.DateRangeChanged(
                    LocalDate(2026, 11, 13)..LocalDate(2026, 11, 14),
                ),
            )
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertEquals(listOf("Last", "First"), state.titles())
            assertNull(state.openPicker)
        }

    @Test
    fun `search matches title or note, ignoring case`() =
        runTest(mainDispatcherRule.testDispatcher) {
            transactions.save(transaction("Lunch", "2026-11-15T05:00:00Z"))
            val groceries =
                transaction("Groceries", "2026-11-15T04:00:00Z", note = "weekly LUNCH boxes")
            transactions.save(groceries)
            transactions.save(transaction("Cab", "2026-11-15T03:00:00Z"))
            val viewModel = viewModel()

            viewModel.onEvent(TransactionsEvent.QueryChanged("lunch"))
            advanceUntilIdle()

            assertEquals(listOf("Lunch", "Groceries"), viewModel.uiState.value.titles())
            assertTrue(viewModel.uiState.value.isSearching)
            assertEquals("lunch", viewModel.uiState.value.query)
            assertEquals(2, viewModel.uiState.value.resultCount)
        }

    @Test
    fun `empty list is filtered-empty only when chip filters are set`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val viewModel = viewModel()
            assertFalse(viewModel.uiState.value.isFilteredEmpty)

            viewModel.onEvent(TransactionsEvent.TypeToggled(TransactionType.TRANSFER))
            advanceUntilIdle()
            assertTrue(viewModel.uiState.value.isFilteredEmpty)
        }
}
