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
            val balanceById = balances.associate { it.accountId to it.balanceMinor }
            entities.map { entity ->
                val account = entity.asModel()
                AccountWithBalance(
                    account = account,
                    balance = Money(balanceById[account.id] ?: entity.openingBalanceMinor, account.currencyCode),
                )
            }
        }.flowOn(ioDispatcher)

    override suspend fun save(account: Account): Long = withContext(ioDispatcher) {
        accountDao.upsert(account.asEntity()).takeIf { it > 0 } ?: account.id
    }
}
