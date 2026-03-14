package com.prajwalpawar.budgetear.ui.screens.analysis

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prajwalpawar.budgetear.data.local.pref.PreferenceManager
import com.prajwalpawar.budgetear.domain.model.Category
import com.prajwalpawar.budgetear.domain.model.Transaction
import com.prajwalpawar.budgetear.domain.model.TransactionType
import com.prajwalpawar.budgetear.domain.repository.BudgetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*
import javax.inject.Inject

enum class AnalysisGranularity {
    DAILY, MONTHLY
}

enum class AnalysisChartType {
    BAR, LINE, PIE
}

data class TimeDataPoint(
    val label: String,
    val amount: Double
)

enum class TimeRange {
    ALL, TODAY, THIS_WEEK, THIS_MONTH, THIS_YEAR, CUSTOM
}

data class AnalysisUiState(
    val categoryBreakdown: List<CategoryAnalysis> = emptyList(),
    val totalExpense: Double = 0.0,
    val totalIncome: Double = 0.0,
    val currency: String = "USD",
    val granularity: AnalysisGranularity = AnalysisGranularity.DAILY,
    val expenseDataPoints: List<TimeDataPoint> = emptyList(),
    val incomeDataPoints: List<TimeDataPoint> = emptyList(),
    val selectedTimeRange: TimeRange = TimeRange.ALL,
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
    val selectedTransactionType: TransactionType? = TransactionType.EXPENSE,
    val allCategories: List<Category> = emptyList(),
    val selectedCategoryId: Long? = null,
    val selectedChartType: AnalysisChartType = AnalysisChartType.BAR
)

data class CategoryAnalysis(
    val category: Category,
    val amount: Double,
    val percentage: Float,
    val transactionCount: Int
)

@HiltViewModel
class AnalysisViewModel @Inject constructor(
    private val repository: BudgetRepository,
    private val preferenceManager: PreferenceManager
) : ViewModel() {

    private val _granularity = MutableStateFlow(AnalysisGranularity.DAILY)
    private val _selectedTimeRange = MutableStateFlow(TimeRange.ALL)
    private val _startDate = MutableStateFlow<LocalDate?>(null)
    private val _endDate = MutableStateFlow<LocalDate?>(null)
    private val _selectedTransactionType = MutableStateFlow<TransactionType?>(TransactionType.EXPENSE)
    private val _selectedCategoryId = MutableStateFlow<Long?>(null)
    private val _selectedChartType = MutableStateFlow(AnalysisChartType.BAR)

    private val _categories = repository.getCategories()
        .distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _filters = combine<Any?, FilterParams>(
        _granularity,
        _selectedTimeRange,
        _startDate,
        _endDate,
        _selectedTransactionType,
        _selectedCategoryId,
        _selectedChartType
    ) { params ->
        FilterParams(
            granularity = params[0] as AnalysisGranularity,
            timeRange = params[1] as TimeRange,
            startDate = params[2] as? LocalDate,
            endDate = params[3] as? LocalDate,
            type = params[4] as? TransactionType,
            categoryId = params[5] as? Long,
            chartType = params[6] as AnalysisChartType
        )
    }

    private data class FilterParams(
        val granularity: AnalysisGranularity,
        val timeRange: TimeRange,
        val startDate: LocalDate?,
        val endDate: LocalDate?,
        val type: TransactionType?,
        val categoryId: Long?,
        val chartType: AnalysisChartType
    )

    val uiState: StateFlow<AnalysisUiState> = combine(
        repository.getTransactions(),
        _categories,
        preferenceManager.currency,
        _filters
    ) { transactions, categories, currency, filters ->
        val filteredTransactions = transactions.filter { transaction ->
            val matchesType = filters.type == null || transaction.type == filters.type
            val matchesCategory = filters.categoryId == null || transaction.categoryId == filters.categoryId
            
            val transactionDate = transaction.date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
            val now = LocalDate.now()
            val matchesTime = when (filters.timeRange) {
                TimeRange.ALL -> true
                TimeRange.TODAY -> transactionDate.isEqual(now)
                TimeRange.THIS_WEEK -> {
                    val startOfWeek = now.minusDays(now.dayOfWeek.value.toLong() - 1)
                    !transactionDate.isBefore(startOfWeek) && !transactionDate.isAfter(now)
                }
                TimeRange.THIS_MONTH -> transactionDate.month == now.month && transactionDate.year == now.year
                TimeRange.THIS_YEAR -> transactionDate.year == now.year
                TimeRange.CUSTOM -> {
                    if (filters.startDate != null && filters.endDate != null) {
                        !transactionDate.isBefore(filters.startDate) && !transactionDate.isAfter(filters.endDate)
                    } else true
                }
            }
            
            matchesType && matchesCategory && matchesTime
        }
        
        val expenses = filteredTransactions.filter { it.type == TransactionType.EXPENSE }
        val income = filteredTransactions.filter { it.type == TransactionType.INCOME }
        
        val totalExpense = expenses.sumOf { it.amount }
        val totalIncome = income.sumOf { it.amount }
        
        val categoryMap = categories.associateBy { it.id ?: 0L }
        
        val breakdown = if (filters.type == TransactionType.INCOME) {
            income.groupBy { it.categoryId }
        } else {
            expenses.groupBy { it.categoryId }
        }.mapNotNull { (catId, transList) ->
            val category = categoryMap[catId] ?: return@mapNotNull null
            val amount = transList.sumOf { it.amount }
            val total = if (filters.type == TransactionType.INCOME) totalIncome else totalExpense
            CategoryAnalysis(
                category = category,
                amount = amount,
                percentage = if (total > 0) (amount / total).toFloat() else 0f,
                transactionCount = transList.size
            )
        }.sortedByDescending { it.amount }

        val expensePoints = aggregateByTime(expenses, filters.granularity)
        val incomePoints = aggregateByTime(income, filters.granularity)

        AnalysisUiState(
            categoryBreakdown = breakdown,
            totalExpense = totalExpense,
            totalIncome = totalIncome,
            currency = currency,
            granularity = filters.granularity,
            expenseDataPoints = expensePoints,
            incomeDataPoints = incomePoints,
            selectedTimeRange = filters.timeRange,
            startDate = filters.startDate,
            endDate = filters.endDate,
            selectedTransactionType = filters.type,
            allCategories = categories.distinctBy { "${it.name}-${it.type}" },
            selectedCategoryId = filters.categoryId,
            selectedChartType = filters.chartType
        )
    }.flowOn(Dispatchers.Default)
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AnalysisUiState()
    )
    
    fun setGranularity(granularity: AnalysisGranularity) {
        _granularity.value = granularity
    }

    fun onTimeRangeSelected(range: TimeRange) {
        _selectedTimeRange.value = range
    }

    fun onDateRangeSelected(start: LocalDate?, end: LocalDate?) {
        _startDate.value = start
        _endDate.value = end
    }

    fun onTransactionTypeSelected(type: TransactionType?) {
        _selectedTransactionType.value = type
    }

    fun onCategorySelected(categoryId: Long?) {
        _selectedCategoryId.value = categoryId
    }

    fun onChartTypeSelected(chartType: AnalysisChartType) {
        _selectedChartType.value = chartType
    }

    private fun aggregateByTime(transactions: List<Transaction>, granularity: AnalysisGranularity): List<TimeDataPoint> {
        val dateFormat = if (granularity == AnalysisGranularity.DAILY) {
            DateTimeFormatter.ofPattern("dd MMM", Locale.getDefault())
        } else {
            DateTimeFormatter.ofPattern("MMM yyyy", Locale.getDefault())
        }

        return transactions.groupBy { 
            val date = it.date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
            val groupDate = if (granularity == AnalysisGranularity.MONTHLY) {
                date.withDayOfMonth(1)
            } else date
            dateFormat.format(groupDate)
        }.map { (label, transList) ->
            TimeDataPoint(label, transList.sumOf { it.amount })
        }.sortedByDescending { it.label }.reversed() // Sort chronologically
    }
}
