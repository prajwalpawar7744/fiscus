package com.prajwalpawar.fiscus.data.db

import androidx.room3.Database
import androidx.room3.RoomDatabase
import com.prajwalpawar.fiscus.data.db.dao.AccountDao
import com.prajwalpawar.fiscus.data.db.dao.CategoryDao
import com.prajwalpawar.fiscus.data.db.dao.TransactionDao
import com.prajwalpawar.fiscus.data.db.entity.AccountEntity
import com.prajwalpawar.fiscus.data.db.entity.CategoryEntity
import com.prajwalpawar.fiscus.data.db.entity.TransactionEntity

/**
 * App database (Room 3). Schemas are exported to `app/schemas/`; every version change needs a
 * migration, never destructive fallback (ADR-017).
 *
 * - v1: accounts, categories, transactions (ADR-016).
 * - v2: required transaction `title` ([Migration1To2], ADR-021).
 * - v3: `accounts.isDefault`, one default account ([Migration2To3], ADR-037).
 * - v4: `categories.isSeeded`, starter vs user-added ([Migration3To4], ADR-041).
 */
@Database(
    entities = [AccountEntity::class, CategoryEntity::class, TransactionEntity::class],
    version = 4,
    exportSchema = true,
)
abstract class FiscusDatabase : RoomDatabase() {
    abstract fun accountDao(): AccountDao
    abstract fun categoryDao(): CategoryDao
    abstract fun transactionDao(): TransactionDao

    companion object {
        const val NAME = "fiscus.db"
    }
}
