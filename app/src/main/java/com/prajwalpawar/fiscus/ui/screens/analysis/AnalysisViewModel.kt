package com.prajwalpawar.fiscus.ui.screens.analysis

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prajwalpawar.fiscus.data.local.pref.PreferenceManager
import com.prajwalpawar.fiscus.domain.model.Category
import com.prajwalpawar.fiscus.domain.model.Transaction
import com.prajwalpawar.fiscus.domain.model.TransactionType
import com.prajwalpawar.fiscus.domain.repository.FiscusRepository
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
    BAR, LINE, PIE, HEATMAP
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
    val allAccounts: com.prajwalpawar.fiscus.domain.model.Account? = null, // Wait, I need list of accounts
    val selectedCategoryId: Long? = null,
    val selectedAccountId: Long? = null,
    val allAccountsList: List<com.prajwalpawar.fiscus.domain.model.Account> = emptyList(),
    val selectedChartType: AnalysisChartType = AnalysisChartType.BAR,
    val areAnimationsEnabled: Boolean = true,
    val topBarStyle: String = "standard",
    val activityPoints: List<ActivityPoint> = emptyList(),
    val effectiveStartDate: LocalDate? = null,
    val effectiveEndDate: LocalDate? = null,
    val isPrivacyModeEnabled: Boolean = false
)

data class ActivityPoint(
    val date: LocalDate,
    val intensity: Float // 0.0 to 1.0
)

data class CategoryAnalysis(
    val category: Category,
    val amount: Double,
    val percentage: Float,
    val transactionCount: Int
)

@HiltViewModel
class AnalysisViewModel @Inject constructor(
    private val repository: FiscusRepository,
    private val preferenceManager: PreferenceManager
) : ViewModel() {

    private val _granularity = MutableStateFlow(AnalysisGranularity.DAILY)
    private val _selectedTimeRange = MutableStateFlow(TimeRange.ALL)
    private val _startDate = MutableStateFlow<LocalDate?>(null)
    private val _endDate = MutableStateFlow<LocalDate?>(null)
    private val _selectedTransactionType = MutableStateFlow<TransactionType?>(TransactionType.EXPENSE)
    private val _selectedCategoryId = MutableStateFlow<Long?>(null)
    private val _selectedAccountId = MutableStateFlow<Long?>(null)
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
        _selectedAccountId,
        _selectedChartType
    ) { params ->
        FilterParams(
            granularity = params[0] as AnalysisGranularity,
            timeRange = params[1] as TimeRange,
            startDate = params[2] as? LocalDate,
            endDate = params[3] as? LocalDate,
            type = params[4] as? TransactionType,
            categoryId = params[5] as? Long,
            accountId = params[6] as? Long,
            chartType = params[7] as AnalysisChartType
        )
    }

    private data class FilterParams(
        val granularity: AnalysisGranularity,
        val timeRange: TimeRange,
        val startDate: LocalDate?,
        val endDate: LocalDate?,
        val type: TransactionType?,
        val categoryId: Long?,
        val accountId: Long?,
        val chartType: AnalysisChartType
    )

    private val _accounts = repository.getAccounts()
        .distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val uiState: StateFlow<AnalysisUiState> = combine<Any?, AnalysisUiState>(
        repository.getTransactions(),
        _categories,
        _accounts,
        preferenceManager.currency,
        _filters,
        preferenceManager.areAnimationsEnabled,
        preferenceManager.topBarStyle,
        preferenceManager.isPrivacyModeEnabled
    ) { args ->
        val transactions = args[0] as List<Transaction>
        val categories = args[1] as List<Category>
        val accounts = args[2] as List<com.prajwalpawar.fiscus.domain.model.Account>
        val currency = args[3] as String
        val filters = args[4] as FilterParams
        val animationsEnabled = args[5] as Boolean
        val topBarStyle = args[6] as String
        val privacyEnabled = args[7] as Boolean
        
        val timeFilteredTransactions = transactions.filter { transaction ->
            val transactionDate = transaction.date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
            val now = LocalDate.now()
            when (filters.timeRange) {
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
        }
        
        val filteredTransactions = timeFilteredTransactions.filter { transaction ->
            val matchesType = filters.type == null || transaction.type == filters.type
            val matchesCategory = filters.categoryId == null || transaction.categoryId == filters.categoryId
            val matchesAccount = filters.accountId == null || transaction.accountId == filters.accountId || transaction.toAccountId == filters.accountId
            matchesType && matchesCategory && matchesAccount
        }
        
        // Final filter for period summary (includes account/category but maybe not type if summary shows both?)
        // Actually, period summary usually shows TOTAL income/expense for the selected filters.
        val summaryTransactions = timeFilteredTransactions.filter { transaction ->
            val matchesCategory = filters.categoryId == null || transaction.categoryId == filters.categoryId
            val matchesAccount = filters.accountId == null || transaction.accountId == filters.accountId || transaction.toAccountId == filters.accountId
            matchesCategory && matchesAccount
        }
        
        val now = LocalDate.now()
        val effectiveStart = when (filters.timeRange) {
            TimeRange.TODAY -> now
            TimeRange.THIS_WEEK -> now.minusDays(now.dayOfWeek.value.toLong() - 1)
            TimeRange.THIS_MONTH -> now.withDayOfMonth(1)
            TimeRange.THIS_YEAR -> now.withDayOfYear(1)
            TimeRange.CUSTOM -> filters.startDate ?: now.minusDays(30)
            TimeRange.ALL -> transactions.minOfOrNull { it.date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate() } ?: now.minusDays(30)
        }
        val effectiveEnd = when (filters.timeRange) {
            TimeRange.CUSTOM -> filters.endDate ?: now
            else -> now
        }
        
        val expenses = filteredTransactions.filter { it.type == TransactionType.EXPENSE }
        val income = filteredTransactions.filter { it.type == TransactionType.INCOME }
        
        val totalExpense = summaryTransactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
        val totalIncome = summaryTransactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
        
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

        val expensePoints = aggregateByTime(expenses, filters.granularity, filters.timeRange, filters.startDate, filters.endDate)
        val incomePoints = aggregateByTime(income, filters.granularity, filters.timeRange, filters.startDate, filters.endDate)

        // Activity points for Heatmap (frequency of transactions)
        val activityMap = filteredTransactions.groupBy { 
            it.date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate() 
        }
        val maxActivity = activityMap.values.maxOfOrNull { it.size } ?: 1
        val activityPoints = activityMap.map { (date, list) ->
            ActivityPoint(date, list.size.toFloat() / maxActivity)
        }

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
            selectedAccountId = filters.accountId,
            allAccountsList = accounts,
            selectedChartType = filters.chartType,
            areAnimationsEnabled = animationsEnabled,
            topBarStyle = topBarStyle,
            activityPoints = activityPoints,
            effectiveStartDate = effectiveStart,
            effectiveEndDate = effectiveEnd,
            isPrivacyModeEnabled = privacyEnabled
        )
    }.flowOn(Dispatchers.Default)
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
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

    fun onAccountSelected(accountId: Long?) {
        _selectedAccountId.value = accountId
    }

    fun onChartTypeSelected(chartType: AnalysisChartType) {
        _selectedChartType.value = chartType
    }

    private fun aggregateByTime(
        transactions: List<Transaction>,
        granularity: AnalysisGranularity,
        timeRange: TimeRange,
        startDate: LocalDate?,
        endDate: LocalDate?
    ): List<TimeDataPoint> {
        val zoneId = ZoneId.systemDefault()
        
        val dateFormat = if (granularity == AnalysisGranularity.DAILY) {
            DateTimeFormatter.ofPattern("dd MMM", Locale.getDefault())
        } else {
            DateTimeFormatter.ofPattern("MMM yyyy", Locale.getDefault())
        }

        return transactions.groupBy { 
            val date = it.date.toInstant().atZone(zoneId).toLocalDate()
            if (granularity == AnalysisGranularity.MONTHLY) {
                date.withDayOfMonth(1)
            } else date
        }.map { (date, transList) ->
            date to TimeDataPoint(dateFormat.format(date), transList.sumOf { it.amount })
        }.sortedBy { it.first }
        .map { it.second }
    }
}
