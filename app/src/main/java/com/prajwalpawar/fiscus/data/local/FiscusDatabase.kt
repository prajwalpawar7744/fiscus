package com.prajwalpawar.fiscus.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.prajwalpawar.fiscus.data.local.dao.TransactionDao
import com.prajwalpawar.fiscus.data.local.entity.TransactionEntity

@Database(
    entities = [TransactionEntity::class],
    version = 1,
    exportSchema = true
)
abstract class FiscusDatabase: RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
}