package com.prajwalpawar.budgetear.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prajwalpawar.budgetear.domain.model.Transaction
import com.prajwalpawar.budgetear.domain.repository.BudgetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import javax.inject.Inject

data class DashboardUiState(
    val balance: Double = 0.0,
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val recentTransactions: List<Transaction> = emptyList()
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: BudgetRepository
) : ViewModel() {

    val uiState: StateFlow<DashboardUiState> = repository.getTransactions()
        .flowOn(Dispatchers.IO)
        .map { transactions ->
            var income = 0.0
            var expense = 0.0
            
            for (transaction in transactions) {
                if (transaction.type.name == "INCOME") {
                    income += transaction.amount
                } else {
                    expense += transaction.amount
                }
            }
            
            DashboardUiState(
                balance = income - expense,
                totalIncome = income,
                totalExpense = expense,
                recentTransactions = transactions.take(5)
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DashboardUiState()
        )
}
