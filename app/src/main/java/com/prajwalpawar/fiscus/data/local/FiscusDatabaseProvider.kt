package com.prajwalpawar.fiscus.data.local

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.prajwalpawar.fiscus.data.local.migration.MIGRATION_1_2
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
                .addMigrations(MIGRATION_1_2)
                .addCallback(seedCallback)
                .build()
                .also { database = it }
        }
    }
}