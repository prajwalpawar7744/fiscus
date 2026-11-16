package com.prajwalpawar.fiscus.ui.feature.accountdetail

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prajwalpawar.fiscus.R
import com.prajwalpawar.fiscus.data.repository.AccountRepository
import com.prajwalpawar.fiscus.data.repository.TransactionRepository
import com.prajwalpawar.fiscus.ui.navigation.AccountDetailNavKey
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Screen-level state holder for one account (ADR-036/037): its balance and transaction count from
 * repository streams via `stateIn`, plus archive/unarchive and delete (only when unused).
 * Receives its nav key through assisted injection (ADR-018).
 */
@HiltViewModel(assistedFactory = AccountDetailViewModel.Factory::class)
class AccountDetailViewModel @AssistedInject constructor(
    @Assisted private val navKey: AccountDetailNavKey,
    private val accountRepository: AccountRepository,
    transactionRepository: TransactionRepository,
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(navKey: AccountDetailNavKey): AccountDetailViewModel
    }

    private val actions = MutableStateFlow(Actions())

    val uiState: StateFlow<AccountDetailUiState> = combine(
        accountRepository.observeAccountWithBalance(navKey.accountId),
        transactionRepository.observeTransactions(navKey.accountId),
        actions,
    ) { account, transactions, actions ->
        if (account == null) {
            AccountDetailUiState(
                accountId = navKey.accountId,
                isLoading = false,
                isFinished = true,
                resultMessage = if (actions.deleted) R.string.account_result_deleted else null,
            )
        } else {
            AccountDetailUiState(
                accountId = navKey.accountId,
                isLoading = false,
                account = account,
                transactionCount = transactions.size,
                showDeleteConfirmation = actions.showDeleteConfirmation,
                userMessage = actions.userMessage,
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AccountDetailUiState(accountId = navKey.accountId),
    )

    fun onEvent(event: AccountDetailEvent) {
        when (event) {
            AccountDetailEvent.ArchiveToggled -> toggleArchived()
            AccountDetailEvent.DeleteRequested ->
                actions.update { it.copy(showDeleteConfirmation = uiState.value.canDelete) }
            AccountDetailEvent.DeleteDismissed ->
                actions.update { it.copy(showDeleteConfirmation = false) }
            AccountDetailEvent.DeleteConfirmed -> delete()
            AccountDetailEvent.UserMessageShown -> actions.update { it.copy(userMessage = null) }
        }
    }

    private fun toggleArchived() {
        val account = uiState.value.account?.account ?: return
        viewModelScope.launch {
            accountRepository.setArchived(account.id, !account.archived)
            val message = if (account.archived) {
                R.string.account_result_unarchived
            } else {
                R.string.account_result_archived
            }
            actions.update { it.copy(userMessage = message) }
        }
    }

    private fun delete() {
        actions.update { it.copy(showDeleteConfirmation = false) }
        // Re-checked here: a transaction may have been added since the dialog opened.
        if (!uiState.value.canDelete) return
        viewModelScope.launch {
            // Mark first, so the missing account that follows reads as "deleted".
            actions.update { it.copy(deleted = true) }
            accountRepository.delete(navKey.accountId)
        }
    }

    /** What the user did on this page; everything else comes from the repositories. */
    private data class Actions(
        val showDeleteConfirmation: Boolean = false,
        val deleted: Boolean = false,
        @StringRes val userMessage: Int? = null,
    )
}
