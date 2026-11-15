package com.prajwalpawar.fiscus.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.datetime.TimeZone
import kotlin.time.Clock

/** Time sources, injected so logic is testable (ADR-018). Never call `Clock.System` in logic. */
@Module
@InstallIn(SingletonComponent::class)
object TimeModule {
    @Provides
    fun provideClock(): Clock = Clock.System

    /** Not a singleton: follows the device's current time zone. */
    @Provides
    fun provideTimeZone(): TimeZone = TimeZone.currentSystemDefault()
}
