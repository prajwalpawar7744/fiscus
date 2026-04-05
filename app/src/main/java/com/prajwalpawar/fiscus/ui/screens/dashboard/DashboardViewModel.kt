package com.prajwalpawar.fiscus.ui.screens.dashboard

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
import javax.inject.Inject

data class DashboardUiState(
    val balance: Double = 0.0,
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val recentTransactions: List<Transaction> = emptyList(),
    val categories: Map<Long, Category> = emptyMap(),
    val currency: String = "USD",
    val userName: String = "",
    val userPhotoUri: String? = null,
    val topBarStyle: String = "standard",
    val areAnimationsEnabled: Boolean = true
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: FiscusRepository,
    private val preferenceManager: PreferenceManager
) : ViewModel() {

    private val _categories = repository.getCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _userPrefs = combine(
        preferenceManager.currency,
        preferenceManager.userName,
        preferenceManager.userPhotoUri,
        preferenceManager.topBarStyle,
        preferenceManager.areAnimationsEnabled
    ) { currency, name, photo, style, animations ->
        DashboardUserPrefs(currency, name, photo, style, animations)
    }

    data class DashboardUserPrefs(
        val currency: String,
        val userName: String,
        val userPhotoUri: String?,
        val topBarStyle: String,
        val areAnimationsEnabled: Boolean
    )

    val uiState: StateFlow<DashboardUiState> = combine(
        repository.getTotalAmountByType(TransactionType.INCOME.name),
        repository.getTotalAmountByType(TransactionType.EXPENSE.name),
        repository.getRecentTransactions(5),
        _categories,
        _userPrefs
    ) { income, expense, recent, categories, prefs ->
        DashboardUiState(
            balance = income - expense,
            totalIncome = income,
            totalExpense = expense,
            recentTransactions = recent,
            categories = categories.associateBy { it.id ?: 0L },
            currency = prefs.currency,
            userName = prefs.userName,
            userPhotoUri = prefs.userPhotoUri,
            topBarStyle = prefs.topBarStyle,
            areAnimationsEnabled = prefs.areAnimationsEnabled
        )
    }.flowOn(Dispatchers.Default)
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardUiState()
    )
}
