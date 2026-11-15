package com.prajwalpawar.fiscus.ui.feature.transactiondetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prajwalpawar.fiscus.R
import com.prajwalpawar.fiscus.data.repository.AccountRepository
import com.prajwalpawar.fiscus.data.repository.CategoryRepository
import com.prajwalpawar.fiscus.data.repository.TransactionRepository
import com.prajwalpawar.fiscus.ui.navigation.TransactionDetailNavKey
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
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

/**
 * Screen-level state holder for one transaction. State is derived from repository streams with
 * `stateIn` (ARCHITECTURE.md), so edits made in the editor show up here automatically. Receives
 * its nav key through assisted injection (ADR-018).
 */
@HiltViewModel(assistedFactory = TransactionDetailViewModel.Factory::class)
class TransactionDetailViewModel @AssistedInject constructor(
    @Assisted private val navKey: TransactionDetailNavKey,
    private val transactionRepository: TransactionRepository,
    accountRepository: AccountRepository,
    categoryRepository: CategoryRepository,
    clock: Clock,
    timeZone: TimeZone,
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(navKey: TransactionDetailNavKey): TransactionDetailViewModel
    }

    private val showDeleteConfirmation = MutableStateFlow(false)
    private val deleted = MutableStateFlow(false)

    val uiState: StateFlow<TransactionDetailUiState> = combine(
        transactionRepository.observeTransaction(navKey.transactionId),
        accountRepository.observeAccounts(),
        categoryRepository.observeCategories(),
        showDeleteConfirmation,
        deleted,
    ) { transaction, accounts, categories, showConfirmation, isDeleted ->
        if (transaction == null) {
            TransactionDetailUiState(
                isLoading = false,
                isFinished = true,
                resultMessage = if (isDeleted) R.string.detail_result_deleted else null,
            )
        } else {
            TransactionDetailUiState(
                isLoading = false,
                transaction = transaction,
                occurredAt = transaction.occurredAt.toLocalDateTime(timeZone),
                today = clock.now().toLocalDateTime(timeZone).date,
                account = accounts.find { it.id == transaction.accountId },
                toAccount = transaction.toAccountId?.let { id -> accounts.find { it.id == id } },
                category = transaction.categoryId?.let { id -> categories.find { it.id == id } },
                showDeleteConfirmation = showConfirmation,
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = TransactionDetailUiState(),
    )

    fun onEvent(event: TransactionDetailEvent) {
        when (event) {
            TransactionDetailEvent.DeleteRequested -> showDeleteConfirmation.value = true
            TransactionDetailEvent.DeleteDismissed -> showDeleteConfirmation.value = false
            TransactionDetailEvent.DeleteConfirmed -> {
                showDeleteConfirmation.value = false
                viewModelScope.launch {
                    // Mark first, so the null transaction that follows reads as "deleted".
                    deleted.update { true }
                    transactionRepository.delete(navKey.transactionId)
                }
            }
        }
    }
}
