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
     * Balance per account (DOMAIN.md): opening + income − expense − transfers out + transfers in.
     */
    @Query(
        """
        SELECT a.id AS accountId,
            a.openingBalanceMinor
            + COALESCE((SELECT SUM(CASE WHEN t.type = 'INCOME' THEN t.amountMinor ELSE -t.amountMinor END)
                FROM transactions t WHERE t.accountId = a.id), 0)
            + COALESCE((SELECT SUM(t.amountMinor)
                FROM transactions t WHERE t.type = 'TRANSFER' AND t.toAccountId = a.id), 0)
            AS balanceMinor
        FROM accounts a
        """,
    )
    fun observeBalances(): Flow<List<AccountBalance>>

    @Upsert
    suspend fun upsert(account: AccountEntity): Long
}

/** Result row of [AccountDao.observeBalances]. */
data class AccountBalance(val accountId: Long, val balanceMinor: Long)
