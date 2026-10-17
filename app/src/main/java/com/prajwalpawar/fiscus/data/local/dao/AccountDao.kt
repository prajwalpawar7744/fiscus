package com.prajwalpawar.fiscus.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.prajwalpawar.fiscus.data.local.entity.AccountEntity
import kotlinx.coroutines.flow.Flow

data class AccountBalanceRow(
    val id: Long,
    val name: String,
    val type: String,
    val balance: Long
)

@Dao
interface AccountDao {
    @Query(
        """
        SELECT *
        FROM accounts
        WHERE isArchived = 0
        ORDER BY isDefault DESC, name COLLATE NOCASE ASC
        """
    )
    fun observeAccounts(): Flow<List<AccountEntity>>

    // Opening balance, plus income, minus expenses, plus transfers in, minus transfers out.
    // A credit card spent on goes negative, which is how it counts as debt in totals.
    @Query(
        """
        SELECT
            a.id,
            a.name,
            a.type,
            a.openingBalance + COALESCE((
                SELECT SUM(
                    CASE
                        WHEN t.type = 'INCOME' AND t.accountId = a.id THEN t.amount
                        WHEN t.type = 'EXPENSE' AND t.accountId = a.id THEN -t.amount
                        WHEN t.type = 'TRANSFER' AND t.destinationAccountId = a.id THEN t.amount
                        WHEN t.type = 'TRANSFER' AND t.sourceAccountId = a.id THEN -t.amount
                        ELSE 0
                    END
                )
                FROM transactions AS t
                WHERE t.accountId = a.id
                   OR t.sourceAccountId = a.id
                   OR t.destinationAccountId = a.id
            ), 0) AS balance
        FROM accounts AS a
        WHERE a.isArchived = 0
        ORDER BY a.isDefault DESC, a.name COLLATE NOCASE ASC
        """
    )
    fun observeAccountBalances(): Flow<List<AccountBalanceRow>>

    @Query("SELECT * FROM accounts WHERE id = :id")
    suspend fun getAccount(id: Long): AccountEntity?

    @Insert
    suspend fun insertAccount(account: AccountEntity): Long

    @Update
    suspend fun updateAccount(account: AccountEntity)

    @Query("UPDATE accounts SET isArchived = 1 WHERE id = :id")
    suspend fun archiveAccount(id: Long)
}