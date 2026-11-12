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
 */
@Database(
    entities = [AccountEntity::class, CategoryEntity::class, TransactionEntity::class],
    version = 1,
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
