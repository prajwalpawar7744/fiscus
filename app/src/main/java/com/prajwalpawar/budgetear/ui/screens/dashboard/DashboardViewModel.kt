package com.prajwalpawar.budgetear.ui.screens.dashboard

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
import javax.inject.Inject

data class DashboardUiState(
    val balance: Double = 0.0,
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val recentTransactions: List<Transaction> = emptyList(),
    val categories: Map<Long, Category> = emptyMap(),
    val currency: String = "USD",
    val userName: String = "",
    val userPhotoUri: String? = null
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: BudgetRepository,
    private val preferenceManager: PreferenceManager
) : ViewModel() {

    private val _categories = repository.getCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val uiState: StateFlow<DashboardUiState> = combine(
        repository.getTransactions(),
        _categories,
        preferenceManager.currency,
        preferenceManager.userName,
        preferenceManager.userPhotoUri
    ) { transactions, categories, currency, name, photo ->
        var income = 0.0
        var expense = 0.0
        
        for (transaction in transactions) {
            if (transaction.type == TransactionType.INCOME) {
                income += transaction.amount
            } else {
                expense += transaction.amount
            }
        }
        
        DashboardUiState(
            balance = income - expense,
            totalIncome = income,
            totalExpense = expense,
            recentTransactions = transactions.take(5),
            categories = categories.associateBy { it.id ?: 0L },
            currency = currency,
            userName = name,
            userPhotoUri = photo
        )
    }.flowOn(Dispatchers.IO)
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardUiState()
    )
}
