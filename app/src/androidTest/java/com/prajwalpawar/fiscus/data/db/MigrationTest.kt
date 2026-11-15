package com.prajwalpawar.fiscus.data.db

import androidx.room3.testing.MigrationTestHelper
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.sqlite.execSQL
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.prajwalpawar.fiscus.model.TransactionType
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Every schema version change ships a migration and a test of it (ADR-017). */
@RunWith(AndroidJUnit4::class)
class MigrationTest {

    private val instrumentation = InstrumentationRegistry.getInstrumentation()

    @get:Rule
    val helper = MigrationTestHelper(
        instrumentation = instrumentation,
        file = instrumentation.targetContext.getDatabasePath("migration-test.db"),
        driver = BundledSQLiteDriver(),
        databaseClass = FiscusDatabase::class,
    )

    private val typeLabels = mapOf(
        TransactionType.EXPENSE to "Expense",
        TransactionType.INCOME to "Income",
        TransactionType.TRANSFER to "Transfer",
    )

    @Test
    fun migrate1To2_givesEveryTransactionATitle() = runTest {
        helper.createDatabase(1).use { connection ->
            connection.execSQL(
                "INSERT INTO accounts (id, name, type, currencyCode, openingBalanceMinor, archived) " +
                    "VALUES (1, 'Cash', 'CASH', 'INR', 0, 0), (2, 'Bank', 'BANK', 'INR', 0, 0)",
            )
            connection.execSQL(
                "INSERT INTO categories (id, name, kind, iconKey, archived) " +
                    "VALUES (1, 'Food', 'EXPENSE', 'restaurant', 0)",
            )
            connection.execSQL(
                "INSERT INTO transactions (id, type, amountMinor, currencyCode, accountId, toAccountId, " +
                    "categoryId, note, occurredAtMillis, createdAtMillis, updatedAtMillis) VALUES " +
                    // Category name wins.
                    "(1, 'EXPENSE', 100, 'INR', 1, NULL, 1, 'ignored note', 1, 1, 1), " +
                    // No category (deleted → SET NULL): the note's text.
                    "(2, 'EXPENSE', 100, 'INR', 1, NULL, NULL, '  Taxi home ', 1, 1, 1), " +
                    // Transfer without a note: the type label.
                    "(3, 'TRANSFER', 100, 'INR', 1, 2, NULL, '', 1, 1, 1), " +
                    // Blank note: the type label.
                    "(4, 'INCOME', 100, 'INR', 1, NULL, NULL, '   ', 1, 1, 1)",
            )
        }

        helper.runMigrationsAndValidate(2, listOf(Migration1To2(typeLabels))).use { connection ->
            val titles = connection.prepare("SELECT title FROM transactions ORDER BY id").use { statement ->
                buildList { while (statement.step()) add(statement.getText(0)) }
            }
            assertEquals(listOf("Food", "Taxi home", "Transfer", "Income"), titles)
        }
    }
}
