package com.prajwalpawar.fiscus.data.local

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.prajwalpawar.fiscus.data.local.entity.AccountEntity
import com.prajwalpawar.fiscus.data.local.entity.CategoryEntity
import com.prajwalpawar.fiscus.data.local.entity.TransactionEntity
import com.prajwalpawar.fiscus.data.repository.AccountRepository
import com.prajwalpawar.fiscus.data.repository.CategoryRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

// Checks usage counts, archiving and the delete guards behind the manage pages.
@RunWith(AndroidJUnit4::class)
class ManageQueriesTest {
    private lateinit var db: FiscusDatabase
    private lateinit var categories: CategoryRepository
    private lateinit var accounts: AccountRepository

    private var cash = 0L
    private var bank = 0L
    private var spare = 0L
    private var food = 0L
    private var unused = 0L

    @Before
    fun setUp() = runBlocking {
        db = Room.inMemoryDatabaseBuilder(
            InstrumentationRegistry.getInstrumentation().targetContext,
            FiscusDatabase::class.java
        ).build()
        categories = CategoryRepository(db.categoryDao())
        accounts = AccountRepository(db.accountDao())

        cash = db.accountDao().insertAccount(AccountEntity(name = "Cash", type = "CASH", isDefault = true))
        bank = db.accountDao().insertAccount(AccountEntity(name = "Bank", type = "BANK"))
        spare = db.accountDao().insertAccount(AccountEntity(name = "Spare", type = "OTHER"))
        food = db.categoryDao().insertCategory(CategoryEntity(name = "Food", type = "EXPENSE"))
        unused = db.categoryDao().insertCategory(CategoryEntity(name = "Unused", type = "EXPENSE"))

        insert(type = "EXPENSE", category = food, account = cash)
        insert(type = "TRANSFER", from = cash, to = bank)
    }

    @After
    fun tearDown() {
        db.close()
    }

    private suspend fun insert(
        type: String,
        category: Long? = null,
        account: Long? = null,
        from: Long? = null,
        to: Long? = null
    ) {
        db.transactionDao().insertTransaction(
            TransactionEntity(
                type = type,
                amount = 100,
                categoryId = category,
                accountId = account,
                sourceAccountId = from,
                destinationAccountId = to,
                title = "t",
                note = null,
                date = 1
            )
        )
    }

    @Test
    fun usageCountsIncludeBothSidesOfATransfer() = runBlocking {
        assertEquals(2, accounts.usageCount(cash))
        assertEquals(1, accounts.usageCount(bank))
        assertEquals(0, accounts.usageCount(spare))
        assertEquals(1, categories.usageCount(food))
        assertEquals(0, categories.usageCount(unused))
    }

    @Test
    fun archivedAccountsLeavePickersButStayInTheManageList() = runBlocking {
        accounts.setArchived(spare, true)

        assertFalse(accounts.accounts.first().any { it.id == spare })
        assertTrue(accounts.accountBalances.first().single { it.id == spare }.isArchived)
        assertEquals(2, accounts.activeCount())

        accounts.setArchived(spare, false)
        assertTrue(accounts.accounts.first().any { it.id == spare })
    }

    @Test
    fun unusedNonDefaultItemsCanBeDeleted() = runBlocking {
        categories.deleteCategory(unused)
        accounts.deleteAccount(spare)

        assertNull(categories.getCategory(unused))
        assertNull(accounts.getAccount(spare))
    }

    @Test(expected = IllegalArgumentException::class)
    fun usedCategoryCannotBeDeleted(): Unit = runBlocking {
        categories.deleteCategory(food)
    }

    @Test(expected = IllegalArgumentException::class)
    fun usedAccountCannotBeDeleted(): Unit = runBlocking {
        accounts.deleteAccount(bank)
    }

    @Test(expected = IllegalArgumentException::class)
    fun defaultAccountCannotBeDeleted(): Unit = runBlocking {
        // Unused, so only the default rule can block it.
        val freshDefault = db.accountDao().insertAccount(
            AccountEntity(name = "Wallet", type = "CASH", isDefault = true)
        )
        accounts.deleteAccount(freshDefault)
    }
}
