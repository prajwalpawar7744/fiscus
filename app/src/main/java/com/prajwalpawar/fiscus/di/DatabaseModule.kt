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
            ).fallbackToDestructiveMigration(false)
            .build()
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
