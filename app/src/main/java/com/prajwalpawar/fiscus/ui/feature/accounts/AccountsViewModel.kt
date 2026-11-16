package com.prajwalpawar.fiscus.ui.feature.accounts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prajwalpawar.fiscus.data.repository.AccountRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/** Screen-level state holder for the Accounts page: every account with its balance (ADR-036). */
@HiltViewModel
class AccountsViewModel @Inject constructor(
    accountRepository: AccountRepository,
) : ViewModel() {

    val uiState: StateFlow<AccountsUiState> = accountRepository.observeAccountsWithBalance()
        .map { accounts ->
            val (archived, active) = accounts.partition { it.account.archived }
            AccountsUiState(isLoading = false, active = active, archived = archived)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AccountsUiState(),
        )
}
