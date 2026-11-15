package com.prajwalpawar.fiscus.di

import android.content.Context
import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.prajwalpawar.fiscus.core.coroutines.IoDispatcher
import com.prajwalpawar.fiscus.R
import com.prajwalpawar.fiscus.data.db.FiscusDatabase
import com.prajwalpawar.fiscus.data.db.Migration1To2
import com.prajwalpawar.fiscus.data.db.SeedCallback
import com.prajwalpawar.fiscus.data.db.dao.AccountDao
import com.prajwalpawar.fiscus.data.db.dao.CategoryDao
import com.prajwalpawar.fiscus.data.db.dao.TransactionDao
import com.prajwalpawar.fiscus.model.TransactionType
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
        @IoDispatcher ioDispatcher: CoroutineDispatcher,
    ): FiscusDatabase = Room.databaseBuilder(context, FiscusDatabase::class.java, FiscusDatabase.NAME)
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(ioDispatcher)
        .addCallback(SeedCallback(context))
        .addMigrations(
            Migration1To2(
                typeLabels = mapOf(
                    TransactionType.EXPENSE to context.getString(R.string.transaction_type_expense),
                    TransactionType.INCOME to context.getString(R.string.transaction_type_income),
                    TransactionType.TRANSFER to context.getString(R.string.transaction_type_transfer),
                ),
            ),
        )
        .build()

    @Provides
    fun provideAccountDao(database: FiscusDatabase): AccountDao = database.accountDao()

    @Provides
    fun provideCategoryDao(database: FiscusDatabase): CategoryDao = database.categoryDao()

    @Provides
    fun provideTransactionDao(database: FiscusDatabase): TransactionDao = database.transactionDao()
}
