package com.prajwalpawar.fiscus.di

import com.prajwalpawar.fiscus.data.repository.FiscusRepositoryImpl
import com.prajwalpawar.fiscus.domain.repository.FiscusRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindFiscusRepository(
        fiscusRepositoryImpl: FiscusRepositoryImpl
    ): FiscusRepository
}
