package com.prajwalpawar.fiscus.data.repository

import com.prajwalpawar.fiscus.data.local.dao.TransactionDao
import com.prajwalpawar.fiscus.data.local.entity.toEntity
import com.prajwalpawar.fiscus.data.local.entity.toTransaction
import com.prajwalpawar.fiscus.data.model.Transaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TransactionRepository (
    private val transactionDao: TransactionDao
) {
    val transactions: Flow<List<Transaction>> =
        transactionDao.observeTransactions()
            .map { transactions ->
                transactions.map { transaction ->
                    transaction.toTransaction()
                }
            }

    suspend fun getTransaction(
        id: Long
    ): Transaction? {
        return transactionDao
            .getTransaction(id)
            ?.toTransaction()
    }

    suspend fun addTransaction(
        transaction: Transaction
    ): Long {
        return transactionDao.insertTransaction(
            transaction.toEntity()
        )
    }

    suspend fun updateTransaction(
        transaction: Transaction
    ) {
        transactionDao.updateTransaction(
            transaction.toEntity()
        )
    }

    suspend fun deleteTransaction(
        transaction: Transaction
    ) {
        transactionDao.deleteTransaction(
            transaction.toEntity()
        )
    }
}