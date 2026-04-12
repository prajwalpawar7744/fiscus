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
            .addMigrations(MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
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

    private val MIGRATION_3_4 = object : Migration(3, 4) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // Add isSystem column to categories
            db.execSQL("ALTER TABLE categories ADD COLUMN isSystem INTEGER NOT NULL DEFAULT 0")
            
            // Clean up duplicates before adding unique index
            // We keep the one with the smallest ID for each (name, type) pair
            db.execSQL("""
                DELETE FROM categories 
                WHERE id NOT IN (
                    SELECT MIN(id) 
                    FROM categories 
                    GROUP BY name, IFNULL(type, 'NULL')
                )
            """.trimIndent())

            // Mark existing categories as system categories
            db.execSQL("UPDATE categories SET isSystem = 1 WHERE name IN ('Food', 'Travel', 'Education', 'Shopping', 'Health', 'Bills', 'Entertainment', 'Salary', 'Freelance', 'Gift', 'Investment', 'Other', 'Transfer')")

            // Create unique index on name and type to prevent future duplicates
            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_categories_name_type ON categories(name, type)")
        }
    }

    private val MIGRATION_4_5 = object : Migration(4, 5) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // Re-enforce isSystem flag for all default categories to ensure they can't be deleted
            db.execSQL("UPDATE categories SET isSystem = 1 WHERE name IN ('Food', 'Travel', 'Education', 'Shopping', 'Health', 'Bills', 'Entertainment', 'Salary', 'Freelance', 'Gift', 'Investment', 'Other', 'Transfer')")
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
