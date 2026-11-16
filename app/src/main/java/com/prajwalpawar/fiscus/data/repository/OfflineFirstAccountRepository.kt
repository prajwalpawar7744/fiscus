package com.prajwalpawar.fiscus.data.repository

import com.prajwalpawar.fiscus.core.coroutines.IoDispatcher
import com.prajwalpawar.fiscus.data.db.dao.AccountDao
import com.prajwalpawar.fiscus.data.db.mapper.asEntity
import com.prajwalpawar.fiscus.data.db.mapper.asModel
import com.prajwalpawar.fiscus.model.Account
import com.prajwalpawar.fiscus.model.AccountWithBalance
import com.prajwalpawar.fiscus.model.Money
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

class OfflineFirstAccountRepository @Inject constructor(
    private val accountDao: AccountDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : AccountRepository {

    override fun observeAccounts(): Flow<List<Account>> =
        accountDao.observeAll().map { entities -> entities.map { it.asModel() } }.flowOn(ioDispatcher)

    override fun observeAccountsWithBalance(): Flow<List<AccountWithBalance>> =
        combine(accountDao.observeAll(), accountDao.observeBalances()) { entities, balances ->
            val balanceById = balances.associateBy { it.accountId }
            entities.map { entity ->
                val account = entity.asModel()
                val balance = balanceById[account.id]
                AccountWithBalance(
                    account = account,
                    balance = Money(balance?.balanceMinor ?: entity.openingBalanceMinor, account.currencyCode),
                    transactionCount = balance?.transactionCount ?: 0,
                )
            }
        }.flowOn(ioDispatcher)

    override fun observeAccountWithBalance(id: Long): Flow<AccountWithBalance?> =
        observeAccountsWithBalance().map { list -> list.find { it.account.id == id } }

    override suspend fun setArchived(id: Long, archived: Boolean) = withContext(ioDispatcher) {
        accountDao.setArchived(id, archived)
    }

    override suspend fun setDefault(id: Long) = withContext(ioDispatcher) {
        accountDao.setDefault(id)
    }

    override suspend fun delete(id: Long) = withContext(ioDispatcher) {
        accountDao.deleteById(id)
    }

    /** A saved default account becomes the only default (ADR-037). */
    override suspend fun save(account: Account): Long = withContext(ioDispatcher) {
        val id = accountDao.upsert(account.asEntity()).takeIf { it > 0 } ?: account.id
        if (account.isDefault) accountDao.setDefault(id)
        id
    }
}
