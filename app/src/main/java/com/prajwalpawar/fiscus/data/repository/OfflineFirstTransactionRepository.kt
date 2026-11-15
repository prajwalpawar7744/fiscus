package com.prajwalpawar.fiscus.data.repository

import com.prajwalpawar.fiscus.core.coroutines.IoDispatcher
import com.prajwalpawar.fiscus.data.db.dao.TransactionDao
import com.prajwalpawar.fiscus.data.db.mapper.asEntity
import com.prajwalpawar.fiscus.data.db.mapper.asModel
import com.prajwalpawar.fiscus.model.Transaction
import com.prajwalpawar.fiscus.model.TransactionFilter
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

class OfflineFirstTransactionRepository @Inject constructor(
    private val transactionDao: TransactionDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : TransactionRepository {

    override fun observeTransactions(): Flow<List<Transaction>> =
        transactionDao.observeAll().map { entities -> entities.map { it.asModel() } }.flowOn(ioDispatcher)

    override fun observeTransactions(accountId: Long): Flow<List<Transaction>> =
        transactionDao.observeByAccount(accountId).map { entities -> entities.map { it.asModel() } }
            .flowOn(ioDispatcher)

    override fun observeTransactions(filter: TransactionFilter): Flow<List<Transaction>> =
        transactionDao.observeFiltered(
            allTypes = filter.types.isEmpty(),
            types = filter.types,
            allAccounts = filter.accountIds.isEmpty(),
            accountIds = filter.accountIds,
            allCategories = filter.categoryIds.isEmpty(),
            categoryIds = filter.categoryIds,
            fromMillis = filter.from?.toEpochMilliseconds(),
            toMillisExclusive = filter.toExclusive?.toEpochMilliseconds(),
            pattern = containsLikePattern(filter.query),
        ).map { entities -> entities.map { it.asModel() } }.flowOn(ioDispatcher)

    override fun observeTransaction(id: Long): Flow<Transaction?> =
        transactionDao.observeById(id).map { it?.asModel() }.flowOn(ioDispatcher)

    override suspend fun save(transaction: Transaction): Long = withContext(ioDispatcher) {
        transactionDao.upsert(transaction.asEntity()).takeIf { it > 0 } ?: transaction.id
    }

    override suspend fun delete(id: Long) = withContext(ioDispatcher) {
        transactionDao.deleteById(id)
    }
}

/**
 * A SQL `LIKE` pattern (escape character `\`) matching [query] anywhere, with `%`, `_` and `\`
 * taken literally; null for a blank query. SQLite's LIKE ignores case for ASCII letters only.
 */
internal fun containsLikePattern(query: String): String? {
    val trimmed = query.trim()
    if (trimmed.isEmpty()) return null
    val escaped = buildString {
        trimmed.forEach { char ->
            if (char == '\\' || char == '%' || char == '_') append('\\')
            append(char)
        }
    }
    return "%$escaped%"
}
