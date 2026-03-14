package com.prajwalpawar.budgetear.di

import android.app.Application
import androidx.room.Room
import com.prajwalpawar.budgetear.data.local.BudgetDatabase
import com.prajwalpawar.budgetear.data.local.dao.AccountDao
import com.prajwalpawar.budgetear.data.local.dao.CategoryDao
import com.prajwalpawar.budgetear.data.local.dao.TransactionDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideBudgetDatabase(app: Application): BudgetDatabase {
        return Room.databaseBuilder(
                app,
                BudgetDatabase::class.java,
                BudgetDatabase.DATABASE_NAME
            ).fallbackToDestructiveMigration(false)
            .build()
    }

    @Provides
    @Singleton
    fun provideTransactionDao(db: BudgetDatabase): TransactionDao = db.transactionDao

    @Provides
    @Singleton
    fun provideCategoryDao(db: BudgetDatabase): CategoryDao = db.categoryDao

    @Provides
    @Singleton
    fun provideAccountDao(db: BudgetDatabase): AccountDao = db.accountDao
}
