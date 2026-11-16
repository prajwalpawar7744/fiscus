package com.prajwalpawar.fiscus.testing

import com.prajwalpawar.fiscus.data.repository.AccountRepository
import com.prajwalpawar.fiscus.data.repository.CategoryRepository
import com.prajwalpawar.fiscus.data.repository.TransactionRepository
import com.prajwalpawar.fiscus.model.Account
import com.prajwalpawar.fiscus.model.AccountWithBalance
import com.prajwalpawar.fiscus.model.Category
import com.prajwalpawar.fiscus.model.CategoryKind
import com.prajwalpawar.fiscus.model.CategoryWithUsage
import com.prajwalpawar.fiscus.model.Transaction
import com.prajwalpawar.fiscus.model.TransactionFilter
import com.prajwalpawar.fiscus.model.TransactionType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

// In-memory fakes for ViewModel tests (prefer fakes to mocks, ARCHITECTURE.md).

class FakeTransactionRepository : TransactionRepository {
    val transactions = MutableStateFlow<List<Transaction>>(emptyList())
    private var nextId = 1L

    override fun observeTransactions(): Flow<List<Transaction>> =
        transactions.map { list -> list.sortedWith(compareByDescending<Transaction> { it.occurredAt }.thenByDescending { it.id }) }

    override fun observeTransactions(accountId: Long): Flow<List<Transaction>> =
        observeTransactions().map { list -> list.filter { it.accountId == accountId || it.toAccountId == accountId } }

    /** Same rules as `TransactionDao.observeFiltered`, in memory. */
    override fun observeTransactions(filter: TransactionFilter): Flow<List<Transaction>> =
        observeTransactions().map { list ->
            val query = filter.query.trim()
            list.filter { t ->
                (filter.types.isEmpty() || t.type in filter.types) &&
                    (
                        filter.accountIds.isEmpty() || t.accountId in filter.accountIds ||
                            t.toAccountId in filter.accountIds
                        ) &&
                    (filter.categoryIds.isEmpty() || t.categoryId in filter.categoryIds) &&
                    (filter.from == null || t.occurredAt >= filter.from) &&
                    (filter.toExclusive == null || t.occurredAt < filter.toExclusive) &&
                    (
                        query.isEmpty() || t.title.contains(query, ignoreCase = true) ||
                            t.note.contains(query, ignoreCase = true)
                        )
            }
        }

    override fun observeTransaction(id: Long): Flow<Transaction?> = transactions.map { list -> list.find { it.id == id } }

    override suspend fun save(transaction: Transaction): Long {
        val saved = if (transaction.id == 0L) transaction.copy(id = nextId++) else transaction
        transactions.update { list -> list.filterNot { it.id == saved.id } + saved }
        return saved.id
    }

    override suspend fun delete(id: Long) = transactions.update { list -> list.filterNot { it.id == id } }
}

class FakeAccountRepository(
    private val transactionRepository: FakeTransactionRepository = FakeTransactionRepository(),
) : AccountRepository {
    val accounts = MutableStateFlow<List<Account>>(emptyList())

    override fun observeAccounts(): Flow<List<Account>> = accounts

    override fun observeAccountsWithBalance(): Flow<List<AccountWithBalance>> =
        combine(accounts, transactionRepository.transactions) { accounts, transactions ->
            accounts.map { account ->
                val balance = transactions.fold(account.openingBalance) { total, t ->
                    when {
                        t.accountId == account.id && t.type == TransactionType.INCOME -> total + t.amount
                        t.accountId == account.id -> total - t.amount
                        t.toAccountId == account.id -> total + t.amount
                        else -> total
                    }
                }
                val count = transactions.count { it.accountId == account.id || it.toAccountId == account.id }
                AccountWithBalance(account, balance, count)
            }
        }

    override fun observeAccountWithBalance(id: Long): Flow<AccountWithBalance?> =
        observeAccountsWithBalance().map { list -> list.find { it.account.id == id } }

    override suspend fun setArchived(id: Long, archived: Boolean) =
        accounts.update { list ->
            list.map {
                if (it.id != id) {
                    it
                } else {
                    it.copy(archived = archived, isDefault = it.isDefault && !archived)
                }
            }
        }

    override suspend fun setDefault(id: Long) =
        accounts.update { list -> list.map { it.copy(isDefault = it.id == id) } }

    /** Same rule as the FK RESTRICT: an account with transactions can't be deleted. */
    override suspend fun delete(id: Long) {
        val used = transactionRepository.transactions.value
            .any { it.accountId == id || it.toAccountId == id }
        check(!used) {
            "FOREIGN KEY constraint failed"
        }
        accounts.update { list -> list.filterNot { it.id == id } }
    }

    override suspend fun save(account: Account): Long {
        // The next free id, so tests can seed accounts with fixed ids first.
        val nextId = (accounts.value.maxOfOrNull { it.id } ?: 0L) + 1
        val saved = if (account.id == 0L) account.copy(id = nextId) else account
        accounts.update { list -> list.filterNot { it.id == saved.id } + saved }
        if (saved.isDefault) setDefault(saved.id)
        return saved.id
    }
}

class FakeCategoryRepository(
    private val transactionRepository: FakeTransactionRepository = FakeTransactionRepository(),
) : CategoryRepository {
    val categories = MutableStateFlow<List<Category>>(emptyList())
    private var nextId = 1L

    override fun observeCategories(): Flow<List<Category>> = categories

    override fun observeActiveCategories(kind: CategoryKind): Flow<List<Category>> =
        categories.map { list -> list.filter { it.kind == kind && !it.archived } }

    override suspend fun save(category: Category): Long {
        val saved = if (category.id == 0L) category.copy(id = nextId++) else category
        categories.update { list -> list.filterNot { it.id == saved.id } + saved }
        return saved.id
    }

    override suspend fun setArchived(id: Long, archived: Boolean) =
        categories.update { list -> list.map { if (it.id == id) it.copy(archived = archived) else it } }

    override fun observeCategoriesWithUsage(): Flow<List<CategoryWithUsage>> =
        combine(categories, transactionRepository.transactions) { categories, transactions ->
            categories.map { category ->
                CategoryWithUsage(category, transactions.count { it.categoryId == category.id })
            }
        }

    override fun observeCategory(id: Long): Flow<Category?> =
        categories.map { list -> list.find { it.id == id } }

    override suspend fun delete(id: Long) = categories.update { list -> list.filterNot { it.id == id } }
}
