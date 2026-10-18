package com.prajwalpawar.fiscus.data.local

import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MigrationTest {
    private val dbName = "migration-test"

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        FiscusDatabase::class.java
    )

    // 0.1.0-beta.1 data must survive the upgrade, and the new items table must work.
    @Test
    fun migrate1To2_keepsTransactionsAndAddsItems() {
        helper.createDatabase(dbName, 1).apply {
            execSQL("INSERT INTO accounts (id, name, type, openingBalance, isDefault, isArchived) VALUES (1, 'Cash', 'CASH', 0, 1, 0)")
            execSQL("INSERT INTO transactions (id, type, amount, categoryId, accountId, sourceAccountId, destinationAccountId, title, note, date) VALUES (1, 'EXPENSE', 16000, NULL, 1, NULL, NULL, 'Groceries', NULL, 0)")
            close()
        }

        val db = helper.runMigrationsAndValidate(dbName, 2, true)

        db.query("SELECT title, amount FROM transactions WHERE id = 1").use { cursor ->
            cursor.moveToFirst()
            assertEquals("Groceries", cursor.getString(0))
            assertEquals(16000L, cursor.getLong(1))
        }

        db.execSQL("INSERT INTO transaction_items (transactionId, position, name, quantity, unitPrice) VALUES (1, 0, 'Milk', 200, 8000)")
        db.query("SELECT COUNT(*) FROM transaction_items WHERE transactionId = 1").use { cursor ->
            cursor.moveToFirst()
            assertEquals(1, cursor.getInt(0))
        }
    }
}
