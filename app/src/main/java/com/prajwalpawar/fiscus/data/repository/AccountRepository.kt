package com.prajwalpawar.fiscus.data.repository

import com.prajwalpawar.fiscus.model.Account
import com.prajwalpawar.fiscus.model.AccountWithBalance
import kotlinx.coroutines.flow.Flow

interface AccountRepository {
    fun observeAccounts(): Flow<List<Account>>
    fun observeAccountsWithBalance(): Flow<List<AccountWithBalance>>

    /** One account with its balance; null once it no longer exists. */
    fun observeAccountWithBalance(id: Long): Flow<AccountWithBalance?>

    /** Inserts or updates; returns the account id. */
    suspend fun save(account: Account): Long
    suspend fun setArchived(id: Long, archived: Boolean)

    /** Makes [id] the one default account; every other account stops being default (ADR-037). */
    suspend fun setDefault(id: Long)

    /** Only for accounts without transactions (ADR-036); the database rejects the rest. */
    suspend fun delete(id: Long)
}
