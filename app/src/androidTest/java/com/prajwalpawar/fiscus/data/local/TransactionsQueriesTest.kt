package com.prajwalpawar.fiscus.data.local

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.prajwalpawar.fiscus.data.local.entity.AccountEntity
import com.prajwalpawar.fiscus.data.local.entity.CategoryEntity
import com.prajwalpawar.fiscus.data.local.entity.TransactionEntity
import com.prajwalpawar.fiscus.data.repository.TransactionRepository
import com.prajwalpawar.fiscus.util.DateRange
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

// Checks the month list and search behind the Transactions tab.
@RunWith(AndroidJUnit4::class)
class TransactionsQueriesTest {
    private lateinit var db: FiscusDatabase
    private lateinit var repository: TransactionRepository

    @Before
    fun setUp() = runBlocking {
        db = Room.inMemoryDatabaseBuilder(
            InstrumentationRegistry.getInstrumentation().targetContext,
            FiscusDatabase::class.java
        ).build()
        repository = TransactionRepository(db, db.transactionDao())

        val cash = db.accountDao().insertAccount(AccountEntity(name = "Cash", type = "CASH"))
        val food = db.categoryDao().insertCategory(CategoryEntity(name = "Food", type = "EXPENSE"))

        listOf(
            Triple("Lunch", "with team", 100L),
            Triple("50% off shoes", null, 150L),
            Triple("Coffee_beans", null, 199L),
            Triple("Dinner", "50 percent tip", 200L)
        ).forEach { (title, note, date) ->
            db.transactionDao().insertTransaction(
                TransactionEntity(
                    type = "EXPENSE", amount = 100, categoryId = food, accountId = cash,
                    sourceAccountId = null, destinationAccountId = null,
                    title = title, note = note, date = date
                )
            )
        }
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun rangeIsStartInclusiveEndExclusiveNewestFirst() = runBlocking {
        val titles = repository.observeTransactionsInRange(DateRange(100, 200)).first().map { it.title }
        assertEquals(listOf("Coffee_beans", "50% off shoes", "Lunch"), titles)
    }

    @Test
    fun searchMatchesTitleOrNoteIgnoringCase() = runBlocking {
        assertEquals(listOf("Lunch"), repository.searchTransactions("TEAM", 10).first().map { it.title })
        assertEquals(listOf("Lunch"), repository.searchTransactions("lun", 10).first().map { it.title })
    }

    @Test
    fun wildcardsInTheQueryMatchLiterally() = runBlocking {
        // "%" must not match "50 percent".
        assertEquals(listOf("50% off shoes"), repository.searchTransactions("50%", 10).first().map { it.title })
        // "_" must not match any single character.
        assertEquals(listOf("Coffee_beans"), repository.searchTransactions("e_b", 10).first().map { it.title })
    }
}
