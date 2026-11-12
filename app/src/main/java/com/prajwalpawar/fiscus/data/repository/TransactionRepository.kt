package com.prajwalpawar.fiscus.data.repository

import com.prajwalpawar.fiscus.model.Transaction
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {
    /** All transactions, newest first. */
    fun observeTransactions(): Flow<List<Transaction>>

    /** Transactions touching [accountId] (including transfers in), newest first. */
    fun observeTransactions(accountId: Long): Flow<List<Transaction>>
    fun observeTransaction(id: Long): Flow<Transaction?>

    /** Inserts or updates; returns the transaction id. */
    suspend fun save(transaction: Transaction): Long
    suspend fun delete(id: Long)
}
