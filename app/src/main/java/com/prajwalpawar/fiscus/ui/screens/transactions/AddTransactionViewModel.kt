package com.prajwalpawar.fiscus.ui.screens.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prajwalpawar.fiscus.domain.model.Transaction
import com.prajwalpawar.fiscus.domain.model.TransactionType
import com.prajwalpawar.fiscus.domain.repository.FiscusRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.prajwalpawar.fiscus.domain.model.Category
import kotlinx.coroutines.flow.*
import com.prajwalpawar.fiscus.data.local.pref.PreferenceManager
import java.util.Date
import javax.inject.Inject
import com.prajwalpawar.fiscus.domain.model.Account

data class AddTransactionUiState(
    val title: String = "",
    val amount: String = "",
    val note: String = "",
    val type: TransactionType = TransactionType.EXPENSE,
    val categoryId: Long? = null,
    val categories: List<Category> = emptyList(),
    val accountId: Long? = null,
    val accounts: List<Account> = emptyList(),
    val isSaved: Boolean = false,
    val transactionId: Long? = null,
    val date: Date = Date(),
    val areAnimationsEnabled: Boolean = true
)

@HiltViewModel
class AddTransactionViewModel @Inject constructor(
    private val repository: FiscusRepository,
    private val preferenceManager: PreferenceManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddTransactionUiState())
    private val _allCategories = MutableStateFlow<List<Category>>(emptyList())

    private val _allAccounts = MutableStateFlow<List<Account>>(emptyList())

    val uiState: StateFlow<AddTransactionUiState> = combine(
        _uiState,
        _allCategories,
        _allAccounts,
        preferenceManager.areAnimationsEnabled
    ) { state, allCategories, allAccounts, animationsEnabled ->
        state.copy(
            categories = allCategories.filter { it.type == null || it.type == state.type },
            accounts = allAccounts,
            areAnimationsEnabled = animationsEnabled
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AddTransactionUiState())

    init {
        fetchCategories()
        fetchAccounts()
    }

    private fun fetchCategories() {
        viewModelScope.launch {
            repository.getCategories().collect { categories ->
                _allCategories.value = categories.distinctBy { it.name + it.type?.name }
                
                if (categories.isEmpty()) {
                    seedDefaultCategories()
                } else {
                    _uiState.update { currentState ->
                        val filtered = _allCategories.value.filter { it.type == null || it.type == currentState.type }
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

    private fun fetchAccounts() {
        viewModelScope.launch {
            repository.getAccounts().collect { accounts ->
                _allAccounts.value = accounts
                
                _uiState.update { currentState ->
                    currentState.copy(
                        accountId = currentState.accountId ?: accounts.firstOrNull()?.id
                    )
                }
            }
        }
    }

    fun onTitleChange(title: String) {
        _uiState.update { it.copy(title = title) }
    }

    fun onAmountChange(amount: String) {
        // Only allow digits and a single decimal point
        val filteredAmount = amount.filter { it.isDigit() || it == '.' }
        val dotCount = filteredAmount.count { it == '.' }
        
        if (dotCount <= 1) {
            _uiState.update { it.copy(amount = filteredAmount) }
        }
    }

    fun onNoteChange(note: String) {
        _uiState.update { it.copy(note = note) }
    }

    fun onAccountChange(accountId: Long) {
        _uiState.update { it.copy(accountId = accountId) }
    }

    fun onDateChange(date: Date) {
        _uiState.update { it.copy(date = date) }
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
                accountId = transaction.accountId,
                date = transaction.date,
                isSaved = false
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
                    categoryId = currentState.categoryId!!,
                    accountId = currentState.accountId!!,
                    date = currentState.date,
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
                        categoryId = currentState.categoryId!!,
                        accountId = currentState.accountId!!,
                        date = currentState.date,
                        note = currentState.note
                    )
                )
                _uiState.update { it.copy(isSaved = true) }
            }
        }
    }
}
