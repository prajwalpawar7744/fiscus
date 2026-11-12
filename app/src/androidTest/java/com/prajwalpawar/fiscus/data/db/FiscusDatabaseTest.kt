package com.prajwalpawar.fiscus.data.db

import androidx.room3.Room
import androidx.room3.executeSQL
import androidx.room3.useWriterConnection
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.prajwalpawar.fiscus.data.db.entity.AccountEntity
import com.prajwalpawar.fiscus.data.db.entity.TransactionEntity
import com.prajwalpawar.fiscus.model.AccountType
import com.prajwalpawar.fiscus.model.CategoryKind
import com.prajwalpawar.fiscus.model.TransactionType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FiscusDatabaseTest {

    private lateinit var database: FiscusDatabase

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, FiscusDatabase::class.java)
            .setDriver(BundledSQLiteDriver())
            .addCallback(SeedCallback(context, currencyCode = "INR"))
            .build()
    }

    @After
    fun tearDown() = database.close()

    @Test
    fun seedCreatesCashAccountAndStarterCategories() = runTest {
        val accounts = database.accountDao().observeAll().first()
        assertEquals(1, accounts.size)
        assertEquals(AccountType.CASH, accounts.single().type)
        assertEquals("INR", accounts.single().currencyCode)

        val categories = database.categoryDao().observeAll().first()
        assertEquals(9, categories.count { it.kind == CategoryKind.EXPENSE })
        assertEquals(4, categories.count { it.kind == CategoryKind.INCOME })
    }

    @Test
    fun balanceCountsIncomeExpenseAndTransfers() = runTest {
        val cashId = database.accountDao().observeAll().first().single().id
        val bankId = database.accountDao().upsert(account("Bank", openingMinor = 10_000))
        val categoryId = database.categoryDao().observeAll().first().first().id
        val dao = database.transactionDao()
        dao.upsert(transaction(TransactionType.INCOME, 5_000, cashId, categoryId = categoryId))
        dao.upsert(transaction(TransactionType.EXPENSE, 1_200, cashId, categoryId = categoryId))
        dao.upsert(transaction(TransactionType.TRANSFER, 800, cashId, toAccountId = bankId))

        val balances = database.accountDao().observeBalances().first().associate { it.accountId to it.balanceMinor }
        assertEquals(5_000L - 1_200L - 800L, balances[cashId])
        assertEquals(10_000L + 800L, balances[bankId])
    }

    @Test
    fun transactionsAreNewestFirstAndDeletable() = runTest {
        val cashId = database.accountDao().observeAll().first().single().id
        val categoryId = database.categoryDao().observeAll().first().first().id
        val dao = database.transactionDao()
        val olderId = dao.upsert(transaction(TransactionType.EXPENSE, 100, cashId, categoryId = categoryId, at = 1_000))
        val newerId = dao.upsert(transaction(TransactionType.EXPENSE, 200, cashId, categoryId = categoryId, at = 2_000))

        assertEquals(listOf(newerId, olderId), dao.observeAll().first().map { it.id })
        dao.deleteById(newerId)
        assertEquals(listOf(olderId), dao.observeAll().first().map { it.id })
    }

    @Test
    fun accountWithTransactionsCannotBeDeleted() = runTest {
        val cashId = database.accountDao().observeAll().first().single().id
        val categoryId = database.categoryDao().observeAll().first().first().id
        database.transactionDao().upsert(transaction(TransactionType.EXPENSE, 100, cashId, categoryId = categoryId))

        val failed = runCatching {
            database.useWriterConnection { transactor ->
                transactor.executeSQL("DELETE FROM accounts WHERE id = $cashId")
            }
        }.isFailure
        assertTrue("RESTRICT foreign key should block deleting a used account", failed)
    }

    private fun account(name: String, openingMinor: Long) = AccountEntity(
        name = name,
        type = AccountType.BANK,
        currencyCode = "INR",
        openingBalanceMinor = openingMinor,
        archived = false,
    )

    private fun transaction(
        type: TransactionType,
        amountMinor: Long,
        accountId: Long,
        toAccountId: Long? = null,
        categoryId: Long? = null,
        at: Long = 1_000,
    ) = TransactionEntity(
        type = type,
        amountMinor = amountMinor,
        currencyCode = "INR",
        accountId = accountId,
        toAccountId = toAccountId,
        categoryId = categoryId,
        note = "",
        occurredAtMillis = at,
        createdAtMillis = at,
        updatedAtMillis = at,
    )
}
