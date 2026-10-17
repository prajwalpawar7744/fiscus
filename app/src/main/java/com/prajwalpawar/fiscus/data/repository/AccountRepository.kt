package com.prajwalpawar.fiscus.data.repository

import com.prajwalpawar.fiscus.data.local.dao.AccountDao
import com.prajwalpawar.fiscus.data.local.entity.toAccount
import com.prajwalpawar.fiscus.data.local.entity.toEntity
import com.prajwalpawar.fiscus.data.model.Account
import com.prajwalpawar.fiscus.data.model.AccountBalance
import com.prajwalpawar.fiscus.data.model.AccountType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AccountRepository(
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
                    isDefault = row.isDefault,
                    isArchived = row.isArchived,
                    balance = row.balance,
                    usageCount = row.usageCount
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

    suspend fun usageCount(id: Long): Int {
        return accountDao.countUsage(id)
    }

    suspend fun activeCount(): Int {
        return accountDao.countActive()
    }

    suspend fun setArchived(id: Long, archived: Boolean) {
        accountDao.setArchived(id, archived)
    }

    // Only unused, non-default accounts can be deleted; anything else is archived instead.
    suspend fun deleteAccount(id: Long) {
        val account = requireNotNull(accountDao.getAccount(id)) {
            "Account $id doesn't exist."
        }
        require(!account.isDefault) {
            "The default account can't be deleted."
        }
        require(accountDao.countUsage(id) == 0) {
            "An account used by transactions can't be deleted."
        }

        accountDao.deleteAccount(id)
    }
}