package com.prajwalpawar.fiscus.ui.screens.manage

import android.database.sqlite.SQLiteConstraintException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prajwalpawar.fiscus.data.model.Account
import com.prajwalpawar.fiscus.data.model.AccountType
import com.prajwalpawar.fiscus.data.repository.AccountRepository
import com.prajwalpawar.fiscus.ui.screens.transaction.amountToMinorUnits
import com.prajwalpawar.fiscus.ui.screens.transaction.isAmountInput
import com.prajwalpawar.fiscus.ui.screens.transaction.minorUnitsToAmountInput
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AccountEditorUiState(
    val name: String = "",
    val type: AccountType = AccountType.BANK,
    // Typed text; a leading "-" means money owed (e.g. a credit card's existing debt).
    val openingBalance: String = "",
    // The account new transactions start with. Turned off only by making another the default.
    val isDefault: Boolean = false,
    // False while this is already the default, or archived.
    val canChangeDefault: Boolean = true,
    val isDuplicate: Boolean = false,
    val isSaving: Boolean = false,
    // False only while an existing account is being loaded.
    val isLoaded: Boolean = true,
    // Null when creating a new account.
    val manage: ItemManageState? = null
) {
    val isEditing: Boolean get() = manage != null
    val canSave: Boolean get() = isLoaded && name.isNotBlank() && !isSaving
}

// Creates an account, or edits one when [editingId] is set.
class AccountEditorViewModel(
    private val accountRepository: AccountRepository,
    private val editingId: Long?
) : ViewModel() {
    private val state = MutableStateFlow(AccountEditorUiState(isLoaded = editingId == null))
    val uiState: StateFlow<AccountEditorUiState> = state.asStateFlow()

    private var original: Account? = null

    init {
        if (editingId != null) {
            viewModelScope.launch { load(editingId) }
        }
    }

    private suspend fun load(id: Long) {
        val account = accountRepository.getAccount(id) ?: return
        original = account

        state.value = AccountEditorUiState(
            name = account.name,
            type = account.type,
            openingBalance = if (account.openingBalance == 0L) "" else minorUnitsToAmountInput(account.openingBalance),
            isDefault = account.isDefault,
            canChangeDefault = !account.isDefault && !account.isArchived,
            isLoaded = true,
            manage = ItemManageState(
                isArchived = account.isArchived,
                isDefault = account.isDefault,
                usageCount = accountRepository.usageCount(id),
                canArchive = !account.isDefault && accountRepository.activeCount() > 1,
                archiveBlockedHint = if (account.isDefault) {
                    "This is your default account. Make another account the default to archive it."
                } else {
                    null
                }
            )
        )
    }

    fun setName(name: String) {
        state.update { it.copy(name = name, isDuplicate = false) }
    }

    fun setOpeningBalance(value: String) {
        if (isAmountInput(value.removePrefix("-"))) {
            state.update { it.copy(openingBalance = value) }
        }
    }

    fun setDefault(isDefault: Boolean) {
        if (state.value.canChangeDefault) {
            state.update { it.copy(isDefault = isDefault) }
        }
    }

    fun setType(type: AccountType) {
        state.update { it.copy(type = type) }
    }

    fun save(onSaved: (id: Long) -> Unit) {
        val current = state.value
        if (!current.canSave) return

        val name = current.name.trim()
        val openingBalance = amountToMinorUnits(current.openingBalance) ?: 0L

        viewModelScope.launch {
            state.update { it.copy(isSaving = true) }

            // The database index is case-sensitive; treat "hdfc" and "HDFC" as the same.
            val exists = accountRepository.accounts.first()
                .any { it.id != editingId && it.name.equals(name, ignoreCase = true) }

            val id = if (exists) {
                null
            } else {
                try {
                    val existing = original
                    if (existing != null) {
                        accountRepository.updateAccount(
                            existing.copy(
                                name = name,
                                type = current.type,
                                openingBalance = openingBalance
                            )
                        )
                        existing.id
                    } else {
                        accountRepository.addAccount(Account(name = name, type = current.type, openingBalance = openingBalance))
                    }
                } catch (e: SQLiteConstraintException) {
                    // Matches an archived account with the same name.
                    null
                }
            }

            if (id != null && current.isDefault && original?.isDefault != true) {
                accountRepository.setDefault(id)
            }

            state.update { it.copy(isSaving = false, isDuplicate = id == null) }
            id?.let(onSaved)
        }
    }

    fun toggleArchived(onDone: () -> Unit) {
        val id = editingId ?: return
        val manage = state.value.manage ?: return

        viewModelScope.launch {
            accountRepository.setArchived(id, !manage.isArchived)
            onDone()
        }
    }

    fun delete(onDone: () -> Unit) {
        val id = editingId ?: return

        viewModelScope.launch {
            accountRepository.deleteAccount(id)
            onDone()
        }
    }
}
