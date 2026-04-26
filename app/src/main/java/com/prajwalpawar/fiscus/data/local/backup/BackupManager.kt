package com.prajwalpawar.fiscus.data.local.backup

import android.content.Context
import androidx.sqlite.db.SimpleSQLiteQuery
import com.prajwalpawar.fiscus.data.local.FiscusDatabase
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupManager @Inject constructor(
    private val database: FiscusDatabase,
    @param:ApplicationContext private val context: Context
) {
    suspend fun exportDatabase(outputStream: OutputStream): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                // Force checkpoint the WAL so all data is pushed to the main file
                database.query(SimpleSQLiteQuery("pragma wal_checkpoint(full)")).use { it.moveToFirst() }
                
                val dbFile = context.getDatabasePath(FiscusDatabase.DATABASE_NAME)
                dbFile.inputStream().use { input ->
                    input.copyTo(outputStream)
                }
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }

    suspend fun importDatabase(inputStream: InputStream): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                // Close the active database connection
                database.close()
                
                val dbFile = context.getDatabasePath(FiscusDatabase.DATABASE_NAME)
                val walFile = context.getDatabasePath("${FiscusDatabase.DATABASE_NAME}-wal")
                val shmFile = context.getDatabasePath("${FiscusDatabase.DATABASE_NAME}-shm")
                
                // Delete existing WAL and SHM files to prevent corruption with the incoming DB file
                if (walFile.exists()) walFile.delete()
                if (shmFile.exists()) shmFile.delete()
                
                // Overwrite the main DB file
                dbFile.outputStream().use { output ->
                    inputStream.copyTo(output)
                }
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }
}
