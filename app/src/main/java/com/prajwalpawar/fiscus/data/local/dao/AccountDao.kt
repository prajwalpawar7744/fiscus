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
    val isDefault: Boolean,
    val isArchived: Boolean,
    val balance: Long,
    val usageCount: Int
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

    // Every account (archived last) with its balance: opening balance, plus income,
    // minus expenses, plus transfers in, minus transfers out.
    // A credit card spent on goes negative, which is how it counts as debt in totals.
    @Query(
        """
        SELECT
            a.id,
            a.name,
            a.type,
            a.isDefault,
            a.isArchived,
            (
                SELECT COUNT(*)
                FROM transactions AS u
                WHERE u.accountId = a.id
                   OR u.sourceAccountId = a.id
                   OR u.destinationAccountId = a.id
            ) AS usageCount,
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
        ORDER BY a.isArchived ASC, a.isDefault DESC, a.name COLLATE NOCASE ASC
        """
    )
    fun observeAccountBalances(): Flow<List<AccountBalanceRow>>

    @Query("SELECT * FROM accounts WHERE id = :id")
    suspend fun getAccount(id: Long): AccountEntity?

    @Insert
    suspend fun insertAccount(account: AccountEntity): Long

    @Update
    suspend fun updateAccount(account: AccountEntity)

    @Query(
        """
        SELECT COUNT(*)
        FROM transactions
        WHERE accountId = :id OR sourceAccountId = :id OR destinationAccountId = :id
        """
    )
    suspend fun countUsage(id: Long): Int

    @Query("SELECT COUNT(*) FROM accounts WHERE isArchived = 0")
    suspend fun countActive(): Int

    @Query("UPDATE accounts SET isArchived = :archived WHERE id = :id")
    suspend fun setArchived(id: Long, archived: Boolean)

    // One statement, so there is never a moment with zero or two defaults.
    @Query("UPDATE accounts SET isDefault = (id = :id)")
    suspend fun setDefault(id: Long)

    @Query("DELETE FROM accounts WHERE id = :id")
    suspend fun deleteAccount(id: Long)
}