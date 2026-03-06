package com.prajwalpawar.budgetear.ui.screens.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prajwalpawar.budgetear.domain.model.Transaction
import com.prajwalpawar.budgetear.domain.model.TransactionType
import com.prajwalpawar.budgetear.domain.repository.BudgetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

data class AddTransactionUiState(
    val title: String = "",
    val amount: String = "",
    val type: TransactionType = TransactionType.EXPENSE,
    val categoryId: Long = 1, // Default category
    val accountId: Long = 1, // Default account
    val isSaved: Boolean = false
)

@HiltViewModel
class AddTransactionViewModel @Inject constructor(
    private val repository: BudgetRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddTransactionUiState())
    val uiState: StateFlow<AddTransactionUiState> = _uiState.asStateFlow()

    fun onTitleChange(title: String) {
        _uiState.value = _uiState.value.copy(title = title)
    }

    fun onAmountChange(amount: String) {
        if (amount.isEmpty() || amount.toDoubleOrNull() != null) {
            _uiState.value = _uiState.value.copy(amount = amount)
        }
    }

    fun onTypeChange(type: TransactionType) {
        _uiState.value = _uiState.value.copy(type = type)
    }

    fun resetState() {
        _uiState.value = AddTransactionUiState()
    }

    fun saveTransaction() {
        val currentState = _uiState.value
        val amountValue = currentState.amount.toDoubleOrNull() ?: 0.0
        if (currentState.title.isNotBlank() && amountValue > 0) {
            viewModelScope.launch {
                repository.insertTransaction(
                    Transaction(
                        title = currentState.title,
                        amount = amountValue,
                        type = currentState.type,
                        categoryId = currentState.categoryId,
                        accountId = currentState.accountId,
                        date = Date()
                    )
                )
                _uiState.value = _uiState.value.copy(isSaved = true)
            }
        }
    }
}
