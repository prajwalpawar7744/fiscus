package com.prajwalpawar.fiscus.data.local

import android.content.Context
import androidx.room.Room

object FiscusDatabaseProvider {
    @Volatile
    private var database: FiscusDatabase? = null

    fun getDatabase(
        context: Context
    ): FiscusDatabase {
        return database ?: synchronized(this) {
            database ?: Room.databaseBuilder(
                context.applicationContext,
                FiscusDatabase::class.java,
                "fiscus.db"
            ).build().also {
                database = it
            }
        }
    }
}