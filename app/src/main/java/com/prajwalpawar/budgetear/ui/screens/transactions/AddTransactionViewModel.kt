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
import com.prajwalpawar.budgetear.domain.model.Category
import kotlinx.coroutines.flow.*
import java.util.Date
import javax.inject.Inject

data class AddTransactionUiState(
    val title: String = "",
    val amount: String = "",
    val note: String = "",
    val type: TransactionType = TransactionType.EXPENSE,
    val categoryId: Long? = null,
    val categories: List<Category> = emptyList(),
    val accountId: Long = 1, // Default account
    val isSaved: Boolean = false,
    val transactionId: Long? = null
)

@HiltViewModel
class AddTransactionViewModel @Inject constructor(
    private val repository: BudgetRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddTransactionUiState())
    private val _allCategories = MutableStateFlow<List<Category>>(emptyList())

    val uiState: StateFlow<AddTransactionUiState> = combine(
        _uiState,
        _allCategories
    ) { state, allCategories ->
        state.copy(
            categories = allCategories.filter { it.type == null || it.type == state.type }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AddTransactionUiState())

    init {
        fetchCategories()
    }

    private fun fetchCategories() {
        viewModelScope.launch {
            repository.getCategories().collect { categories ->
                if (categories.isEmpty()) {
                    seedDefaultCategories()
                } else {
                    _allCategories.value = categories
                    _uiState.update { currentState ->
                        val filtered = categories.filter { it.type == null || it.type == currentState.type }
                        currentState.copy(
                            categoryId = currentState.categoryId ?: filtered.firstOrNull()?.id
                        )
                    }
                }
            }
        }
    }

    private suspend fun seedDefaultCategories() {
        val defaults = listOf(
            // Expenses
            Category(name = "Food", icon = "restaurant", color = 0xFF4CAF50.toInt(), type = TransactionType.EXPENSE),
            Category(name = "Travel", icon = "directions_car", color = 0xFF2196F3.toInt(), type = TransactionType.EXPENSE),
            Category(name = "Education", icon = "school", color = 0xFFFFC107.toInt(), type = TransactionType.EXPENSE),
            Category(name = "Shopping", icon = "shopping_cart", color = 0xFF9C27B0.toInt(), type = TransactionType.EXPENSE),
            Category(name = "Health", icon = "medical_services", color = 0xFFF44336.toInt(), type = TransactionType.EXPENSE),
            Category(name = "Entertainment", icon = "movie", color = 0xFFE91E63.toInt(), type = TransactionType.EXPENSE),
            Category(name = "Other", icon = "category", color = 0xFF9E9E9E.toInt(), type = TransactionType.EXPENSE),
            
            // Income
            Category(name = "Salary", icon = "payments", color = 0xFF00BCD4.toInt(), type = TransactionType.INCOME),
            Category(name = "Freelance", icon = "work", color = 0xFF4CAF50.toInt(), type = TransactionType.INCOME),
            Category(name = "Gift", icon = "redeem", color = 0xFFFF9800.toInt(), type = TransactionType.INCOME),
            Category(name = "Investment", icon = "trending_up", color = 0xFF8BC34A.toInt(), type = TransactionType.INCOME),
            Category(name = "Other", icon = "category", color = 0xFF9E9E9E.toInt(), type = TransactionType.INCOME)
        )
        defaults.forEach { repository.insertCategory(it) }
    }

    fun onTitleChange(title: String) {
        _uiState.update { it.copy(title = title) }
    }

    fun onAmountChange(amount: String) {
        if (amount.isEmpty() || amount.toDoubleOrNull() != null) {
            _uiState.update { it.copy(amount = amount) }
        }
    }

    fun onNoteChange(note: String) {
        _uiState.update { it.copy(note = note) }
    }

    fun onCategoryChange(categoryId: Long) {
        _uiState.update { it.copy(categoryId = categoryId) }
    }

    fun onTypeChange(type: TransactionType) {
        _uiState.update { currentState ->
            val filtered = _allCategories.value.filter { it.type == null || it.type == type }
            currentState.copy(
                type = type,
                categoryId = filtered.firstOrNull()?.id
            )
        }
    }

    fun resetState() {
        val type = _uiState.value.type
        val filtered = _allCategories.value.filter { it.type == null || it.type == type }
        _uiState.value = AddTransactionUiState(
            type = type,
            categoryId = filtered.firstOrNull()?.id
        )
    }

    fun setTransactionForEdit(transaction: Transaction) {
        _uiState.update {
            it.copy(
                transactionId = transaction.id,
                title = transaction.title,
                amount = transaction.amount.toString(),
                note = transaction.note,
                type = transaction.type,
                categoryId = transaction.categoryId,
                accountId = transaction.accountId
            )
        }
    }

    fun saveTransaction() {
        val currentState = _uiState.value
        val amountValue = currentState.amount.toDoubleOrNull() ?: 0.0
        if (currentState.title.isNotBlank() && amountValue > 0) {
            viewModelScope.launch {
                val transaction = Transaction(
                    id = currentState.transactionId,
                    title = currentState.title,
                    amount = amountValue,
                    type = currentState.type,
                    categoryId = currentState.categoryId ?: 1L,
                    accountId = currentState.accountId,
                    date = Date(),
                    note = currentState.note
                )
                if (currentState.transactionId == null) {
                    repository.insertTransaction(transaction)
                } else {
                    repository.updateTransaction(transaction)
                }
                _uiState.update { it.copy(isSaved = true) }
            }
        }
    }

    fun deleteTransaction() {
        val currentState = _uiState.value
        if (currentState.transactionId != null) {
            viewModelScope.launch {
                repository.deleteTransaction(
                    Transaction(
                        id = currentState.transactionId,
                        title = currentState.title,
                        amount = currentState.amount.toDoubleOrNull() ?: 0.0,
                        type = currentState.type,
                        categoryId = currentState.categoryId ?: 1L,
                        accountId = currentState.accountId,
                        date = Date(),
                        note = currentState.note
                    )
                )
                _uiState.update { it.copy(isSaved = true) }
            }
        }
    }
}
