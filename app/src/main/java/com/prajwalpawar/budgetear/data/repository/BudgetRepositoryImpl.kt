package com.prajwalpawar.budgetear.data.repository

import com.prajwalpawar.budgetear.data.local.dao.AccountDao
import com.prajwalpawar.budgetear.data.local.dao.CategoryDao
import com.prajwalpawar.budgetear.data.local.dao.TransactionDao
import com.prajwalpawar.budgetear.data.local.entities.AccountEntity
import com.prajwalpawar.budgetear.data.local.entities.CategoryEntity
import com.prajwalpawar.budgetear.data.local.entities.TransactionEntity
import com.prajwalpawar.budgetear.domain.model.Account
import com.prajwalpawar.budgetear.domain.model.Category
import com.prajwalpawar.budgetear.domain.model.Transaction
import com.prajwalpawar.budgetear.domain.model.TransactionType
import com.prajwalpawar.budgetear.domain.repository.BudgetRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Date
import javax.inject.Inject

class BudgetRepositoryImpl @Inject constructor(
    private val transactionDao: TransactionDao,
    private val categoryDao: CategoryDao,
    private val accountDao: AccountDao
) : BudgetRepository {

    override fun getTransactions(): Flow<List<Transaction>> {
        return transactionDao.getAllTransactions().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun insertTransaction(transaction: Transaction) {
        transactionDao.insertTransaction(transaction.toEntity())
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

    override fun getAccounts(): Flow<List<Account>> {
        return accountDao.getAllAccounts().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun insertAccount(account: Account) {
        accountDao.insertAccount(account.toEntity())
    }

    override suspend fun clearAllData() {
        transactionDao.deleteAllTransactions()
        categoryDao.deleteAllCategories()
        accountDao.deleteAllAccounts()
    }
}

// Mappers
fun TransactionEntity.toDomain() = Transaction(
    id = id,
    title = title,
    amount = amount,
    type = TransactionType.valueOf(type),
    categoryId = categoryId,
    accountId = accountId,
    date = Date(date),
    note = note
)

fun Transaction.toEntity() = TransactionEntity(
    id = id ?: 0,
    title = title,
    amount = amount,
    type = type.name,
    categoryId = categoryId,
    accountId = accountId,
    date = date.time,
    note = note
)

fun CategoryEntity.toDomain() = Category(id, name, icon, color, type?.let { TransactionType.valueOf(it) })
fun Category.toEntity() = CategoryEntity(id ?: 0, name, icon, color, type?.name)
fun AccountEntity.toDomain() = Account(id, name, balance, icon)
fun Account.toEntity() = AccountEntity(id ?: 0, name, balance, icon)
