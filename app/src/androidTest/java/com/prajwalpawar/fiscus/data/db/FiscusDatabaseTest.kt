package com.prajwalpawar.fiscus.data.db

import androidx.room3.Room
import androidx.room3.executeSQL
import androidx.room3.useWriterConnection
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.prajwalpawar.fiscus.data.db.entity.AccountEntity
import com.prajwalpawar.fiscus.data.db.entity.TransactionEntity
import com.prajwalpawar.fiscus.data.repository.containsLikePattern
import com.prajwalpawar.fiscus.model.AccountType
import com.prajwalpawar.fiscus.model.CategoryKind
import com.prajwalpawar.fiscus.model.TransactionType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
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
        assertTrue(categories.all { it.isSeeded })
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

    @Test
    fun accountsArchiveAndUnusedOnesDelete() = runTest {
        val dao = database.accountDao()
        val cashId = dao.observeAll().first().single().id
        val categoryId = database.categoryDao().observeAll().first().first().id
        val bankId = dao.upsert(account("Bank", openingMinor = -5_000))
        val lunch = transaction(TransactionType.EXPENSE, 100, cashId, categoryId = categoryId)
        database.transactionDao().upsert(lunch)

        dao.setArchived(cashId, true)
        assertTrue(dao.observeById(cashId).first()?.archived == true)
        dao.deleteById(bankId)
        assertEquals(listOf(cashId), dao.observeAll().first().map { it.id })
        assertTrue(
            "RESTRICT foreign key should block deleting a used account",
            runCatching { dao.deleteById(cashId) }.isFailure,
        )
    }

    @Test
    fun usageCountsTransactionsPerCategoryAndAccount() = runTest {
        val cashId = database.accountDao().observeAll().first().single().id
        val categoryId = database.categoryDao().observeAll().first().first().id
        val bankId = database.accountDao().upsert(account("Bank", openingMinor = 0))
        database.transactionDao().upsert(transaction(TransactionType.EXPENSE, 100, cashId, categoryId = categoryId))
        database.transactionDao().upsert(transaction(TransactionType.TRANSFER, 50, cashId, toAccountId = bankId))

        val usage = database.categoryDao().observeUsage().first().associate { it.categoryId to it.transactionCount }
        assertEquals(1, usage[categoryId])
        val counts = database.accountDao().observeBalances().first().associate { it.accountId to it.transactionCount }
        assertEquals(mapOf(cashId to 2, bankId to 1), counts)
    }

    @Test
    fun categoriesArchiveAndDelete() = runTest {
        val dao = database.categoryDao()
        val category = dao.observeAll().first().first()
        dao.setArchived(category.id, true)
        assertTrue(dao.observeById(category.id).first()?.archived == true)
        dao.deleteById(category.id)
        assertNull(dao.observeById(category.id).first())
    }

    @Test
    fun onlyOneAccountIsDefaultAndArchivingClearsIt() = runTest {
        val dao = database.accountDao()
        val cashId = dao.observeAll().first().single().id
        assertTrue("Seeded Cash is the default", dao.observeById(cashId).first()?.isDefault == true)
        val bankId = dao.upsert(account("Bank", openingMinor = 0))

        dao.setDefault(bankId)
        assertEquals(listOf(bankId), dao.observeAll().first().filter { it.isDefault }.map { it.id })
        dao.setArchived(bankId, true)
        assertTrue(dao.observeAll().first().none { it.isDefault })
    }

    @Test
    fun filteredQueryAppliesEveryCondition() = runTest {
        val cashId = database.accountDao().observeAll().first().single().id
        val bankId = database.accountDao().upsert(account("Bank", openingMinor = 0))
        val categories = database.categoryDao().observeAll().first()
        val food = categories.first { it.kind == CategoryKind.EXPENSE }.id
        val salary = categories.first { it.kind == CategoryKind.INCOME }.id
        val dao = database.transactionDao()
        val expense = TransactionType.EXPENSE
        val lunch = dao.upsert(
            transaction(expense, 100, cashId, categoryId = food, at = 1_000, title = "Lunch"),
        )
        val pay = dao.upsert(
            transaction(TransactionType.INCOME, 900, cashId, categoryId = salary, at = 2_000),
        )
        val move = dao.upsert(
            transaction(TransactionType.TRANSFER, 50, cashId, toAccountId = bankId, at = 3_000),
        )
        val deal = dao.upsert(
            transaction(expense, 10, cashId, categoryId = food, at = 4_000, note = "50% off_sale"),
        )

        suspend fun ids(
            types: Set<TransactionType> = emptySet(),
            accountIds: Set<Long> = emptySet(),
            categoryIds: Set<Long> = emptySet(),
            from: Long? = null,
            to: Long? = null,
            query: String = "",
        ) = dao.observeFiltered(
            allTypes = types.isEmpty(),
            types = types,
            allAccounts = accountIds.isEmpty(),
            accountIds = accountIds,
            allCategories = categoryIds.isEmpty(),
            categoryIds = categoryIds,
            fromMillis = from,
            toMillisExclusive = to,
            pattern = containsLikePattern(query),
        ).first().map { it.id }

        assertEquals(listOf(deal, move, pay, lunch), ids())
        assertEquals(listOf(pay), ids(types = setOf(TransactionType.INCOME)))
        // A transfer into the account counts for that account.
        assertEquals(listOf(move), ids(accountIds = setOf(bankId)))
        assertEquals(listOf(deal, lunch), ids(categoryIds = setOf(food)))
        assertEquals(listOf(move, pay), ids(from = 2_000, to = 4_000))
        assertEquals(listOf(lunch), ids(query = "LUN"))
        // % and _ are literal, and the note is searched too.
        assertEquals(listOf(deal), ids(query = "50% off_"))
        assertEquals(emptyList<Long>(), ids(query = "5_%"))
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
        title: String = "Test",
        note: String = "",
    ) = TransactionEntity(
        type = type,
        title = title,
        amountMinor = amountMinor,
        currencyCode = "INR",
        accountId = accountId,
        toAccountId = toAccountId,
        categoryId = categoryId,
        note = note,
        occurredAtMillis = at,
        createdAtMillis = at,
        updatedAtMillis = at,
    )
}
