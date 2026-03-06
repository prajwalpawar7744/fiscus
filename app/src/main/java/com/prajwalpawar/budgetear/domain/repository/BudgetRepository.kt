package com.prajwalpawar.budgetear.domain.repository

import com.prajwalpawar.budgetear.domain.model.Account
import com.prajwalpawar.budgetear.domain.model.Category
import com.prajwalpawar.budgetear.domain.model.Transaction
import kotlinx.coroutines.flow.Flow

interface BudgetRepository {
    // Transactions
    fun getTransactions(): Flow<List<Transaction>>
    suspend fun insertTransaction(transaction: Transaction)
    suspend fun deleteTransaction(transaction: Transaction)

    // Categories
    fun getCategories(): Flow<List<Category>>
    suspend fun insertCategory(category: Category)

    // Accounts
    fun getAccounts(): Flow<List<Account>>
    suspend fun insertAccount(account: Account)
}
