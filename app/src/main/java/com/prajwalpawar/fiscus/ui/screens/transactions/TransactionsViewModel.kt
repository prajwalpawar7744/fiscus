package com.prajwalpawar.fiscus.ui.screens.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prajwalpawar.fiscus.data.local.pref.PreferenceManager
import com.prajwalpawar.fiscus.domain.model.Category
import com.prajwalpawar.fiscus.domain.model.Transaction
import com.prajwalpawar.fiscus.domain.repository.FiscusRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.flow.debounce
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

enum class TimeRange {
    ALL, TODAY, THIS_WEEK, THIS_MONTH, THIS_YEAR, CUSTOM
}

data class TransactionsUiState(
    val groupedTransactions: Map<String, Map<LocalDate, List<Transaction>>> = emptyMap(),
    val categories: Map<Long, Category> = emptyMap(),
    val currency: String = "USD",
    val searchText: String = "",
    val selectedCategoryId: Long? = null,
    val selectedTimeRange: TimeRange = TimeRange.ALL,
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
    val allCategories: List<Category> = emptyList(),
    val areAnimationsEnabled: Boolean = true,
    val topBarStyle: String = "standard"
)

@HiltViewModel
class TransactionsViewModel @Inject constructor(
    private val repository: FiscusRepository,
    private val preferenceManager: PreferenceManager
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

    val uiState: StateFlow<TransactionsUiState> = combine<Any, TransactionsUiState>(
        repository.getTransactions(),
        _categories,
        preferenceManager.currency,
        _filters,
        preferenceManager.areAnimationsEnabled,
        preferenceManager.topBarStyle,
        _searchText
    ) { args ->
        val transactions = args[0] as List<Transaction>
        val categories = args[1] as List<Category>
        val currency = args[2] as String
        val filters = args[3] as FilterParams
        val animationsEnabled = args[4] as Boolean
        val topBarStyle = args[5] as String
        val rawSearchText = args[6] as String
        
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
            currency = currency,
            searchText = rawSearchText,
            selectedCategoryId = categoryId,
            selectedTimeRange = timeRange,
            startDate = start,
            endDate = end,
            allCategories = categories.distinctBy { "${it.name}-${it.type}" },
            areAnimationsEnabled = animationsEnabled,
            topBarStyle = topBarStyle
        )
    }
.flowOn(Dispatchers.Default)
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = TransactionsUiState()
    )

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
}
