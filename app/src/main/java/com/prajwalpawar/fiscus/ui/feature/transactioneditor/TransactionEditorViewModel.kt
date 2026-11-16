package com.prajwalpawar.fiscus.ui.feature.transactioneditor

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prajwalpawar.fiscus.R
import com.prajwalpawar.fiscus.core.money.formatMoneyInput
import com.prajwalpawar.fiscus.core.money.isValidAmountInput
import com.prajwalpawar.fiscus.core.money.parseMoneyInput
import com.prajwalpawar.fiscus.data.repository.AccountRepository
import com.prajwalpawar.fiscus.data.repository.CategoryRepository
import com.prajwalpawar.fiscus.data.repository.TransactionRepository
import com.prajwalpawar.fiscus.model.Account
import com.prajwalpawar.fiscus.model.Category
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
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

/**
 * Screen-level state holder for adding or editing a transaction. Receives its
 * [TransactionEditorNavKey] through assisted injection (ADR-018).
 *
 * State follows ARCHITECTURE.md: what the user typed or chose lives in [form]; accounts and
 * categories come from repository streams; [uiState] combines them with `stateIn`. Defaults
 * (first account, transfer destination, dropping a category that no longer applies) are derived
 * in [toUiState], never copied into the form.
 */
@HiltViewModel(assistedFactory = TransactionEditorViewModel.Factory::class)
class TransactionEditorViewModel @AssistedInject constructor(
    @Assisted private val navKey: TransactionEditorNavKey,
    private val transactionRepository: TransactionRepository,
    accountRepository: AccountRepository,
    categoryRepository: CategoryRepository,
    private val clock: Clock,
    private val timeZone: TimeZone,
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(navKey: TransactionEditorNavKey): TransactionEditorViewModel
    }

    private val form = MutableStateFlow(EditorForm(type = navKey.type))

    /** The transaction being edited, or null when adding. */
    private var original: Transaction? = null

    @OptIn(ExperimentalCoroutinesApi::class) // flatMapLatest
    private val categories = form.map { it.type }
        .distinctUntilChanged()
        .flatMapLatest { type ->
            val kind = type.categoryKind ?: return@flatMapLatest flowOf(emptyList())
            categoryRepository.observeCategories().map { list -> list.filter { it.kind == kind } }
        }

    val uiState: StateFlow<TransactionEditorUiState> = combine(
        form,
        accountRepository.observeAccounts(),
        categories,
    ) { form, accounts, categories -> form.toUiState(accounts, categories) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = TransactionEditorUiState(),
        )

    init {
        viewModelScope.launch { loadInitialForm() }
    }

    fun onEvent(event: TransactionEditorEvent) {
        when (event) {
            is TransactionEditorEvent.TypeChanged -> form.update {
                it.copy(type = event.type, categoryId = null)
            }
            is TransactionEditorEvent.AmountChanged -> {
                if (!isValidAmountInput(event.text)) return
                form.update { it.copy(amountText = event.text, amountError = null) }
            }
            is TransactionEditorEvent.TitleChanged -> form.update { it.copy(title = event.title) }
            is TransactionEditorEvent.AccountSelected -> form.update { it.copy(accountId = event.accountId) }
            is TransactionEditorEvent.ToAccountSelected -> form.update { it.copy(toAccountId = event.accountId) }
            is TransactionEditorEvent.CategorySelected -> form.update { it.copy(categoryId = event.categoryId) }
            is TransactionEditorEvent.DateSelected -> form.update { it.copy(date = event.date) }
            is TransactionEditorEvent.NoteChanged -> form.update { it.copy(note = event.note) }
            TransactionEditorEvent.Save -> save()
        }
    }

    /** Fills the form from the nav key; finishes when the edited transaction is gone. */
    private suspend fun loadInitialForm() {
        val id = navKey.transactionId
        val existing = id?.let { transactionRepository.observeTransaction(it).first() }
        if (id != null && existing == null) {
            form.update { it.copy(isLoaded = true, isFinished = true) }
            return
        }
        original = existing
        form.update { current ->
            if (existing == null) {
                current.copy(isLoaded = true, date = clock.now().toLocalDateTime(timeZone).date)
            } else {
                current.copy(
                    isLoaded = true,
                    isNew = false,
                    type = existing.type,
                    amountText = formatMoneyInput(existing.amount),
                    title = existing.title,
                    accountId = existing.accountId,
                    toAccountId = existing.toAccountId,
                    categoryId = existing.categoryId,
                    date = existing.occurredAt.toLocalDateTime(timeZone).date,
                    note = existing.note,
                )
            }
        }
    }

    private fun save() {
        // Use the latest form, not uiState.value: the combined state may not have re-emitted the
        // last keystroke yet. Reference data (accounts, categories) comes from the current state.
        val reference = uiState.value
        val state = form.value.toUiState(reference.accounts, reference.categories)
        val account = state.selectedAccount ?: return
        val date = state.date ?: return
        val amount = parseMoneyInput(state.amountText, account.currencyCode)
        if (amount == null) {
            form.update { it.copy(amountError = R.string.editor_amount_error) }
            return
        }
        if (!state.canSave) return

        form.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            val now = clock.now()
            // Keep the original time of day when editing; new entries use the current time.
            val time = (original?.occurredAt ?: now).toLocalDateTime(timeZone).time
            val isTransfer = state.type == TransactionType.TRANSFER
            val transaction = Transaction(
                id = original?.id ?: 0L,
                type = state.type,
                title = state.title.trim(),
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
            form.update { it.copy(isSaving = false, isFinished = true, resultMessage = message) }
        }
    }
}

/** What the user has entered or chosen. Reference data (accounts, categories) is not stored here. */
private data class EditorForm(
    val isLoaded: Boolean = false,
    val isNew: Boolean = true,
    val type: TransactionType,
    val amountText: String = "",
    @StringRes val amountError: Int? = null,
    val title: String = "",
    val accountId: Long? = null,
    val toAccountId: Long? = null,
    val categoryId: Long? = null,
    val date: LocalDate? = null,
    val note: String = "",
    val isSaving: Boolean = false,
    val isFinished: Boolean = false,
    @StringRes val resultMessage: Int? = null,
)

/** Combines the form with the latest reference data, deriving the defaults. */
private fun EditorForm.toUiState(accounts: List<Account>, categories: List<Category>): TransactionEditorUiState {
    // Archived accounts stay selectable only if this transaction already uses them.
    val usable = accounts.filter { !it.archived || it.id == accountId || it.id == toAccountId }
    // Same for archived categories (ADR-040).
    val usableCategories = categories.filter { !it.archived || it.id == categoryId }
    // New transactions start on the default account (ADR-037), else the first.
    val selectedAccount = usable.find { it.id == accountId }
        ?: usable.find { it.isDefault && !it.archived }
        ?: usable.firstOrNull()
    val destinations = selectedAccount?.let { from ->
        usable.filter { it.id != from.id && it.currencyCode == from.currencyCode }
    }.orEmpty()
    val toAccountId = when (type) {
        TransactionType.TRANSFER ->
            destinations.find { it.id == toAccountId }?.id ?: destinations.firstOrNull()?.id
        TransactionType.INCOME, TransactionType.EXPENSE -> null
    }
    return TransactionEditorUiState(
        isLoading = !isLoaded,
        isNew = isNew,
        type = type,
        amountText = amountText,
        amountError = amountError,
        title = title,
        accounts = usable,
        accountId = selectedAccount?.id,
        toAccountId = toAccountId,
        categories = usableCategories,
        categoryId = categoryId?.takeIf { id -> usableCategories.any { it.id == id } },
        date = date,
        note = note,
        isSaving = isSaving,
        isFinished = isFinished,
        resultMessage = resultMessage,
    )
}

private val TransactionType.categoryKind: CategoryKind?
    get() = when (this) {
        TransactionType.INCOME -> CategoryKind.INCOME
        TransactionType.EXPENSE -> CategoryKind.EXPENSE
        TransactionType.TRANSFER -> null
    }
