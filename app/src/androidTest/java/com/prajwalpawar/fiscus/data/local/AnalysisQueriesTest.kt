package com.prajwalpawar.fiscus.data.local

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.prajwalpawar.fiscus.data.local.entity.AccountEntity
import com.prajwalpawar.fiscus.data.local.entity.CategoryEntity
import com.prajwalpawar.fiscus.data.local.entity.TransactionEntity
import com.prajwalpawar.fiscus.data.model.TransactionType
import com.prajwalpawar.fiscus.data.repository.TransactionRepository
import com.prajwalpawar.fiscus.util.monthRange
import com.prajwalpawar.fiscus.util.monthSpanRange
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneOffset

// Checks the queries behind the Analysis tab.
@RunWith(AndroidJUnit4::class)
class AnalysisQueriesTest {
    private lateinit var db: FiscusDatabase
    private lateinit var repository: TransactionRepository

    private var cash = 0L
    private var food = 0L
    private var travel = 0L
    private var salary = 0L

    // Stored form: the local day as UTC midnight.
    private fun day(y: Int, m: Int, d: Int) =
        LocalDate.of(y, m, d).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

    private suspend fun add(type: String, amount: Long, date: Long, category: Long) {
        db.transactionDao().insertTransaction(
            TransactionEntity(
                type = type, amount = amount, categoryId = category, accountId = cash,
                sourceAccountId = null, destinationAccountId = null,
                title = "t", note = null, date = date
            )
        )
    }

    @Before
    fun setUp() = runBlocking {
        db = Room.inMemoryDatabaseBuilder(
            InstrumentationRegistry.getInstrumentation().targetContext,
            FiscusDatabase::class.java
        ).build()
        repository = TransactionRepository(db, db.transactionDao())

        cash = db.accountDao().insertAccount(AccountEntity(name = "Cash", type = "CASH"))
        food = db.categoryDao().insertCategory(CategoryEntity(name = "Food", type = "EXPENSE"))
        travel = db.categoryDao().insertCategory(CategoryEntity(name = "Travel", type = "EXPENSE"))
        salary = db.categoryDao().insertCategory(CategoryEntity(name = "Salary", type = "INCOME"))

        add("EXPENSE", 300, day(2026, 9, 30), food)   // last day of September
        add("EXPENSE", 100, day(2026, 10, 1), food)   // first day of October
        add("EXPENSE", 250, day(2026, 10, 15), travel)
        add("EXPENSE", 50, day(2026, 10, 31), food)   // last day of October
        add("INCOME", 5000, day(2026, 10, 5), salary)
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun categoryTotalsAreLargestFirstForOneType() = runBlocking {
        val totals = repository.observeCategoryTotals(TransactionType.EXPENSE, monthRange(YearMonth.of(2026, 10))).first()

        assertEquals(listOf("Travel" to 250L, "Food" to 150L), totals.map { it.name to it.total })
    }

    @Test
    fun monthlyTotalsGroupByCalendarMonthAtTheEdges() = runBlocking {
        val rows = repository.observeMonthlyTotals(
            monthSpanRange(YearMonth.of(2026, 9), YearMonth.of(2026, 10))
        ).first()

        assertEquals(
            listOf(
                Triple(YearMonth.of(2026, 9), 0L, 300L),
                Triple(YearMonth.of(2026, 10), 5000L, 400L)
            ),
            rows.map { Triple(it.month, it.income, it.expense) }
        )
    }

    @Test
    fun dailyTotalsGroupByStoredDayAndCountTransactions() = runBlocking {
        val range = com.prajwalpawar.fiscus.util.Period(LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 15)).toDateRange()

        val days = repository.observeDailyTotals(range).first()
        assertEquals(
            listOf(LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 5), LocalDate.of(2026, 10, 15)),
            days.map { it.day }
        )
        assertEquals(listOf(100L, 0L, 250L), days.map { it.expense })

        // 3 transactions fall inside 1-15 Oct.
        assertEquals(3, repository.observePeriodTotals(range).first().count)
    }

    @Test
    fun largestExpensesAreBiggestFirstWithinTheMonth() = runBlocking {
        val largest = repository.observeLargestExpenses(monthRange(YearMonth.of(2026, 10)), limit = 2).first()

        assertEquals(listOf(250L, 100L), largest.map { it.amount })
    }
}
