package com.prajwalpawar.budgetear.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.prajwalpawar.budgetear.data.local.dao.AccountDao
import com.prajwalpawar.budgetear.data.local.dao.CategoryDao
import com.prajwalpawar.budgetear.data.local.dao.TransactionDao
import com.prajwalpawar.budgetear.data.local.entities.AccountEntity
import com.prajwalpawar.budgetear.data.local.entities.CategoryEntity
import com.prajwalpawar.budgetear.data.local.entities.TransactionEntity

@Database(
    entities = [TransactionEntity::class, CategoryEntity::class, AccountEntity::class],
    version = 1,
    exportSchema = false
)
abstract class BudgetDatabase : RoomDatabase() {
    abstract val transactionDao: TransactionDao
    abstract val categoryDao: CategoryDao
    abstract val accountDao: AccountDao

    companion object {
        const val DATABASE_NAME = "budget_db"
    }
}
