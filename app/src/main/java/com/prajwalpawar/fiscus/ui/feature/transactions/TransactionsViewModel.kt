package com.prajwalpawar.fiscus.ui.feature.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prajwalpawar.fiscus.data.repository.AccountRepository
import com.prajwalpawar.fiscus.data.repository.CategoryRepository
import com.prajwalpawar.fiscus.data.repository.TransactionRepository
import com.prajwalpawar.fiscus.model.Account
import com.prajwalpawar.fiscus.model.Category
import com.prajwalpawar.fiscus.model.Money
import com.prajwalpawar.fiscus.model.Transaction
import com.prajwalpawar.fiscus.model.TransactionFilter
import com.prajwalpawar.fiscus.model.TransactionType
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.time.Clock
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateRange
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime

/**
 * Screen-level state holder for the transaction list (ADR-022). What the user chose (query, chip
 * filters, open picker) is a private form; it's turned into a [TransactionFilter] that the
 * database applies, and the result is grouped by local day and exposed with `stateIn`
 * (ARCHITECTURE.md "Forms").
 */
@HiltViewModel
class TransactionsViewModel @Inject constructor(
    transactionRepository: TransactionRepository,
    accountRepository: AccountRepository,
    categoryRepository: CategoryRepository,
    private val clock: Clock,
    private val timeZone: TimeZone,
) : ViewModel() {

    private val form = MutableStateFlow(ListForm())

    @OptIn(ExperimentalCoroutinesApi::class) // flatMapLatest
    private val transactions = form
        .map { it.toFilter() }
        .distinctUntilChanged()
        .flatMapLatest(transactionRepository::observeTransactions)

    val uiState: StateFlow<TransactionsUiState> = combine(
        transactions,
        accountRepository.observeAccounts(),
        categoryRepository.observeCategories(),
        form,
    ) { transactions, accounts, categories, form ->
        TransactionsUiState(
            isLoading = false,
            today = clock.now().toLocalDateTime(timeZone).date,
            sections = groupByDay(transactions, accounts, categories),
            selectedTypes = form.types,
            selectedAccountIds = form.accountIds,
            selectedCategoryIds = form.categoryIds,
            dateRange = form.dateRange,
            query = form.query,
            accounts = accounts,
            categories = categories,
            openPicker = form.openPicker,
            summary = summarize(transactions),
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = TransactionsUiState(),
    )

    fun onEvent(event: TransactionsEvent) {
        when (event) {
            is TransactionsEvent.QueryChanged -> form.update { it.copy(query = event.query) }
            is TransactionsEvent.TypeToggled ->
                form.update { it.copy(types = it.types.toggle(event.type)) }
            is TransactionsEvent.AccountToggled ->
                form.update { it.copy(accountIds = it.accountIds.toggle(event.accountId)) }
            is TransactionsEvent.CategoryToggled ->
                form.update { it.copy(categoryIds = it.categoryIds.toggle(event.categoryId)) }
            is TransactionsEvent.DateRangeChanged ->
                form.update { it.copy(dateRange = event.range, openPicker = null) }
            is TransactionsEvent.PickerOpened -> form.update { it.copy(openPicker = event.picker) }
            TransactionsEvent.PickerDismissed -> form.update { it.copy(openPicker = null) }
            is TransactionsEvent.FilterCleared -> form.update { it.cleared(event.picker) }
            TransactionsEvent.FiltersCleared -> form.update { ListForm(query = it.query) }
        }
    }

    /**
     * The database filter (ADR-029). Without a picked range the list shows **the current month**;
     * a search looks through every month unless a range was picked.
     */
    private fun ListForm.toFilter(): TransactionFilter {
        val today = clock.now().toLocalDateTime(timeZone).date
        val range = dateRange ?: currentMonth(today).takeIf { query.isBlank() }
        return TransactionFilter(
            query = query,
            types = types,
            accountIds = accountIds,
            categoryIds = categoryIds,
            from = range?.start?.atStartOfDayIn(timeZone),
            toExclusive = range?.endInclusive?.plus(DatePeriod(days = 1))?.atStartOfDayIn(timeZone),
        )
    }

    private fun groupByDay(
        transactions: List<Transaction>,
        accounts: List<Account>,
        categories: List<Category>,
    ): List<TransactionDaySection> {
        val accountsById = accounts.associateBy { it.id }
        val categoriesById = categories.associateBy { it.id }
        // Transactions arrive newest first, and groupBy keeps that order for days and items.
        return transactions
            .groupBy { it.occurredAt.toLocalDateTime(timeZone).date }
            .map { (date, dayTransactions) ->
                TransactionDaySection(
                    date = date,
                    items = dayTransactions.map { transaction ->
                        TransactionListEntry(
                            transaction = transaction,
                            account = accountsById[transaction.accountId],
                            toAccount = transaction.toAccountId?.let(accountsById::get),
                            category = transaction.categoryId?.let(categoriesById::get),
                        )
                    },
                    net = dayNet(dayTransactions),
                )
            }
    }

    /** What the user chose; everything else is derived. */
    private data class ListForm(
        val query: String = "",
        val types: Set<TransactionType> = emptySet(),
        val accountIds: Set<Long> = emptySet(),
        val categoryIds: Set<Long> = emptySet(),
        val dateRange: LocalDateRange? = null,
        val openPicker: FilterPicker? = null,
    ) {
        fun cleared(picker: FilterPicker) = when (picker) {
            FilterPicker.TYPE -> copy(types = emptySet())
            FilterPicker.ACCOUNT -> copy(accountIds = emptySet())
            FilterPicker.CATEGORY -> copy(categoryIds = emptySet())
            FilterPicker.DATE -> copy(dateRange = null)
        }
    }
}

/**
 * Income and expense totals; transfers don't count (DOMAIN.md). Null when there's no income or
 * expense, or the currencies are mixed (no conversion yet, ADR-016).
 */
internal fun summarize(transactions: List<Transaction>): PeriodSummary? {
    val counted = transactions.filter { it.type != TransactionType.TRANSFER }
    val currency = counted.map { it.amount.currencyCode }.distinct().singleOrNull() ?: return null
    val zero = Money.zero(currency)
    return counted.fold(PeriodSummary(zero, zero)) { summary, transaction ->
        when (transaction.type) {
            TransactionType.INCOME -> summary.copy(income = summary.income + transaction.amount)
            else -> summary.copy(expense = summary.expense + transaction.amount)
        }
    }
}

/** Income minus expense for one day; null like [summarize]. */
internal fun dayNet(transactions: List<Transaction>): Money? = summarize(transactions)?.net

/** The calendar month containing [today], first to last day. */
internal fun currentMonth(today: LocalDate): LocalDateRange {
    val first = LocalDate(today.year, today.month, 1)
    return first..first.plus(DatePeriod(months = 1)).minus(DatePeriod(days = 1))
}

private fun <T> Set<T>.toggle(item: T): Set<T> = if (item in this) this - item else this + item
