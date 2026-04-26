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
import com.prajwalpawar.fiscus.domain.model.TransactionSubItem

data class AddTransactionUiState(
    val title: String = "",
    val amount: String = "",
    val note: String = "",
    val type: TransactionType = TransactionType.EXPENSE,
    val categoryId: Long? = null,
    val categories: List<Category> = emptyList(),
    val accountId: Long? = null,
    val toAccountId: Long? = null,
    val accounts: List<Account> = emptyList(),
    val isSaved: Boolean = false,
    val transactionId: Long? = null,
    val date: Date = Date(),
    val areAnimationsEnabled: Boolean = true,
    val subItems: List<TransactionSubItem> = emptyList(),
    val isBreakdownEnabled: Boolean = false
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
        val filtered = allCategories.filter { it.type == null || it.type == state.type }
        val sorted = filtered.sortedWith(
            compareBy<Category> { it.name == "Other" }
                .thenBy { it.id ?: Long.MAX_VALUE }
        )
        state.copy(
            categories = sorted,
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
                        val filtered =
                            _allCategories.value.filter { it.type == null || it.type == currentState.type }
                        currentState.copy(
                            categoryId = currentState.categoryId
                                ?: filtered.firstOrNull { it.name == "Transfer" || it.type == currentState.type }?.id
                                ?: filtered.firstOrNull()?.id
                        )
                    }
                }
            }
        }
    }

    private suspend fun seedDefaultCategories() {
        val defaults = listOf(
            // Expenses
            Category(
                name = "Food",
                icon = "restaurant",
                color = 0xFF4CAF50.toInt(),
                type = TransactionType.EXPENSE,
                isSystem = true
            ),
            Category(
                name = "Travel",
                icon = "directions_car",
                color = 0xFF2196F3.toInt(),
                type = TransactionType.EXPENSE,
                isSystem = true
            ),
            Category(
                name = "Education",
                icon = "school",
                color = 0xFFFFC107.toInt(),
                type = TransactionType.EXPENSE,
                isSystem = true
            ),
            Category(
                name = "Shopping",
                icon = "shopping_cart",
                color = 0xFF9C27B0.toInt(),
                type = TransactionType.EXPENSE,
                isSystem = true
            ),
            Category(
                name = "Health",
                icon = "medical_services",
                color = 0xFFF44336.toInt(),
                type = TransactionType.EXPENSE,
                isSystem = true
            ),
            Category(
                name = "Bills",
                icon = "receipt_long",
                color = 0xFFFF5722.toInt(),
                type = TransactionType.EXPENSE,
                isSystem = true
            ),
            Category(
                name = "Entertainment",
                icon = "movie",
                color = 0xFFE91E63.toInt(),
                type = TransactionType.EXPENSE,
                isSystem = true
            ),
            Category(
                name = "Other",
                icon = "category",
                color = 0xFF9E9E9E.toInt(),
                type = TransactionType.EXPENSE,
                isSystem = true
            ),

            // Income
            Category(
                name = "Salary",
                icon = "payments",
                color = 0xFF00BCD4.toInt(),
                type = TransactionType.INCOME,
                isSystem = true
            ),
            Category(
                name = "Freelance",
                icon = "work",
                color = 0xFF4CAF50.toInt(),
                type = TransactionType.INCOME,
                isSystem = true
            ),
            Category(
                name = "Gift",
                icon = "redeem",
                color = 0xFFFF9800.toInt(),
                type = TransactionType.INCOME,
                isSystem = true
            ),
            Category(
                name = "Investment",
                icon = "trending_up",
                color = 0xFF8BC34A.toInt(),
                type = TransactionType.INCOME,
                isSystem = true
            ),
            Category(
                name = "Other",
                icon = "category",
                color = 0xFF9E9E9E.toInt(),
                type = TransactionType.INCOME,
                isSystem = true
            ),

            // Transfer
            Category(
                name = "Transfer",
                icon = "swap_horiz",
                color = 0xFF607D8B.toInt(),
                type = TransactionType.TRANSFER,
                isSystem = true
            )
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

    fun onToAccountChange(toAccountId: Long) {
        _uiState.update { it.copy(toAccountId = toAccountId) }
    }

    fun onCategoryChange(categoryId: Long) {
        _uiState.update { it.copy(categoryId = categoryId) }
    }

    fun toggleBreakdown(enabled: Boolean) {
        _uiState.update { state ->
            val totalAmount = state.subItems.sumOf { it.amount }
            state.copy(
                isBreakdownEnabled = enabled,
                amount = if (enabled && totalAmount > 0) totalAmount.toString() else state.amount
            )
        }
    }

    fun addSubItem() {
        _uiState.update { state ->
            state.copy(
                subItems = state.subItems + TransactionSubItem(name = "", amount = 0.0)
            )
        }
    }

    fun removeSubItem(index: Int) {
        _uiState.update { state ->
            val newList = state.subItems.toMutableList()
            if (index in newList.indices) {
                newList.removeAt(index)
            }
            val totalAmount = newList.sumOf { it.amount }
            state.copy(
                subItems = newList,
                amount = if (state.isBreakdownEnabled) totalAmount.toString() else state.amount
            )
        }
    }

    fun onSubItemNameChange(index: Int, name: String) {
        _uiState.update { state ->
            val newList = state.subItems.toMutableList()
            if (index in newList.indices) {
                newList[index] = newList[index].copy(name = name)
            }
            state.copy(subItems = newList)
        }
    }

    fun onSubItemAmountChange(index: Int, amountStr: String) {
        val amount = amountStr.toDoubleOrNull() ?: 0.0
        _uiState.update { state ->
            val newList = state.subItems.toMutableList()
            if (index in newList.indices) {
                newList[index] = newList[index].copy(amount = amount)
            }
            // Auto-update total amount if breakdown is enabled
            val totalAmount = newList.sumOf { it.amount }
            state.copy(
                subItems = newList,
                amount = if (state.isBreakdownEnabled) totalAmount.toString() else state.amount
            )
        }
    }

    fun onTypeChange(type: TransactionType) {
        _uiState.update { currentState ->
            val filtered = _allCategories.value.filter { it.type == null || it.type == type }
            currentState.copy(
                type = type,
                categoryId = filtered.find { it.name == "Transfer" }?.id
                    ?: filtered.firstOrNull()?.id,
                toAccountId = if (type == TransactionType.TRANSFER) {
                    _allAccounts.value.firstOrNull { it.id != currentState.accountId }?.id
                } else null,
                isBreakdownEnabled = if (type == TransactionType.TRANSFER) false else currentState.isBreakdownEnabled
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
                toAccountId = transaction.toAccountId,
                date = transaction.date,
                subItems = transaction.subItems,
                isBreakdownEnabled = transaction.subItems.isNotEmpty(),
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
                    toAccountId = currentState.toAccountId,
                    date = currentState.date,
                    note = currentState.note,
                    subItems = if (currentState.isBreakdownEnabled) currentState.subItems.filter { it.name.isNotBlank() && it.amount > 0 } else emptyList()
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
                        toAccountId = currentState.toAccountId,
                        date = currentState.date,
                        note = currentState.note
                    )
                )
                _uiState.update { it.copy(isSaved = true) }
            }
        }
    }
}
