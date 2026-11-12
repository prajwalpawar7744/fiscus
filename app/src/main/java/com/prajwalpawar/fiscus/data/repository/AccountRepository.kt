package com.prajwalpawar.fiscus.data.repository

import com.prajwalpawar.fiscus.model.Account
import com.prajwalpawar.fiscus.model.AccountWithBalance
import kotlinx.coroutines.flow.Flow

interface AccountRepository {
    fun observeAccounts(): Flow<List<Account>>
    fun observeAccountsWithBalance(): Flow<List<AccountWithBalance>>

    /** Inserts or updates; returns the account id. */
    suspend fun save(account: Account): Long
}
