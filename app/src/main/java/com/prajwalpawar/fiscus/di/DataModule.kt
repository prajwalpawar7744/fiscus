package com.prajwalpawar.fiscus.di

import com.prajwalpawar.fiscus.data.repository.AccountRepository
import com.prajwalpawar.fiscus.data.repository.CategoryRepository
import com.prajwalpawar.fiscus.data.repository.OfflineFirstAccountRepository
import com.prajwalpawar.fiscus.data.repository.OfflineFirstCategoryRepository
import com.prajwalpawar.fiscus.data.repository.OfflineFirstTransactionRepository
import com.prajwalpawar.fiscus.data.repository.TransactionRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface DataModule {
    @Binds
    fun bindAccountRepository(impl: OfflineFirstAccountRepository): AccountRepository

    @Binds
    fun bindCategoryRepository(impl: OfflineFirstCategoryRepository): CategoryRepository

    @Binds
    fun bindTransactionRepository(impl: OfflineFirstTransactionRepository): TransactionRepository
}
