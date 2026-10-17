package com.prajwalpawar.fiscus.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.prajwalpawar.fiscus.data.local.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

// A transaction joined with the names needed to display it.
private const val DETAILS_SELECT = """
    SELECT
        t.id,
        t.type,
        t.amount,
        t.title,
        t.note,
        t.date,
        c.name AS categoryName,
        c.iconKey AS categoryIconKey,
        a.name AS accountName,
        source.name AS sourceAccountName,
        destination.name AS destinationAccountName
    FROM transactions AS t
    LEFT JOIN categories AS c ON c.id = t.categoryId
    LEFT JOIN accounts AS a ON a.id = t.accountId
    LEFT JOIN accounts AS source ON source.id = t.sourceAccountId
    LEFT JOIN accounts AS destination ON destination.id = t.destinationAccountId
"""

data class TransactionDetailsRow(
    val id: Long,
    val type: String,
    val amount: Long,
    val title: String,
    val note: String?,
    val date: Long,
    val categoryName: String?,
    val categoryIconKey: String?,
    val accountName: String?,
    val sourceAccountName: String?,
    val destinationAccountName: String?
)

data class PeriodTotalsRow(
    val income: Long,
    val expense: Long
)

@Dao
interface TransactionDao {
    @Query( """
        SELECT *
        FROM transactions
        ORDER BY date DESC, id DESC
        """
    )
    fun observeTransactions(): Flow<List<TransactionEntity>>

    @Query(
        """
        SELECT *
        FROM transactions
        WHERE id = :id
        """
    )
    suspend fun getTransaction(id: Long): TransactionEntity?

    // Newest first, with the names needed to show each row.
    @Query("$DETAILS_SELECT ORDER BY t.date DESC, t.id DESC LIMIT :limit")
    fun observeRecentTransactions(limit: Int): Flow<List<TransactionDetailsRow>>

    // Emits null once the transaction is deleted.
    @Query("$DETAILS_SELECT WHERE t.id = :id")
    fun observeTransactionDetails(id: Long): Flow<TransactionDetailsRow?>

    // Income and expense in [startMillis, endMillis); transfers move money, so they're left out.
    @Query(
        """
        SELECT
            COALESCE(SUM(CASE WHEN type = 'INCOME' THEN amount END), 0) AS income,
            COALESCE(SUM(CASE WHEN type = 'EXPENSE' THEN amount END), 0) AS expense
        FROM transactions
        WHERE date >= :startMillis AND date < :endMillis
        """
    )
    fun observePeriodTotals(startMillis: Long, endMillis: Long): Flow<PeriodTotalsRow>

    @Insert
    suspend fun insertTransaction(transaction: TransactionEntity): Long

    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)

    @Delete
    suspend fun deleteTransaction(transaction: TransactionEntity)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteTransaction(id: Long)
}