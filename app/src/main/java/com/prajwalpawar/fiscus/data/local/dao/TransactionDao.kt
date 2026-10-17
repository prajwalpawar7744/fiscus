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
        t.categoryId,
        t.accountId,
        t.sourceAccountId,
        t.destinationAccountId,
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
    val categoryId: Long?,
    val accountId: Long?,
    val sourceAccountId: Long?,
    val destinationAccountId: Long?,
    val categoryName: String?,
    val categoryIconKey: String?,
    val accountName: String?,
    val sourceAccountName: String?,
    val destinationAccountName: String?
)

data class CategoryTotalRow(
    val categoryId: Long?,
    val categoryName: String?,
    val iconKey: String?,
    val total: Long
)

data class MonthTotalsRow(
    // "2026-10"
    val month: String,
    val income: Long,
    val expense: Long
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

    // Everything dated in [startMillis, endMillis), newest first.
    @Query("$DETAILS_SELECT WHERE t.date >= :startMillis AND t.date < :endMillis ORDER BY t.date DESC, t.id DESC")
    fun observeTransactionsInRange(startMillis: Long, endMillis: Long): Flow<List<TransactionDetailsRow>>

    // Title or note contains [pattern] (already LIKE-escaped with '\'), across all dates.
    @Query(
        """
        $DETAILS_SELECT
        WHERE t.title LIKE '%' || :pattern || '%' ESCAPE '\'
           OR t.note LIKE '%' || :pattern || '%' ESCAPE '\'
        ORDER BY t.date DESC, t.id DESC
        LIMIT :limit
        """
    )
    fun searchTransactions(pattern: String, limit: Int): Flow<List<TransactionDetailsRow>>

    // Totals per category for one type in [startMillis, endMillis), largest first.
    @Query(
        """
        SELECT
            t.categoryId AS categoryId,
            c.name AS categoryName,
            c.iconKey AS iconKey,
            SUM(t.amount) AS total
        FROM transactions AS t
        LEFT JOIN categories AS c ON c.id = t.categoryId
        WHERE t.type = :type AND t.date >= :startMillis AND t.date < :endMillis
        GROUP BY t.categoryId
        ORDER BY total DESC
        """
    )
    fun observeCategoryTotals(type: String, startMillis: Long, endMillis: Long): Flow<List<CategoryTotalRow>>

    // Income and expense per calendar month. Dates are UTC midnights, so grouping in
    // UTC gives each transaction its local calendar month. Months with nothing are absent.
    @Query(
        """
        SELECT
            strftime('%Y-%m', date / 1000, 'unixepoch') AS month,
            COALESCE(SUM(CASE WHEN type = 'INCOME' THEN amount END), 0) AS income,
            COALESCE(SUM(CASE WHEN type = 'EXPENSE' THEN amount END), 0) AS expense
        FROM transactions
        WHERE date >= :startMillis AND date < :endMillis
        GROUP BY month
        ORDER BY month
        """
    )
    fun observeMonthlyTotals(startMillis: Long, endMillis: Long): Flow<List<MonthTotalsRow>>

    // The largest expenses in [startMillis, endMillis).
    @Query("$DETAILS_SELECT WHERE t.type = 'EXPENSE' AND t.date >= :startMillis AND t.date < :endMillis ORDER BY t.amount DESC, t.date DESC LIMIT :limit")
    fun observeLargestExpenses(startMillis: Long, endMillis: Long, limit: Int): Flow<List<TransactionDetailsRow>>

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