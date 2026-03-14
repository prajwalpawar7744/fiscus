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
import java.util.*
import javax.inject.Inject

enum class AnalysisGranularity {
    DAILY, MONTHLY
}

data class TimeDataPoint(
    val label: String,
    val amount: Double
)

data class AnalysisUiState(
    val categoryBreakdown: List<CategoryAnalysis> = emptyList(),
    val totalExpense: Double = 0.0,
    val totalIncome: Double = 0.0,
    val currency: String = "USD",
    val granularity: AnalysisGranularity = AnalysisGranularity.DAILY,
    val expenseDataPoints: List<TimeDataPoint> = emptyList(),
    val incomeDataPoints: List<TimeDataPoint> = emptyList()
)

data class CategoryAnalysis(
    val category: Category,
    val amount: Double,
    val percentage: Float
)

@HiltViewModel
class AnalysisViewModel @Inject constructor(
    private val repository: BudgetRepository,
    private val preferenceManager: PreferenceManager
) : ViewModel() {

    private val _granularity = MutableStateFlow(AnalysisGranularity.DAILY)

    private val _categories = repository.getCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val uiState: StateFlow<AnalysisUiState> = combine(
        repository.getTransactions(),
        _categories,
        preferenceManager.currency,
        _granularity
    ) { transactions, categories, currency, granularity ->
        val expenses = transactions.filter { it.type == TransactionType.EXPENSE }
        val income = transactions.filter { it.type == TransactionType.INCOME }
        
        val totalExpense = expenses.sumOf { it.amount }
        val totalIncome = income.sumOf { it.amount }
        
        val categoryMap = categories.associateBy { it.id ?: 0L }
        
        val breakdown = expenses.groupBy { it.categoryId }
            .mapNotNull { (catId, transList) ->
                val category = categoryMap[catId] ?: return@mapNotNull null
                val amount = transList.sumOf { it.amount }
                CategoryAnalysis(
                    category = category,
                    amount = amount,
                    percentage = if (totalExpense > 0) (amount / totalExpense).toFloat() else 0f
                )
            }.sortedByDescending { it.amount }

        val expensePoints = aggregateByTime(expenses, granularity)
        val incomePoints = aggregateByTime(income, granularity)

        AnalysisUiState(
            categoryBreakdown = breakdown,
            totalExpense = totalExpense,
            totalIncome = totalIncome,
            currency = currency,
            granularity = granularity,
            expenseDataPoints = expensePoints,
            incomeDataPoints = incomePoints
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

    private fun aggregateByTime(transactions: List<Transaction>, granularity: AnalysisGranularity): List<TimeDataPoint> {
        val dateFormat = if (granularity == AnalysisGranularity.DAILY) {
            java.text.SimpleDateFormat("dd MMM", Locale.getDefault())
        } else {
            java.text.SimpleDateFormat("MMM yyyy", Locale.getDefault())
        }

        return transactions.groupBy { 
            val cal = Calendar.getInstance().apply { time = it.date }
            if (granularity == AnalysisGranularity.MONTHLY) {
                cal.set(Calendar.DAY_OF_MONTH, 1)
            }
            dateFormat.format(cal.time)
        }.map { (label, transList) ->
            TimeDataPoint(label, transList.sumOf { it.amount })
        }.sortedBy { it.label }
    }
}
