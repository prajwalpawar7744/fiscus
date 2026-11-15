package com.prajwalpawar.fiscus.ui.feature.transactioneditor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prajwalpawar.fiscus.R
import com.prajwalpawar.fiscus.core.money.formatMoneyInput
import com.prajwalpawar.fiscus.core.money.isValidAmountInput
import com.prajwalpawar.fiscus.core.money.parseMoneyInput
import com.prajwalpawar.fiscus.data.repository.AccountRepository
import com.prajwalpawar.fiscus.data.repository.CategoryRepository
import com.prajwalpawar.fiscus.data.repository.TransactionRepository
import com.prajwalpawar.fiscus.model.CategoryKind
import com.prajwalpawar.fiscus.model.Transaction
import com.prajwalpawar.fiscus.model.TransactionType
import com.prajwalpawar.fiscus.ui.navigation.TransactionEditorNavKey
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

/**
 * Screen-level state holder for adding or editing a transaction. Receives its
 * [TransactionEditorNavKey] through assisted injection (ADR-018).
 */
@HiltViewModel(assistedFactory = TransactionEditorViewModel.Factory::class)
class TransactionEditorViewModel @AssistedInject constructor(
    @Assisted private val navKey: TransactionEditorNavKey,
    private val transactionRepository: TransactionRepository,
    private val accountRepository: AccountRepository,
    private val categoryRepository: CategoryRepository,
    private val clock: Clock,
    private val timeZone: TimeZone,
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(navKey: TransactionEditorNavKey): TransactionEditorViewModel
    }

    private val _uiState = MutableStateFlow(TransactionEditorUiState())
    val uiState: StateFlow<TransactionEditorUiState> = _uiState.asStateFlow()

    /** The transaction being edited, or null when adding. */
    private var original: Transaction? = null

    init {
        viewModelScope.launch {
            if (!loadInitialForm()) return@launch
            launch { observeAccounts() }
            launch { observeCategories() }
        }
    }

    fun onEvent(event: TransactionEditorEvent) {
        when (event) {
            is TransactionEditorEvent.TypeChanged -> _uiState.update { state ->
                state.copy(type = event.type, categoryId = null).withDefaultToAccount()
            }
            is TransactionEditorEvent.AmountChanged -> {
                if (!isValidAmountInput(event.text)) return
                _uiState.update { it.copy(amountText = event.text, amountError = null) }
            }
            is TransactionEditorEvent.AccountSelected -> _uiState.update { state ->
                state.copy(accountId = event.accountId).withDefaultToAccount()
            }
            is TransactionEditorEvent.ToAccountSelected ->
                _uiState.update { it.copy(toAccountId = event.accountId) }
            is TransactionEditorEvent.CategorySelected ->
                _uiState.update { it.copy(categoryId = event.categoryId) }
            is TransactionEditorEvent.DateSelected -> _uiState.update { it.copy(date = event.date) }
            is TransactionEditorEvent.NoteChanged -> _uiState.update { it.copy(note = event.note) }
            TransactionEditorEvent.Save -> save()
        }
    }

    /** Fills the form from the nav key; returns false when the edited transaction is gone. */
    private suspend fun loadInitialForm(): Boolean {
        val id = navKey.transactionId
        val existing = id?.let { transactionRepository.observeTransaction(it).first() }
        if (id != null && existing == null) {
            _uiState.update { it.copy(isFinished = true) }
            return false
        }
        original = existing
        _uiState.update { state ->
            if (existing == null) {
                state.copy(type = navKey.type, date = clock.now().toLocalDateTime(timeZone).date)
            } else {
                state.copy(
                    isNew = false,
                    type = existing.type,
                    amountText = formatMoneyInput(existing.amount),
                    accountId = existing.accountId,
                    toAccountId = existing.toAccountId,
                    categoryId = existing.categoryId,
                    date = existing.occurredAt.toLocalDateTime(timeZone).date,
                    note = existing.note,
                )
            }
        }
        return true
    }

    private suspend fun observeAccounts() {
        accountRepository.observeAccounts().collect { accounts ->
            _uiState.update { state ->
                // Archived accounts stay selectable only if this transaction already uses them.
                val usable = accounts.filter {
                    !it.archived || it.id == state.accountId || it.id == state.toAccountId
                }
                val accountId = state.accountId?.takeIf { id -> usable.any { it.id == id } }
                    ?: usable.firstOrNull()?.id
                state.copy(isLoading = false, accounts = usable, accountId = accountId)
                    .withDefaultToAccount()
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class) // flatMapLatest
    private suspend fun observeCategories() {
        _uiState.map { it.type }
            .distinctUntilChanged()
            .flatMapLatest { type ->
                type.categoryKind?.let(categoryRepository::observeActiveCategories)
                    ?: flowOf(emptyList())
            }
            .collect { categories ->
                _uiState.update { state ->
                    state.copy(
                        categories = categories,
                        categoryId = state.categoryId?.takeIf { id -> categories.any { it.id == id } },
                    )
                }
            }
    }

    private fun save() {
        val state = _uiState.value
        val account = state.selectedAccount ?: return
        val date = state.date ?: return
        val amount = parseMoneyInput(state.amountText, account.currencyCode)
        if (amount == null) {
            _uiState.update { it.copy(amountError = R.string.editor_amount_error) }
            return
        }
        if (!state.canSave) return

        _uiState.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            val now = clock.now()
            // Keep the original time of day when editing; new entries use the current time.
            val time: LocalTime = (original?.occurredAt ?: now).toLocalDateTime(timeZone).time
            val isTransfer = state.type == TransactionType.TRANSFER
            val transaction = Transaction(
                id = original?.id ?: 0L,
                type = state.type,
                amount = amount,
                accountId = account.id,
                toAccountId = if (isTransfer) state.toAccountId else null,
                categoryId = if (isTransfer) null else state.categoryId,
                note = state.note.trim(),
                occurredAt = LocalDateTime(date, time).toInstant(timeZone),
                createdAt = original?.createdAt ?: now,
                updatedAt = now,
            )
            transactionRepository.save(transaction)
            val message = when {
                original != null -> R.string.editor_result_updated
                state.type == TransactionType.EXPENSE -> R.string.editor_result_expense_added
                state.type == TransactionType.INCOME -> R.string.editor_result_income_added
                else -> R.string.editor_result_transfer_added
            }
            _uiState.update { it.copy(isSaving = false, isFinished = true, resultMessage = message) }
        }
    }
}

/** For transfers, makes sure a valid destination is selected; clears it otherwise. */
private fun TransactionEditorUiState.withDefaultToAccount(): TransactionEditorUiState {
    if (type != TransactionType.TRANSFER) return copy(toAccountId = null)
    val destinations = toAccounts
    if (destinations.any { it.id == toAccountId }) return this
    return copy(toAccountId = destinations.firstOrNull()?.id)
}

private val TransactionType.categoryKind: CategoryKind?
    get() = when (this) {
        TransactionType.INCOME -> CategoryKind.INCOME
        TransactionType.EXPENSE -> CategoryKind.EXPENSE
        TransactionType.TRANSFER -> null
    }
