package com.prajwalpawar.fiscus.data.db.dao

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Upsert
import com.prajwalpawar.fiscus.data.db.entity.AccountEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountDao {

    @Query("SELECT * FROM accounts ORDER BY archived, name")
    fun observeAll(): Flow<List<AccountEntity>>

    @Query("SELECT * FROM accounts WHERE id = :id")
    fun observeById(id: Long): Flow<AccountEntity?>

    /**
     * Balance and transaction count (transfers in included) per account (DOMAIN.md). Balance =
     * opening + income − expense − transfers out + transfers in.
     */
    @Query(
        """
        SELECT a.id AS accountId,
            a.openingBalanceMinor
            + COALESCE((SELECT SUM(CASE WHEN t.type = 'INCOME' THEN t.amountMinor ELSE -t.amountMinor END)
                FROM transactions t WHERE t.accountId = a.id), 0)
            + COALESCE((SELECT SUM(t.amountMinor)
                FROM transactions t WHERE t.type = 'TRANSFER' AND t.toAccountId = a.id), 0)
            AS balanceMinor,
            (SELECT COUNT(*) FROM transactions t WHERE t.accountId = a.id OR t.toAccountId = a.id)
            AS transactionCount
        FROM accounts a
        """,
    )
    fun observeBalances(): Flow<List<AccountBalance>>

    @Upsert
    suspend fun upsert(account: AccountEntity): Long

    /** Archiving also clears the default, since an archived account can't be preselected. */
    @Query(
        "UPDATE accounts SET archived = :archived, " +
            "isDefault = CASE WHEN :archived THEN 0 ELSE isDefault END WHERE id = :id",
    )
    suspend fun setArchived(id: Long, archived: Boolean)

    /** Makes [id] the only default account (ADR-037), in one statement so there's never two. */
    @Query("UPDATE accounts SET isDefault = (id = :id)")
    suspend fun setDefault(id: Long)

    /** Fails (FK RESTRICT, ADR-017) if any transaction still refers to the account. */
    @Query("DELETE FROM accounts WHERE id = :id")
    suspend fun deleteById(id: Long)
}

/** Result row of [AccountDao.observeBalances]. */
data class AccountBalance(val accountId: Long, val balanceMinor: Long, val transactionCount: Int)
