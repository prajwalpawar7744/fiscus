@file:OptIn(ExperimentalCoroutinesApi::class) // advanceUntilIdle

package com.prajwalpawar.fiscus.ui.feature.home

import com.prajwalpawar.fiscus.model.AccountWithBalance
import com.prajwalpawar.fiscus.model.Money
import com.prajwalpawar.fiscus.model.PeriodSummary
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
import kotlinx.datetime.TimeZone
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import kotlin.time.Instant

class HomeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val transactions = FakeTransactionRepository()
    private val accounts = FakeAccountRepository(transactions).apply {
        this.accounts.value = listOf(CashAccount, BankAccount)
    }
    private val categories = FakeCategoryRepository().apply {
        this.categories.value = listOf(FoodCategory, SalaryCategory)
    }

    private fun inr(minor: Long) = Money(minor, "INR")

    private fun transaction(
        title: String,
        at: String,
        type: TransactionType = TransactionType.EXPENSE,
        amount: Money = inr(10_000),
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
        occurredAt = Instant.parse(at),
        createdAt = Instant.parse(at),
        updatedAt = Instant.parse(at),
    )

    private fun TestScope.viewModel() = HomeViewModel(
        transactionRepository = transactions,
        accountRepository = accounts,
        categoryRepository = categories,
        clock = FixedClock(), // 15 Nov 2026
        timeZone = TimeZone.of("Asia/Kolkata"),
    ).also { viewModel ->
        // stateIn(WhileSubscribed) only runs while collected, like the screen does.
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        advanceUntilIdle()
    }

    @Test
    fun `total balance sums every active account`() = runTest(mainDispatcherRule.testDispatcher) {
        accounts.accounts.value = listOf(
            CashAccount.copy(openingBalance = inr(50_000)),
            BankAccount.copy(openingBalance = inr(200_000)),
            BankAccount.copy(id = 3, name = "Old", openingBalance = inr(999), archived = true),
        )
        transactions.save(transaction("Lunch", "2026-11-15T03:00:00Z", amount = inr(10_000)))

        val state = viewModel().uiState.value

        assertFalse(state.isLoading)
        assertEquals(inr(240_000), state.totalBalance)
        assertEquals(listOf("Cash", "Bank"), state.accounts.map { it.account.name })
    }

    @Test
    fun `total balance is null when accounts mix currencies`() =
        runTest(mainDispatcherRule.testDispatcher) {
            accounts.accounts.value = listOf(
                CashAccount,
                BankAccount.copy(currencyCode = "USD", openingBalance = Money(0, "USD")),
            )

            assertNull(viewModel().uiState.value.totalBalance)
        }

    @Test
    fun `month summary counts only this month and ignores transfers`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val income = TransactionType.INCOME
            val transfer = TransactionType.TRANSFER
            transactions.save(transaction("Salary", "2026-11-01T04:00:00Z", income, inr(500_000)))
            transactions.save(transaction("Lunch", "2026-11-10T06:00:00Z", amount = inr(20_000)))
            transactions.save(transaction("Move", "2026-11-12T06:00:00Z", transfer, inr(70_000)))
            transactions.save(transaction("October", "2026-10-20T06:00:00Z", amount = inr(30_000)))

            assertEquals(
                PeriodSummary(income = inr(500_000), expense = inr(20_000)),
                viewModel().uiState.value.monthSummary,
            )
        }

    @Test
    fun `recent shows the newest five with names resolved`() =
        runTest(mainDispatcherRule.testDispatcher) {
            (1..7).forEach { day ->
                transactions.save(transaction("Day $day", "2026-11-0${day}T06:00:00Z"))
            }

            val recent = viewModel().uiState.value.recent

            assertEquals(
                listOf("Day 7", "Day 6", "Day 5", "Day 4", "Day 3"),
                recent.map { it.transaction.title },
            )
            assertEquals("Cash", recent.first().account?.name)
            assertEquals("Food", recent.first().category?.name)
        }

    @Test
    fun `total is zero without accounts`() {
        assertTrue(total(emptyList())?.isZero == true)
        assertEquals(inr(5), total(listOf(AccountWithBalance(CashAccount, inr(5)))))
    }

    @Test
    fun `after deleting every account the hero shows zero and zero tiles`() =
        runTest(mainDispatcherRule.testDispatcher) {
            accounts.accounts.value = emptyList()

            val state = viewModel().uiState.value

            assertTrue(state.totalBalance?.isZero == true)
            assertTrue(state.monthSummary?.income?.isZero == true)
            assertTrue(state.monthSummary?.expense?.isZero == true)
        }

    @Test
    fun `spent share rounds down and handles large amounts`() {
        assertEquals(25, percentOf(125_000, 500_000))
        assertEquals(33, percentOf(1, 3))
        assertEquals(100, percentOf(500_000, 500_000))
        assertEquals(50, percentOf(Long.MAX_VALUE / 200, Long.MAX_VALUE / 100))
    }
}
