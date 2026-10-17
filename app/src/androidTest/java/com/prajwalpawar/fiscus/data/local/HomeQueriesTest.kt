package com.prajwalpawar.fiscus.data.local

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.prajwalpawar.fiscus.data.local.entity.AccountEntity
import com.prajwalpawar.fiscus.data.local.entity.CategoryEntity
import com.prajwalpawar.fiscus.data.local.entity.TransactionEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

// Checks the balance and totals SQL behind Home against a throwaway in-memory database.
@RunWith(AndroidJUnit4::class)
class HomeQueriesTest {
    private lateinit var db: FiscusDatabase

    private var cash = 0L
    private var card = 0L
    private var food = 0L
    private var salary = 0L

    @Before
    fun setUp() = runBlocking {
        db = Room.inMemoryDatabaseBuilder(
            InstrumentationRegistry.getInstrumentation().targetContext,
            FiscusDatabase::class.java
        ).build()

        cash = db.accountDao().insertAccount(
            AccountEntity(name = "Cash", type = "CASH", openingBalance = 10_000)
        )
        card = db.accountDao().insertAccount(
            AccountEntity(name = "Card", type = "CREDIT_CARD")
        )
        food = db.categoryDao().insertCategory(CategoryEntity(name = "Food", type = "EXPENSE"))
        salary = db.categoryDao().insertCategory(CategoryEntity(name = "Salary", type = "INCOME"))
    }

    @After
    fun tearDown() {
        db.close()
    }

    private suspend fun add(
        type: String,
        amount: Long,
        date: Long,
        category: Long? = null,
        account: Long? = null,
        from: Long? = null,
        to: Long? = null
    ) {
        db.transactionDao().insertTransaction(
            TransactionEntity(
                type = type,
                amount = amount,
                categoryId = category,
                accountId = account,
                sourceAccountId = from,
                destinationAccountId = to,
                title = "t",
                note = null,
                date = date
            )
        )
    }

    @Test
    fun balancesApplyIncomeExpenseAndTransfers() = runBlocking {
        add("INCOME", 50_000, date = 1, category = salary, account = cash)
        add("EXPENSE", 2_000, date = 2, category = food, account = cash)
        add("EXPENSE", 3_000, date = 3, category = food, account = card)
        add("TRANSFER", 1_000, date = 4, from = cash, to = card)

        val balances =
            db.accountDao().observeAccountBalances().first().associate { it.id to it.balance }

        // 10,000 opening + 50,000 - 2,000 - 1,000 transferred out
        assertEquals(57_000L, balances[cash])
        // Card spending is debt: -3,000 + 1,000 paid in
        assertEquals(-2_000L, balances[card])
    }

    @Test
    fun accountWithNoTransactionsShowsOpeningBalance() = runBlocking {
        val balances =
            db.accountDao().observeAccountBalances().first().associate { it.id to it.balance }

        assertEquals(10_000L, balances[cash])
        assertEquals(0L, balances[card])
    }

    @Test
    fun periodTotalsCountOnlyIncomeAndExpenseInsideTheRange() = runBlocking {
        add("INCOME", 50_000, date = 100, category = salary, account = cash)
        add("EXPENSE", 2_000, date = 150, category = food, account = cash)
        add("TRANSFER", 9_999, date = 150, from = cash, to = card)
        // End of range is exclusive.
        add("EXPENSE", 7_000, date = 200, category = food, account = cash)

        val totals =
            db.transactionDao().observePeriodTotals(startMillis = 100, endMillis = 200).first()

        assertEquals(50_000L, totals.income)
        assertEquals(2_000L, totals.expense)
    }

    @Test
    fun recentTransactionsAreNewestFirstWithNames() = runBlocking {
        add("EXPENSE", 2_000, date = 1, category = food, account = cash)
        add("TRANSFER", 1_000, date = 2, from = cash, to = card)

        val recent = db.transactionDao().observeRecentTransactions(limit = 10).first()

        assertEquals(listOf("TRANSFER", "EXPENSE"), recent.map { it.type })
        assertEquals("Cash", recent[0].sourceAccountName)
        assertEquals("Card", recent[0].destinationAccountName)
        assertEquals("Food", recent[1].categoryName)
        assertEquals("Cash", recent[1].accountName)
    }
}
