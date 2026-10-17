package com.prajwalpawar.fiscus.data.repository

import androidx.room.withTransaction
import com.prajwalpawar.fiscus.data.local.FiscusDatabase
import com.prajwalpawar.fiscus.data.local.dao.TransactionDao
import com.prajwalpawar.fiscus.data.local.dao.TransactionDetailsRow
import com.prajwalpawar.fiscus.data.local.entity.toEntity
import com.prajwalpawar.fiscus.data.local.entity.toTransaction
import com.prajwalpawar.fiscus.data.model.CategoryTotal
import com.prajwalpawar.fiscus.data.model.DayTotals
import com.prajwalpawar.fiscus.data.model.MonthTotals
import com.prajwalpawar.fiscus.data.model.PeriodTotals
import com.prajwalpawar.fiscus.data.model.Transaction
import com.prajwalpawar.fiscus.data.model.TransactionDetails
import com.prajwalpawar.fiscus.data.model.TransactionType
import com.prajwalpawar.fiscus.util.DateRange
import com.prajwalpawar.fiscus.util.storedDateToLocalDate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.YearMonth

class TransactionRepository (
    private val database: FiscusDatabase,
    private val transactionDao: TransactionDao
) {
    val transactions: Flow<List<Transaction>> =
        transactionDao.observeTransactions().map { transaction ->
            transaction.map { it.toTransaction() }
        }

    fun observeRecentTransactions(limit: Int): Flow<List<TransactionDetails>> {
        return transactionDao.observeRecentTransactions(limit).map { rows ->
            rows.map { it.toDetails() }
        }
    }

    fun observeTransactionsInRange(range: DateRange): Flow<List<TransactionDetails>> {
        return transactionDao.observeTransactionsInRange(range.startMillis, range.endMillis)
            .map { rows -> rows.map { it.toDetails() } }
    }

    // Case-insensitive (for ASCII) match on title or note, newest first.
    fun searchTransactions(query: String, limit: Int): Flow<List<TransactionDetails>> {
        // Escape LIKE wildcards so "%" or "_" in the query match literally.
        val pattern = query
            .replace("\\", "\\\\")
            .replace("%", "\\%")
            .replace("_", "\\_")

        return transactionDao.searchTransactions(pattern, limit)
            .map { rows -> rows.map { it.toDetails() } }
    }

    fun observeCategoryTotals(type: TransactionType, range: DateRange): Flow<List<CategoryTotal>> {
        return transactionDao.observeCategoryTotals(type.name, range.startMillis, range.endMillis)
            .map { rows ->
                rows.map { row ->
                    CategoryTotal(
                        categoryId = row.categoryId,
                        name = row.categoryName ?: "Uncategorised",
                        iconKey = row.iconKey ?: "category",
                        total = row.total
                    )
                }
            }
    }

    // One entry per month in the range that has transactions; callers fill the gaps.
    fun observeMonthlyTotals(range: DateRange): Flow<List<MonthTotals>> {
        return transactionDao.observeMonthlyTotals(range.startMillis, range.endMillis)
            .map { rows ->
                rows.map { MonthTotals(YearMonth.parse(it.month), it.income, it.expense) }
            }
    }

    // One entry per day in the range that has transactions; callers fill the gaps.
    fun observeDailyTotals(range: DateRange): Flow<List<DayTotals>> {
        return transactionDao.observeDailyTotals(range.startMillis, range.endMillis)
            .map { rows ->
                rows.map { DayTotals(storedDateToLocalDate(it.day), it.income, it.expense) }
            }
    }

    fun observeLargestExpenses(range: DateRange, limit: Int): Flow<List<TransactionDetails>> {
        return transactionDao.observeLargestExpenses(range.startMillis, range.endMillis, limit)
            .map { rows -> rows.map { it.toDetails() } }
    }

    // Null once the transaction is deleted.
    fun observeTransactionDetails(id: Long): Flow<TransactionDetails?> {
        return transactionDao.observeTransactionDetails(id).map { it?.toDetails() }
    }

    suspend fun deleteTransaction(id: Long) {
        transactionDao.deleteTransaction(id)
    }

    fun observePeriodTotals(range: DateRange): Flow<PeriodTotals> {
        return transactionDao.observePeriodTotals(range.startMillis, range.endMillis).map {
            PeriodTotals(income = it.income, expense = it.expense, count = it.count)
        }
    }

    suspend fun getTransaction(id: Long): Transaction? {
        return transactionDao.getTransaction(id)?.toTransaction()
    }

    suspend fun addTransaction(transaction: Transaction): Long {
        validate(transaction)

        return database.withTransaction {
            transactionDao.insertTransaction(transaction.toEntity())
        }
    }


    suspend fun updateTransaction(transaction: Transaction) {
        validate(transaction)

        database.withTransaction {
            transactionDao.updateTransaction(transaction.toEntity())
        }
    }

    suspend fun deleteTransaction(transaction: Transaction) {
        transactionDao.deleteTransaction(transaction.toEntity())
    }

    private fun TransactionDetailsRow.toDetails(): TransactionDetails {
        return TransactionDetails(
            id = id,
            type = TransactionType.valueOf(type),
            amount = amount,
            title = title,
            note = note,
            date = date,
            categoryId = categoryId,
            accountId = accountId,
            sourceAccountId = sourceAccountId,
            destinationAccountId = destinationAccountId,
            categoryName = categoryName,
            categoryIconKey = categoryIconKey,
            accountName = accountName,
            sourceAccountName = sourceAccountName,
            destinationAccountName = destinationAccountName
        )
    }

    private fun validate(transaction: Transaction) {
        require(transaction.amount > 0L) {
            "Transaction amount must be greater than zero."
        }
        require(transaction.title.isNotBlank()) {
            "A transaction requires a title."
        }

        when (transaction.type) {
            TransactionType.EXPENSE,
            TransactionType.INCOME -> {
                require(transaction.categoryId != null) {
                    "An income or expense requires a category."
                }
                require(transaction.accountId != null) {
                    "An income or expense requires an account."
                }
                require(
                    transaction.sourceAccountId == null &&
                            transaction.destinationAccountId == null
                ) {
                    "An income or expense cannot have transfer accounts."
                }
            }

            TransactionType.TRANSFER -> {
                require(transaction.categoryId == null) {
                    "A transfer cannot have a category."
                }
                require(transaction.accountId == null) {
                    "A transfer uses source and destination accounts."
                }

                val source = requireNotNull(transaction.sourceAccountId) {
                    "A transfer requires a source account."
                }
                val destination =
                    requireNotNull(transaction.destinationAccountId) {
                        "A transfer requires a destination account."
                    }

                require(source != destination) {
                    "Source and destination accounts must be different."
                }
            }
        }
    }
}