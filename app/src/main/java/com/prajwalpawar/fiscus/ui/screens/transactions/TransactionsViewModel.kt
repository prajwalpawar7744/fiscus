package com.prajwalpawar.fiscus.ui.screens.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prajwalpawar.fiscus.data.model.Account
import com.prajwalpawar.fiscus.data.model.Category
import com.prajwalpawar.fiscus.data.model.CategoryType
import com.prajwalpawar.fiscus.data.model.PeriodTotals
import com.prajwalpawar.fiscus.data.model.TransactionDetails
import com.prajwalpawar.fiscus.data.model.TransactionType
import com.prajwalpawar.fiscus.data.repository.AccountRepository
import com.prajwalpawar.fiscus.data.repository.CategoryRepository
import com.prajwalpawar.fiscus.data.repository.TransactionRepository
import com.prajwalpawar.fiscus.util.Period
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import java.time.LocalDate

data class TransactionFilters(
    // Null means all types.
    val type: TransactionType? = null,
    val accountId: Long? = null,
    val categoryId: Long? = null
) {
    val isActive: Boolean get() = type != null || accountId != null || categoryId != null
}

data class TransactionsUiState(
    val isLoading: Boolean = true,
    // A month by default; can be a single day or any range.
    val period: Period = Period.currentMonth(),
    // Totals of the transactions shown, so they follow the filters.
    val totals: PeriodTotals = PeriodTotals(),
    val filters: TransactionFilters = TransactionFilters(),
    // The period's transactions after filters, newest first.
    val transactions: List<TransactionDetails> = emptyList(),
    // True when the period has transactions but the filters hide them all.
    val filteredOut: Boolean = false,
    val accounts: List<Account> = emptyList(),
    // Categories the category filter can offer for the current type filter.
    val categories: List<Category> = emptyList()
)

class TransactionsViewModel(
    transactionRepository: TransactionRepository,
    accountRepository: AccountRepository,
    categoryRepository: CategoryRepository
) : ViewModel() {
    private val period = MutableStateFlow(Period.currentMonth())
    private val filters = MutableStateFlow(TransactionFilters())

    @OptIn(ExperimentalCoroutinesApi::class)
    private val periodTransactions = period.flatMapLatest {
        transactionRepository.observeTransactionsInRange(it.toDateRange())
    }

    private val activeCategories = categoryRepository.categoriesWithUsage.map { all ->
        all.map { it.category }.filterNot { it.isArchived }
    }

    val uiState: StateFlow<TransactionsUiState> =
        combine(
            combine(period, periodTransactions, ::Pair),
            filters,
            accountRepository.accounts,
            activeCategories
        ) { (period, transactions), filters, accounts, categories ->
            val visible = transactions.filter { it.matches(filters) }

            TransactionsUiState(
                isLoading = false,
                period = period,
                totals = visible.totals(),
                filters = filters,
                transactions = visible,
                filteredOut = transactions.isNotEmpty() && visible.isEmpty(),
                accounts = accounts,
                categories = categories.filter { category ->
                    when (filters.type) {
                        null -> true
                        TransactionType.EXPENSE -> category.type == CategoryType.EXPENSE
                        TransactionType.INCOME -> category.type == CategoryType.INCOME
                        TransactionType.TRANSFER -> false
                    }
                }
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = TransactionsUiState()
        )

    // Ignores periods that start in the future.
    fun setPeriod(selected: Period) {
        if (!selected.start.isAfter(LocalDate.now())) period.value = selected
    }

    fun setTypeFilter(type: TransactionType?) {
        filters.update { current ->
            current.copy(
                type = type,
                // A category only fits income or expense, so drop it when the type no longer allows it.
                categoryId = current.categoryId.takeIf { type != TransactionType.TRANSFER }
            )
        }
    }

    fun setAccountFilter(accountId: Long?) {
        filters.update { it.copy(accountId = accountId) }
    }

    fun setCategoryFilter(categoryId: Long?) {
        filters.update { it.copy(categoryId = categoryId) }
    }

    fun clearFilters() {
        filters.value = TransactionFilters()
    }
}

// Income and expense sums (transfers only move money) plus a count of every row.
internal fun List<TransactionDetails>.totals(): PeriodTotals {
    return PeriodTotals(
        income = filter { it.type == TransactionType.INCOME }.sumOf { it.amount },
        expense = filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount },
        count = size
    )
}

internal fun TransactionDetails.matches(filters: TransactionFilters): Boolean {
    if (filters.type != null && type != filters.type) return false

    if (filters.accountId != null &&
        filters.accountId != accountId &&
        filters.accountId != sourceAccountId &&
        filters.accountId != destinationAccountId
    ) {
        return false
    }

    if (filters.categoryId != null && filters.categoryId != categoryId) return false

    return true
}
