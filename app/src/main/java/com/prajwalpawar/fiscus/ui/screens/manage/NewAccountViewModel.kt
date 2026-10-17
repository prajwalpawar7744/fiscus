package com.prajwalpawar.fiscus.ui.screens.manage

import android.database.sqlite.SQLiteConstraintException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prajwalpawar.fiscus.data.model.Account
import com.prajwalpawar.fiscus.data.model.AccountType
import com.prajwalpawar.fiscus.data.repository.AccountRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class NewAccountUiState(
    val name: String = "",
    val type: AccountType = AccountType.BANK,
    val isDuplicate: Boolean = false,
    val isSaving: Boolean = false
) {
    val canSave: Boolean get() = name.isNotBlank() && !isSaving
}

class NewAccountViewModel(
    private val accountRepository: AccountRepository
) : ViewModel() {
    private val state = MutableStateFlow(NewAccountUiState())
    val uiState: StateFlow<NewAccountUiState> = state.asStateFlow()

    fun setName(name: String) {
        state.update { it.copy(name = name, isDuplicate = false) }
    }

    fun setType(type: AccountType) {
        state.update { it.copy(type = type) }
    }

    fun save(onSaved: (id: Long) -> Unit) {
        val current = state.value
        if (!current.canSave) return

        val name = current.name.trim()

        viewModelScope.launch {
            state.update { it.copy(isSaving = true) }

            // The database index is case-sensitive; treat "hdfc" and "HDFC" as the same.
            val exists = accountRepository.accounts.first()
                .any { it.name.equals(name, ignoreCase = true) }

            val id = if (exists) {
                null
            } else {
                try {
                    accountRepository.addAccount(Account(name = name, type = current.type))
                } catch (e: SQLiteConstraintException) {
                    // Matches an archived account with the same name.
                    null
                }
            }

            state.update { it.copy(isSaving = false, isDuplicate = id == null) }
            id?.let(onSaved)
        }
    }
}
