package com.prajwalpawar.fiscus.data.local.backup

import android.content.Context
import com.prajwalpawar.fiscus.domain.model.Account
import com.prajwalpawar.fiscus.domain.model.Transaction
import com.prajwalpawar.fiscus.domain.model.Category
import com.prajwalpawar.fiscus.domain.repository.FiscusRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
data class BackupData(
    val transactions: List<Transaction>,
    val categories: List<Category>,
    val accounts: List<Account> = emptyList()
)

@Singleton
class BackupManager @Inject constructor(
    private val repository: FiscusRepository,
    @ApplicationContext private val context: Context
) {
    private val json = Json { 
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    suspend fun exportData(): String? {
        return try {
            val transactions = repository.getTransactions().first()
            val categories = repository.getCategories().first()
            val accounts = repository.getAccounts().first()
            val backupData = BackupData(transactions, categories, accounts)
            json.encodeToString(backupData)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun importData(jsonData: String): Boolean {
        return try {
            val backupData = json.decodeFromString<BackupData>(jsonData)
            
            // Clear existing data
            repository.clearAllData()

            // Re-insert accounts first (as they might be referenced)
            backupData.accounts.forEach { account ->
                repository.insertAccount(account)
            }

            // Re-insert categories
            backupData.categories.forEach { category ->
                repository.insertCategory(category)
            }

            // Re-insert transactions
            backupData.transactions.forEach { transaction ->
                repository.insertTransaction(transaction)
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
