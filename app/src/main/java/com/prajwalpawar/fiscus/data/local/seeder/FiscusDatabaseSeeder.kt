package com.prajwalpawar.fiscus.data.local.seeder

import androidx.sqlite.db.SupportSQLiteDatabase

object FiscusDatabaseSeeder {
    fun seed(db: SupportSQLiteDatabase) {
        seedDefaultCategories(db)
        seedDefaultAccount(db)
    }

    private fun seedDefaultCategories(
        db: SupportSQLiteDatabase
    ) {
        val defaultCategories = listOf(
            Triple("Food", "EXPENSE", "restaurant"),
            Triple("Transport", "EXPENSE", "directions_car"),
            Triple("Shopping", "EXPENSE", "shopping_bag"),
            Triple("Bills", "EXPENSE", "receipt"),
            Triple("Health", "EXPENSE", "health"),
            Triple("Education", "EXPENSE", "school"),
            Triple("Entertainment", "EXPENSE", "movie"),
            Triple("Home", "EXPENSE", "home"),
            Triple("Other", "EXPENSE", "category"),

            Triple("Salary", "INCOME", "payments"),
            Triple("Business", "INCOME", "business"),
            Triple("Investment", "INCOME", "trending_up"),
            Triple("Gift", "INCOME", "card_giftcard"),
            Triple("Other", "INCOME", "category")
        )

        defaultCategories.forEach { (name, type, iconKey) ->
            db.execSQL(
                """
            INSERT OR IGNORE INTO categories (
                name, type, iconKey, isDefault, isArchived
            )
            VALUES (?, ?, ?, 1, 0)
            """.trimIndent(),
                arrayOf(name, type, iconKey)
            )
        }
    }

    private fun seedDefaultAccount(
        db: SupportSQLiteDatabase
    ) {
        db.execSQL(
            """
        INSERT OR IGNORE INTO accounts (
            name,
            type,
            openingBalance,
            isDefault,
            isArchived
        )
        SELECT 'Cash', 'CASH', 0, 1, 0
        WHERE NOT EXISTS (
            SELECT 1 FROM accounts WHERE isDefault = 1
        )
        """.trimIndent()
        )
    }
}