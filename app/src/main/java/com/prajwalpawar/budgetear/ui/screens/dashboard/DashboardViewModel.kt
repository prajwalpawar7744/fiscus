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

    private val _userPrefs = combine(
        preferenceManager.currency,
        preferenceManager.userName,
        preferenceManager.userPhotoUri
    ) { currency, name, photo ->
        Triple(currency, name, photo)
    }

    val uiState: StateFlow<DashboardUiState> = combine(
        repository.getTotalAmountByType(TransactionType.INCOME.name),
        repository.getTotalAmountByType(TransactionType.EXPENSE.name),
        repository.getRecentTransactions(5),
        _categories,
        _userPrefs
    ) { income, expense, recent, categories, prefs ->
        val (currency, name, photo) = prefs
        DashboardUiState(
            balance = income - expense,
            totalIncome = income,
            totalExpense = expense,
            recentTransactions = recent,
            categories = categories.associateBy { it.id ?: 0L },
            currency = currency,
            userName = name,
            userPhotoUri = photo
        )
    }.flowOn(Dispatchers.Default)
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardUiState()
    )
}
