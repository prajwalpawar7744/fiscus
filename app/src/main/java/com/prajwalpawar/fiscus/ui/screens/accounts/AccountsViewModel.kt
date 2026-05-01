package com.prajwalpawar.fiscus.ui.screens.accounts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prajwalpawar.fiscus.data.local.pref.PreferenceManager
import com.prajwalpawar.fiscus.domain.model.Account
import com.prajwalpawar.fiscus.domain.repository.FiscusRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AccountsUiState(
    val accounts: List<Account> = emptyList(),
    val name: String = "",
    val balance: String = "",
    val icon: String = "account_balance",
    val isEditing: Boolean = false,
    val selectedAccountId: Long? = null,
    val topBarStyle: String = "standard",
    val currency: String = "USD",
    val isCompactNumberFormatEnabled: Boolean = false,
    val areAnimationsEnabled: Boolean = true
)

@HiltViewModel
class AccountsViewModel @Inject constructor(
    private val repository: FiscusRepository,
    private val preferenceManager: PreferenceManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(AccountsUiState())
    val uiState: StateFlow<AccountsUiState> = combine(
        _uiState,
        preferenceManager.topBarStyle,
        preferenceManager.currency,
        preferenceManager.isCompactNumberFormatEnabled,
        preferenceManager.areAnimationsEnabled
    ) { state, topBarStyle, currency, isCompact, areAnimationsEnabled ->
        state.copy(
            topBarStyle = topBarStyle,
            currency = currency,
            isCompactNumberFormatEnabled = isCompact,
            areAnimationsEnabled = areAnimationsEnabled
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AccountsUiState()
    )

    init {
        fetchAccounts()
    }

    private fun fetchAccounts() {
        viewModelScope.launch {
            repository.getAccounts().collect { accounts ->
                _uiState.update { it.copy(accounts = accounts) }
            }
        }
    }

    fun onNameChange(name: String) {
        _uiState.update { it.copy(name = name) }
    }

    fun onBalanceChange(balance: String) {
        val filtered = balance.filter { it.isDigit() || it == '.' }
        if (filtered.count { it == '.' } <= 1) {
            _uiState.update { it.copy(balance = filtered) }
        }
    }

    fun onIconChange(icon: String) {
        _uiState.update { it.copy(icon = icon) }
    }

    fun saveAccount() {
        val state = _uiState.value
        val balanceValue = state.balance.toDoubleOrNull() ?: 0.0
        if (state.name.isNotBlank()) {
            viewModelScope.launch {
                val account = Account(
                    id = state.selectedAccountId,
                    name = state.name,
                    balance = balanceValue,
                    icon = state.icon
                )
                if (state.selectedAccountId == null) {
                    repository.insertAccount(account)
                } else {
                    repository.updateAccount(account)
                }
                resetForm()
            }
        }
    }

    fun deleteAccount(account: Account) {
        viewModelScope.launch {
            repository.deleteAccount(account)
        }
    }

    fun selectAccountForEdit(account: Account) {
        _uiState.update {
            it.copy(
                selectedAccountId = account.id,
                name = account.name,
                balance = account.balance.toString(),
                icon = account.icon,
                isEditing = true
            )
        }
    }

    fun resetForm() {
        _uiState.update {
            it.copy(
                name = "",
                balance = "",
                icon = "account_balance",
                selectedAccountId = null,
                isEditing = false
            )
        }
    }
}
