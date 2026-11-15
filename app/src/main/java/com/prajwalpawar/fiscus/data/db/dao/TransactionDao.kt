package com.prajwalpawar.fiscus.data.db.dao

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Upsert
import com.prajwalpawar.fiscus.data.db.entity.TransactionEntity
import com.prajwalpawar.fiscus.model.TransactionType
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    @Query("SELECT * FROM transactions ORDER BY occurredAtMillis DESC, id DESC")
    fun observeAll(): Flow<List<TransactionEntity>>

    @Query(
        """
        SELECT * FROM transactions WHERE accountId = :accountId OR toAccountId = :accountId
        ORDER BY occurredAtMillis DESC, id DESC
        """,
    )
    fun observeByAccount(accountId: Long): Flow<List<TransactionEntity>>

    /**
     * The filtered list (ADR-022). Each `all*` flag disables its condition, since an empty set
     * means "no restriction". [pattern] is a LIKE pattern with `\` as the escape character, or
     * null for no search. Bounds are UTC epoch millis, `[fromMillis, toMillisExclusive)`.
     */
    @Query(
        """
        SELECT * FROM transactions
        WHERE (:allTypes OR type IN (:types))
          AND (:allAccounts OR accountId IN (:accountIds) OR toAccountId IN (:accountIds))
          AND (:allCategories OR categoryId IN (:categoryIds))
          AND (:fromMillis IS NULL OR occurredAtMillis >= :fromMillis)
          AND (:toMillisExclusive IS NULL OR occurredAtMillis < :toMillisExclusive)
          AND (:pattern IS NULL OR title LIKE :pattern ESCAPE '\' OR note LIKE :pattern ESCAPE '\')
        ORDER BY occurredAtMillis DESC, id DESC
        """,
    )
    fun observeFiltered(
        allTypes: Boolean,
        types: Set<TransactionType>,
        allAccounts: Boolean,
        accountIds: Set<Long>,
        allCategories: Boolean,
        categoryIds: Set<Long>,
        fromMillis: Long?,
        toMillisExclusive: Long?,
        pattern: String?,
    ): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE id = :id")
    fun observeById(id: Long): Flow<TransactionEntity?>

    @Upsert
    suspend fun upsert(transaction: TransactionEntity): Long

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteById(id: Long)
}
