package com.prajwalpawar.fiscus.data.repository

import com.prajwalpawar.fiscus.data.local.dao.AccountDao
import com.prajwalpawar.fiscus.data.local.dao.CategoryDao
import com.prajwalpawar.fiscus.data.local.dao.TransactionDao
import com.prajwalpawar.fiscus.data.local.entities.AccountEntity
import com.prajwalpawar.fiscus.data.local.entities.CategoryEntity
import com.prajwalpawar.fiscus.data.local.entities.TransactionEntity
import com.prajwalpawar.fiscus.data.local.entities.TransactionSubItemEntity
import com.prajwalpawar.fiscus.data.local.entities.TransactionWithSubItems
import com.prajwalpawar.fiscus.domain.model.Account
import com.prajwalpawar.fiscus.domain.model.Category
import com.prajwalpawar.fiscus.domain.model.Transaction
import com.prajwalpawar.fiscus.domain.model.TransactionSubItem
import com.prajwalpawar.fiscus.domain.model.TransactionType
import com.prajwalpawar.fiscus.domain.repository.FiscusRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Date
import javax.inject.Inject

class FiscusRepositoryImpl @Inject constructor(
    private val transactionDao: TransactionDao,
    private val categoryDao: CategoryDao,
    private val accountDao: AccountDao
) : FiscusRepository {

    override fun getTransactions(): Flow<List<Transaction>> {
        return transactionDao.getAllTransactions().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getRecentTransactions(limit: Int): Flow<List<Transaction>> {
        return transactionDao.getRecentTransactions(limit).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getTotalAmountByType(type: String): Flow<Double> {
        return transactionDao.getTotalAmountByType(type).map { it ?: 0.0 }
    }

    override suspend fun insertTransaction(transaction: Transaction) {
        val transactionId = transactionDao.insertTransaction(transaction.toEntity())
        if (transaction.subItems.isNotEmpty()) {
            transactionDao.insertSubItems(transaction.subItems.map { it.toEntity(transactionId) })
        }
    }

    override suspend fun updateTransaction(transaction: Transaction) {
        transactionDao.updateTransaction(transaction.toEntity())
        val transactionId = transaction.id ?: return
        transactionDao.deleteSubItemsByTransactionId(transactionId)
        if (transaction.subItems.isNotEmpty()) {
            transactionDao.insertSubItems(transaction.subItems.map { it.toEntity(transactionId) })
        }
    }

    override suspend fun deleteTransaction(transaction: Transaction) {
        transactionDao.deleteTransaction(transaction.toEntity())
    }

    override fun getCategories(): Flow<List<Category>> {
        return categoryDao.getAllCategories().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun insertCategory(category: Category) {
        categoryDao.insertCategory(category.toEntity())
    }

    override suspend fun deleteCategory(category: Category) {
        categoryDao.deleteCategory(category.toEntity())
    }

    override fun getAccounts(): Flow<List<Account>> {
        return accountDao.getAllAccounts().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun insertAccount(account: Account) {
        accountDao.insertAccount(account.toEntity())
    }

    override suspend fun updateAccount(account: Account) {
        accountDao.updateAccount(account.toEntity())
    }

    override suspend fun deleteAccount(account: Account) {
        accountDao.deleteAccount(account.toEntity())
    }

    override suspend fun clearAllData() {
        transactionDao.deleteAllTransactions()
        categoryDao.deleteAllCategories()
        accountDao.deleteAllAccounts()
    }
}

// Mappers
fun TransactionWithSubItems.toDomain() = Transaction(
    id = transaction.id,
    title = transaction.title,
    amount = transaction.amount,
    type = safeTransactionType(transaction.type),
    categoryId = transaction.categoryId,
    accountId = transaction.accountId,
    toAccountId = transaction.toAccountId,
    date = Date(transaction.date),
    note = transaction.note,
    subItems = subItems.map { it.toDomain() }
)

private fun safeTransactionType(type: String): TransactionType {
    return try {
        TransactionType.valueOf(type)
    } catch (e: Exception) {
        TransactionType.EXPENSE
    }
}

fun TransactionSubItemEntity.toDomain() = TransactionSubItem(
    id = id,
    transactionId = transactionId,
    name = name,
    amount = amount
)

fun TransactionSubItem.toEntity(transactionId: Long) = TransactionSubItemEntity(
    id = id ?: 0,
    transactionId = transactionId,
    name = name,
    amount = amount
)

fun Transaction.toEntity() = TransactionEntity(
    id = id ?: 0,
    title = title,
    amount = amount,
    type = type.name,
    categoryId = categoryId,
    accountId = accountId,
    toAccountId = toAccountId,
    date = date.time,
    note = note
)

fun CategoryEntity.toDomain() = Category(
    id = id, 
    name = name, 
    icon = icon, 
    color = color, 
    type = type?.let { 
        try { TransactionType.valueOf(it) } catch(e: Exception) { null } 
    }, 
    isSystem = isSystem
)
fun Category.toEntity() = CategoryEntity(id ?: 0, name, icon, color, type?.name, isSystem)
fun AccountEntity.toDomain() = Account(id, name, balance, icon)
fun Account.toEntity() = AccountEntity(id ?: 0, name, balance, icon)
