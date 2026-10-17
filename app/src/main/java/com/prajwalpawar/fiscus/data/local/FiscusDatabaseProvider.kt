package com.prajwalpawar.fiscus.data.local

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.prajwalpawar.fiscus.data.local.seeder.FiscusDatabaseSeeder

object FiscusDatabaseProvider {
    @Volatile
    private var database: FiscusDatabase? = null

    private val seedCallback = object: RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            FiscusDatabaseSeeder.seed(db)
        }
    }

    fun getDatabase(
        context: Context
    ): FiscusDatabase {
        return database ?: synchronized(this) {
            database ?: Room.databaseBuilder(
                context.applicationContext,
                FiscusDatabase::class.java,
                "fiscus.db"
            )
                // A restored backup from a newer schema can't be opened; rebuild instead of crashing.
                // Note: this wipes that restored data. Revisit before release.
                .fallbackToDestructiveMigrationOnDowngrade(dropAllTables = true)
                .addCallback(seedCallback)
                .build()
                .also { database = it }
        }
    }
}