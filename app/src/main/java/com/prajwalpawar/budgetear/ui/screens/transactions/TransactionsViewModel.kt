package com.prajwalpawar.budgetear.ui.screens.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prajwalpawar.budgetear.data.local.pref.PreferenceManager
import com.prajwalpawar.budgetear.domain.model.Category
import com.prajwalpawar.budgetear.domain.model.Transaction
import com.prajwalpawar.budgetear.domain.repository.BudgetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import java.time.LocalDate
import java.time.ZoneId
import java.util.*
import javax.inject.Inject

data class TransactionsUiState(
    val groupedTransactions: Map<String, Map<LocalDate, List<Transaction>>> = emptyMap(),
    val categories: Map<Long, Category> = emptyMap(),
    val currency: String = "USD"
)

@HiltViewModel
class TransactionsViewModel @Inject constructor(
    private val repository: BudgetRepository,
    private val preferenceManager: PreferenceManager
) : ViewModel() {

    private val _categories = repository.getCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val uiState: StateFlow<TransactionsUiState> = combine(
        repository.getTransactions(),
        _categories,
        preferenceManager.currency
    ) { transactions, categories, currency ->
        val sortedTransactions = transactions.sortedByDescending { it.date }
        
        // Group by Month-Year string -> Group by LocalDate -> List of Transactions
        val grouped = sortedTransactions.groupBy { transaction ->
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
            currency = currency
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TransactionsUiState()
    )
}
