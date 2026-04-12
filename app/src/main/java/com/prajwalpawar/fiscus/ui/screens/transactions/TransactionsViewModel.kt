package com.prajwalpawar.fiscus.ui.screens.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prajwalpawar.fiscus.data.local.pref.PreferenceManager
import com.prajwalpawar.fiscus.domain.model.Account
import com.prajwalpawar.fiscus.domain.model.Category
import com.prajwalpawar.fiscus.domain.model.Transaction
import com.prajwalpawar.fiscus.domain.repository.FiscusRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.flow.debounce
import com.prajwalpawar.fiscus.data.report.ReportManager
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

enum class TimeRange {
    ALL, TODAY, THIS_WEEK, THIS_MONTH, THIS_YEAR, CUSTOM
}

data class TransactionsUiState(
    val groupedTransactions: Map<String, Map<LocalDate, List<Transaction>>> = emptyMap(),
    val categories: Map<Long, Category> = emptyMap(),
    val accounts: Map<Long, Account> = emptyMap(),
    val currency: String = "USD",
    val searchText: String = "",
    val selectedCategoryId: Long? = null,
    val selectedTimeRange: TimeRange = TimeRange.ALL,
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
    val allCategories: List<Category> = emptyList(),
    val areAnimationsEnabled: Boolean = true,
    val topBarStyle: String = "standard",
    val selectedTransactionDetail: Transaction? = null,
    val isPrivacyModeEnabled: Boolean = false
)

@HiltViewModel
class TransactionsViewModel @Inject constructor(
    private val repository: FiscusRepository,
    private val preferenceManager: PreferenceManager,
    private val reportManager: ReportManager
) : ViewModel() {

    private val _searchText = MutableStateFlow("")
    private val _selectedCategoryId = MutableStateFlow<Long?>(null)
    private val _selectedTimeRange = MutableStateFlow(TimeRange.ALL)
    private val _startDate = MutableStateFlow<LocalDate?>(null)
    private val _endDate = MutableStateFlow<LocalDate?>(null)

    val currentSearchText = _searchText.asStateFlow()

    private val _categories = repository.getCategories()
        .distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _filters = combine(
        _searchText.debounce(300L),
        _selectedCategoryId,
        _selectedTimeRange,
        _startDate,
        _endDate
    ) { search, categoryId, timeRange, start, end ->
        FilterParams(search, categoryId, timeRange, start, end)
    }

    private data class FilterParams(
        val search: String,
        val categoryId: Long?,
        val timeRange: TimeRange,
        val startDate: LocalDate?,
        val endDate: LocalDate?
    )

    private val _accounts = repository.getAccounts()
        .distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _transactionsUiState: StateFlow<TransactionsUiState> = combine<Any?, TransactionsUiState>(
        repository.getTransactions(),
        _categories,
        _accounts,
        preferenceManager.currency,
        _filters,
        preferenceManager.areAnimationsEnabled,
        preferenceManager.topBarStyle,
        _searchText,
        preferenceManager.isPrivacyModeEnabled
    ) { args ->
        val transactions = args[0] as List<Transaction>
        val categories = args[1] as List<Category>
        @Suppress("UNCHECKED_CAST")
        val accounts = args[2] as List<Account>
        val currency = args[3] as String
        val filters = args[4] as FilterParams
        val animationsEnabled = args[5] as Boolean
        val topBarStyle = args[6] as String
        val rawSearchText = args[7] as String
        val privacyEnabled = args[8] as Boolean
        
        val (search, categoryId, timeRange, start, end) = filters
        
        val filteredTransactions = transactions.filter { transaction ->
            val matchesSearch = transaction.title.contains(search, ignoreCase = true) || 
                               transaction.note.contains(search, ignoreCase = true)
            val matchesCategory = categoryId == null || transaction.categoryId == categoryId
            
            val transactionDate = transaction.date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
            val now = LocalDate.now()
            val matchesTime = when (timeRange) {
                TimeRange.ALL -> true
                TimeRange.TODAY -> transactionDate.isEqual(now)
                TimeRange.THIS_WEEK -> {
                    val startOfWeek = now.minusDays(now.dayOfWeek.value.toLong() - 1)
                    !transactionDate.isBefore(startOfWeek) && !transactionDate.isAfter(now)
                }
                TimeRange.THIS_MONTH -> transactionDate.month == now.month && transactionDate.year == now.year
                TimeRange.THIS_YEAR -> transactionDate.year == now.year
                TimeRange.CUSTOM -> {
                    if (start != null && end != null) {
                        !transactionDate.isBefore(start) && !transactionDate.isAfter(end)
                    } else true
                }
            }
            
            matchesSearch && matchesCategory && matchesTime
        }.sortedByDescending { it.date }
        
        // Group by Month-Year string -> Group by LocalDate -> List of Transactions
        val grouped = filteredTransactions.groupBy { transaction ->
            val date = transaction.date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
            "${date.month} ${date.year}"
        }.mapValues { entry ->
            entry.value.groupBy { transaction ->
                transaction.date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
            }
        }

        TransactionsUiState(
            groupedTransactions = grouped,
            categories = categories.associateBy { it.id ?: 0L },
            accounts = accounts.associateBy { it.id ?: 0L },
            currency = currency,
            searchText = rawSearchText,
            selectedCategoryId = categoryId,
            selectedTimeRange = timeRange,
            startDate = start,
            endDate = end,
            allCategories = categories.distinctBy { "${it.name}-${it.type}" },
            areAnimationsEnabled = animationsEnabled,
            topBarStyle = topBarStyle,
            isPrivacyModeEnabled = privacyEnabled
        )
    }
.flowOn(Dispatchers.Default)
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = TransactionsUiState()
    )

    private val _selectedTransaction = MutableStateFlow<Transaction?>(null)

    val uiState: StateFlow<TransactionsUiState> = combine(
        _transactionsUiState,
        _selectedTransaction
    ) { base, selected ->
        base.copy(selectedTransactionDetail = selected)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TransactionsUiState())

    fun onTransactionClick(transaction: Transaction) {
        _selectedTransaction.value = transaction
    }

    fun clearSelectedTransaction() {
        _selectedTransaction.value = null
    }

    fun onSearchTextChanged(text: String) {
        _searchText.value = text
    }

    fun onCategorySelected(categoryId: Long?) {
        _selectedCategoryId.value = categoryId
    }

    fun onTimeRangeSelected(range: TimeRange) {
        _selectedTimeRange.value = range
    }

    fun onDateRangeSelected(start: LocalDate?, end: LocalDate?) {
        _startDate.value = start
        _endDate.value = end
    }

    suspend fun exportTransactionsToCsv(): String {
        val state = uiState.value
        val allFilteredTransactions = mutableListOf<Transaction>()
        
        // Flatten grouped transactions
        state.groupedTransactions.values.forEach { dateGroups ->
            dateGroups.values.forEach { transactions ->
                allFilteredTransactions.addAll(transactions)
            }
        }
        
        // Sort by date descending (already mostly done by grouping, but let's be sure)
        val sortedTransactions = allFilteredTransactions.sortedByDescending { it.date }
        
        return reportManager.generateCsvReport(
            transactions = sortedTransactions,
            categories = state.categories,
            accounts = state.accounts
        )
    }
}
