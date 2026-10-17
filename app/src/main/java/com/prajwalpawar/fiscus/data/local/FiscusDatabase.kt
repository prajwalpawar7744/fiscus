package com.prajwalpawar.fiscus.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.prajwalpawar.fiscus.data.local.dao.AccountDao
import com.prajwalpawar.fiscus.data.local.dao.CategoryDao
import com.prajwalpawar.fiscus.data.local.dao.TransactionDao
import com.prajwalpawar.fiscus.data.local.entity.AccountEntity
import com.prajwalpawar.fiscus.data.local.entity.CategoryEntity
import com.prajwalpawar.fiscus.data.local.entity.TransactionEntity

@Database(
    entities = [
        TransactionEntity::class,
        CategoryEntity::class,
        AccountEntity::class
               ],
    // Reset to 1 before release: the old v1 schema never reached users.
    version = 1,
    exportSchema = true
)
abstract class FiscusDatabase: RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun categoryDao(): CategoryDao
    abstract fun accountDao(): AccountDao
}