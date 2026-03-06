package com.prajwalpawar.budgetear.ui.screens.analysis

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prajwalpawar.budgetear.data.local.pref.PreferenceManager
import com.prajwalpawar.budgetear.domain.model.Category
import com.prajwalpawar.budgetear.domain.model.Transaction
import com.prajwalpawar.budgetear.domain.model.TransactionType
import com.prajwalpawar.budgetear.domain.repository.BudgetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

data class AnalysisUiState(
    val categoryBreakdown: List<CategoryAnalysis> = emptyList(),
    val totalExpense: Double = 0.0,
    val totalIncome: Double = 0.0,
    val currency: String = "USD"
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

    private val _categories = repository.getCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val uiState: StateFlow<AnalysisUiState> = combine(
        repository.getTransactions(),
        _categories,
        preferenceManager.currency
    ) { transactions, categories, currency ->
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

        AnalysisUiState(
            categoryBreakdown = breakdown,
            totalExpense = totalExpense,
            totalIncome = totalIncome,
            currency = currency
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AnalysisUiState()
    )
}
