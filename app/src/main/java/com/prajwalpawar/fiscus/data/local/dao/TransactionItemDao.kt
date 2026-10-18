package com.prajwalpawar.fiscus.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.prajwalpawar.fiscus.data.local.entity.TransactionItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionItemDao {
    @Query("SELECT * FROM transaction_items WHERE transactionId = :transactionId ORDER BY position")
    fun observeItems(transactionId: Long): Flow<List<TransactionItemEntity>>

    @Query("SELECT * FROM transaction_items WHERE transactionId = :transactionId ORDER BY position")
    suspend fun getItems(transactionId: Long): List<TransactionItemEntity>

    @Insert
    suspend fun insertItems(items: List<TransactionItemEntity>)

    @Query("DELETE FROM transaction_items WHERE transactionId = :transactionId")
    suspend fun deleteItems(transactionId: Long)
}
