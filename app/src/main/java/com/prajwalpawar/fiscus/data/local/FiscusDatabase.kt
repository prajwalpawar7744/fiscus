package com.prajwalpawar.fiscus.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.prajwalpawar.fiscus.data.local.dao.AccountDao
import com.prajwalpawar.fiscus.data.local.dao.CategoryDao
import com.prajwalpawar.fiscus.data.local.dao.TransactionDao
import com.prajwalpawar.fiscus.data.local.entities.AccountEntity
import com.prajwalpawar.fiscus.data.local.entities.CategoryEntity
import com.prajwalpawar.fiscus.data.local.entities.TransactionEntity
import com.prajwalpawar.fiscus.data.local.entities.TransactionSubItemEntity

@Database(
    entities = [
        TransactionEntity::class,
        CategoryEntity::class,
        AccountEntity::class,
        TransactionSubItemEntity::class
    ],
    version = 6,
    exportSchema = false
)
abstract class FiscusDatabase : RoomDatabase() {
    abstract val transactionDao: TransactionDao
    abstract val categoryDao: CategoryDao
    abstract val accountDao: AccountDao

    companion object {
        const val DATABASE_NAME = "fiscus_db"
    }
}
