package com.prajwalpawar.fiscus.data.local

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.prajwalpawar.fiscus.data.local.seeder.FiscusDatabaseSeeder
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

// A database that was never "created" by Room (e.g. restored from a backup) still gets
// defaults, but only into empty tables.
@RunWith(AndroidJUnit4::class)
class SeederTest {
    private lateinit var db: FiscusDatabase

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            InstrumentationRegistry.getInstrumentation().targetContext,
            FiscusDatabase::class.java
        ).build()
    }

    @After
    fun tearDown() {
        db.close()
    }

    private fun count(table: String): Int =
        db.openHelper.writableDatabase.query("SELECT COUNT(*) FROM $table").use {
            it.moveToFirst(); it.getInt(0)
        }

    @Test
    fun emptyTablesGetTheDefaults() {
        FiscusDatabaseSeeder.seedEmptyTables(db.openHelper.writableDatabase)

        assertEquals(14, count("categories"))
        assertEquals(1, count("accounts"))
    }

    @Test
    fun tablesWithRowsAreLeftAlone() {
        val sql = db.openHelper.writableDatabase
        sql.execSQL("INSERT INTO categories (name, type, iconKey, isDefault, isArchived) VALUES ('Groceries', 'EXPENSE', 'category', 0, 1)")
        sql.execSQL("INSERT INTO accounts (name, type, openingBalance, isDefault, isArchived) VALUES ('Bank', 'BANK', 0, 0, 0)")

        FiscusDatabaseSeeder.seedEmptyTables(sql)

        // Even an archived-only table counts as non-empty: nothing is re-added.
        assertEquals(1, count("categories"))
        assertEquals(1, count("accounts"))
    }
}
