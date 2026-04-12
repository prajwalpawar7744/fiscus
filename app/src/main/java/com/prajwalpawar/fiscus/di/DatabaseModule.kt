package com.prajwalpawar.fiscus.di

import android.app.Application
import androidx.room.Room
import com.prajwalpawar.fiscus.data.local.FiscusDatabase
import com.prajwalpawar.fiscus.data.local.dao.AccountDao
import com.prajwalpawar.fiscus.data.local.dao.CategoryDao
import com.prajwalpawar.fiscus.data.local.dao.TransactionDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideFiscusDatabase(app: Application): FiscusDatabase {
        return Room.databaseBuilder(
                app,
                FiscusDatabase::class.java,
                FiscusDatabase.DATABASE_NAME
            )
            .addMigrations(MIGRATION_2_3)
            .build()
    }

    private val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // 1. Add the column to transactions table
            db.execSQL("ALTER TABLE transactions ADD COLUMN toAccountId INTEGER")

            // 2. Insert new default categories for existing users
            // Bills: 0xFFFF5722 -> -43230
            db.execSQL("INSERT INTO categories (name, icon, color, type) VALUES ('Bills', 'receipt_long', -43230, 'EXPENSE')")
            
            // Transfer: 0xFF607D8B -> -10453621
            db.execSQL("INSERT INTO categories (name, icon, color, type) VALUES ('Transfer', 'swap_horiz', -10453621, 'TRANSFER')")
        }
    }

    @Provides
    @Singleton
    fun provideTransactionDao(db: FiscusDatabase): TransactionDao = db.transactionDao

    @Provides
    @Singleton
    fun provideCategoryDao(db: FiscusDatabase): CategoryDao = db.categoryDao

    @Provides
    @Singleton
    fun provideAccountDao(db: FiscusDatabase): AccountDao = db.accountDao
}
