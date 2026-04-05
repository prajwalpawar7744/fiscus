package com.prajwalpawar.fiscus.domain.repository

import com.prajwalpawar.fiscus.domain.model.Account
import com.prajwalpawar.fiscus.domain.model.Category
import com.prajwalpawar.fiscus.domain.model.Transaction
import kotlinx.coroutines.flow.Flow

interface FiscusRepository {
    // Transactions
    fun getTransactions(): Flow<List<Transaction>>
    fun getRecentTransactions(limit: Int): Flow<List<Transaction>>
    fun getTotalAmountByType(type: String): Flow<Double>
    suspend fun insertTransaction(transaction: Transaction)
    suspend fun updateTransaction(transaction: Transaction)
    suspend fun deleteTransaction(transaction: Transaction)

    // Categories
    fun getCategories(): Flow<List<Category>>
    suspend fun insertCategory(category: Category)

    // Accounts
    fun getAccounts(): Flow<List<Account>>
    suspend fun insertAccount(account: Account)

    suspend fun clearAllData()
}
