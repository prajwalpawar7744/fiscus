package com.prajwalpawar.fiscus.data.repository

import com.prajwalpawar.fiscus.data.local.dao.AccountDao
import com.prajwalpawar.fiscus.data.local.entity.toAccount
import com.prajwalpawar.fiscus.data.local.entity.toEntity
import com.prajwalpawar.fiscus.data.model.Account
import com.prajwalpawar.fiscus.data.model.AccountType
import com.prajwalpawar.fiscus.data.model.AccountBalance
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AccountRepository (
    private val accountDao: AccountDao
) {
    val accounts: Flow<List<Account>> =
        accountDao.observeAccounts().map { account ->
            account.map { it.toAccount() }
        }

    val accountBalances: Flow<List<AccountBalance>> =
        accountDao.observeAccountBalances().map { rows ->
            rows.map { row ->
                AccountBalance(
                    id = row.id,
                    name = row.name,
                    type = AccountType.valueOf(row.type),
                    balance = row.balance
                )
            }
        }

    suspend fun getAccount(id: Long): Account? {
        return accountDao.getAccount(id)?.toAccount()
    }

    suspend fun addAccount(account: Account): Long {
        require(account.name.isNotBlank()) {
            "Account name cannot be empty."
        }

        return accountDao.insertAccount(account.toEntity())
    }

    suspend fun updateAccount(account: Account) {
        require(account.name.isNotBlank()) {
            "Account name cannot be empty."
        }

        accountDao.updateAccount(account.toEntity())
    }

    suspend fun archiveAccount(id: Long) {
        accountDao.archiveAccount(id)
    }
}