package com.prajwalpawar.fiscus.ui.screens.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prajwalpawar.fiscus.data.model.Account
import com.prajwalpawar.fiscus.data.model.Category
import com.prajwalpawar.fiscus.data.model.PeriodTotals
import com.prajwalpawar.fiscus.data.model.TransactionDetails
import com.prajwalpawar.fiscus.data.model.TransactionType
import com.prajwalpawar.fiscus.data.model.CategoryType
import com.prajwalpawar.fiscus.data.repository.AccountRepository
import com.prajwalpawar.fiscus.data.repository.CategoryRepository
import com.prajwalpawar.fiscus.data.repository.TransactionRepository
import com.prajwalpawar.fiscus.util.monthRange
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import java.time.YearMonth

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
    val month: YearMonth = YearMonth.now(),
    // Totals for the whole month, regardless of filters.
    val monthTotals: PeriodTotals = PeriodTotals(),
    val filters: TransactionFilters = TransactionFilters(),
    // This month's transactions after filters, newest first.
    val transactions: List<TransactionDetails> = emptyList(),
    // True when the month has transactions but the filters hide them all.
    val filteredOut: Boolean = false,
    val accounts: List<Account> = emptyList(),
    // Categories the category filter can offer for the current type filter.
    val categories: List<Category> = emptyList()
) {
    // No browsing into the future.
    val canGoForward: Boolean get() = month < YearMonth.now()
}

class TransactionsViewModel(
    transactionRepository: TransactionRepository,
    accountRepository: AccountRepository,
    categoryRepository: CategoryRepository
) : ViewModel() {
    private val month = MutableStateFlow(YearMonth.now())
    private val filters = MutableStateFlow(TransactionFilters())

    @OptIn(ExperimentalCoroutinesApi::class)
    private val monthTransactions = month.flatMapLatest {
        transactionRepository.observeTransactionsInRange(monthRange(it))
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private val monthTotals = month.flatMapLatest {
        transactionRepository.observePeriodTotals(monthRange(it))
    }

    private val activeCategories = categoryRepository.categoriesWithUsage.map { all ->
        all.map { it.category }.filterNot { it.isArchived }
    }

    val uiState: StateFlow<TransactionsUiState> =
        combine(
            combine(month, monthTotals, monthTransactions, ::Triple),
            filters,
            accountRepository.accounts,
            activeCategories
        ) { (month, totals, transactions), filters, accounts, categories ->
            val visible = transactions.filter { it.matches(filters) }

            TransactionsUiState(
                isLoading = false,
                month = month,
                monthTotals = totals,
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

    fun previousMonth() {
        month.update { it.minusMonths(1) }
    }

    fun nextMonth() {
        month.update { if (it < YearMonth.now()) it.plusMonths(1) else it }
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
