package com.prajwalpawar.fiscus.data.local

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import com.prajwalpawar.fiscus.data.local.dao.AccountDao
import com.prajwalpawar.fiscus.data.local.dao.CategoryDao
import com.prajwalpawar.fiscus.data.local.dao.TransactionDao
import com.prajwalpawar.fiscus.data.local.dao.TransactionItemDao
import com.prajwalpawar.fiscus.data.local.entity.AccountEntity
import com.prajwalpawar.fiscus.data.local.entity.CategoryEntity
import com.prajwalpawar.fiscus.data.local.entity.TransactionEntity
import com.prajwalpawar.fiscus.data.local.entity.TransactionItemEntity

@Database(
    entities = [
        TransactionEntity::class,
        CategoryEntity::class,
        AccountEntity::class,
        TransactionItemEntity::class
    ],
    // 1 shipped in 0.1.0-beta.1. Every change bumps this and adds a migration.
    // 2: transaction_items (itemised transactions).
    version = 2,
    exportSchema = true,
    autoMigrations = [
        AutoMigration(from = 1, to = 2)
    ]
)
abstract class FiscusDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun categoryDao(): CategoryDao
    abstract fun accountDao(): AccountDao
    abstract fun transactionItemDao(): TransactionItemDao
}