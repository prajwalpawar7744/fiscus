package com.prajwalpawar.fiscus.ui.feature.accounteditor

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prajwalpawar.fiscus.R
import com.prajwalpawar.fiscus.core.money.defaultCurrencyCode
import com.prajwalpawar.fiscus.core.money.formatMoneyInput
import com.prajwalpawar.fiscus.core.money.isValidAmountInput
import com.prajwalpawar.fiscus.core.money.parseBalanceInput
import com.prajwalpawar.fiscus.data.repository.AccountRepository
import com.prajwalpawar.fiscus.model.Account
import com.prajwalpawar.fiscus.model.AccountType
import com.prajwalpawar.fiscus.model.AccountWithBalance
import com.prajwalpawar.fiscus.model.Money
import com.prajwalpawar.fiscus.ui.navigation.AccountEditorNavKey
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Screen-level state holder for adding or editing an account (ADR-036). What the user typed lives
 * in [form]; the edited account comes from a repository stream;
 * [uiState] combines them with `stateIn` (ARCHITECTURE.md "Forms"). Receives its nav key through
 * assisted injection (ADR-018).
 */
@HiltViewModel(assistedFactory = AccountEditorViewModel.Factory::class)
class AccountEditorViewModel @AssistedInject constructor(
    @Assisted private val navKey: AccountEditorNavKey,
    private val accountRepository: AccountRepository,
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(navKey: AccountEditorNavKey): AccountEditorViewModel
    }

    private val form = MutableStateFlow(AccountForm())

    private val original: Flow<AccountWithBalance?> =
        navKey.accountId?.let(accountRepository::observeAccountWithBalance) ?: flowOf(null)

    val uiState: StateFlow<AccountEditorUiState> =
        combine(form, original) { form, original -> form.toUiState(original) }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AccountEditorUiState(),
        )

    init {
        viewModelScope.launch { loadInitialForm() }
    }

    fun onEvent(event: AccountEditorEvent) {
        when (event) {
            is AccountEditorEvent.NameChanged -> form.update { it.copy(name = event.name) }
            is AccountEditorEvent.TypeChanged -> form.update { it.copy(type = event.type) }
            is AccountEditorEvent.DefaultChanged ->
                form.update { it.copy(isDefault = event.isDefault) }
            is AccountEditorEvent.AmountChanged -> {
                if (!isValidAmountInput(event.text)) return
                form.update { it.copy(amountText = event.text) }
            }
            is AccountEditorEvent.InDebtChanged -> form.update { it.copy(inDebt = event.inDebt) }
            is AccountEditorEvent.BalanceModeChanged -> changeMode(event.mode)
            AccountEditorEvent.Save -> save()
        }
    }

    /** Fills the form from the edited account; finishes when it's gone. */
    private suspend fun loadInitialForm() {
        val id = navKey.accountId
        if (id == null) {
            form.update { it.copy(isLoaded = true, currencyCode = defaultCurrencyCode()) }
            return
        }
        val existing = original.first()
        if (existing == null) {
            form.update { it.copy(isLoaded = true, isFinished = true) }
            return
        }
        val account = existing.account
        form.update {
            it.copy(
                isLoaded = true,
                isNew = false,
                name = account.name,
                type = account.type,
                currencyCode = account.currencyCode,
                isDefault = account.isDefault,
            ).withAmount(account.openingBalance)
        }
    }

    /** Switching mode starts the field from that balance, so the user edits from what's there. */
    private fun changeMode(mode: BalanceMode) {
        viewModelScope.launch {
            val existing = original.first() ?: return@launch
            val amount = when (mode) {
                BalanceMode.OPENING -> existing.account.openingBalance
                BalanceMode.CURRENT -> existing.balance
            }
            form.update { it.copy(balanceMode = mode).withAmount(amount) }
        }
    }

    private fun save() {
        // The latest form, not uiState.value, which can lag the last keystroke (LESSONS #12).
        val current = form.value
        viewModelScope.launch {
            val existing = original.first()
            val state = current.toUiState(existing)
            if (!state.canSave) return@launch
            val entered = current.signedAmount() ?: return@launch
            val opening = openingBalanceFor(current.balanceMode, entered, existing) ?: return@launch
            form.update { it.copy(isSaving = true) }
            val account = Account(
                id = existing?.account?.id ?: 0L,
                name = current.name.trim(),
                type = current.type,
                currencyCode = state.currencyCode,
                openingBalance = opening,
                archived = existing?.account?.archived ?: false,
                isDefault = current.isDefault && existing?.account?.archived != true,
            )
            accountRepository.save(account)
            val message = if (existing == null) {
                R.string.account_result_added
            } else {
                R.string.account_result_updated
            }
            form.update { it.copy(isSaving = false, isFinished = true, resultMessage = message) }
        }
    }
}

/** What the user has entered or chosen. The edited account itself is not stored here. */
private data class AccountForm(
    val isLoaded: Boolean = false,
    val isNew: Boolean = true,
    val name: String = "",
    val type: AccountType = AccountType.CASH,
    val currencyCode: String = "",
    val isDefault: Boolean = false,
    val balanceMode: BalanceMode = BalanceMode.OPENING,
    val amountText: String = "",
    val inDebt: Boolean = false,
    val isSaving: Boolean = false,
    val isFinished: Boolean = false,
    @StringRes val resultMessage: Int? = null,
) {
    /** The typed amount with the "in debt" sign; null when it doesn't parse. */
    fun signedAmount(): Money? =
        parseBalanceInput(amountText, currencyCode)?.let { if (inDebt) -it else it }

    /** The field shows [amount] unsigned ("" for zero), with the sign in [inDebt]. */
    fun withAmount(amount: Money): AccountForm = copy(
        amountText = when {
            amount.isZero -> ""
            amount.isNegative -> formatMoneyInput(-amount)
            else -> formatMoneyInput(amount)
        },
        inDebt = amount.isNegative,
    )
}

private fun AccountForm.toUiState(original: AccountWithBalance?): AccountEditorUiState {
    // The currency isn't editable (ADR-037): the edited account's, or the device's for a new one.
    val currency = original?.account?.currencyCode ?: currencyCode
    val entered = copy(currencyCode = currency).signedAmount()
    val opening = entered?.let { openingBalanceFor(balanceMode, it, original) }
    return AccountEditorUiState(
        isLoading = !isLoaded,
        isNew = isNew,
        name = name,
        type = type,
        currencyCode = currency,
        isDefault = isDefault,
        canBeDefault = original?.account?.archived != true,
        balanceMode = balanceMode,
        amountText = amountText,
        inDebt = inDebt,
        amountError = if (currency.isNotEmpty() && opening == null) {
            R.string.account_amount_error
        } else {
            null
        },
        openingPreview = opening?.takeIf { balanceMode == BalanceMode.CURRENT },
        isSaving = isSaving,
        isFinished = isFinished,
        resultMessage = resultMessage,
    )
}

/**
 * The opening balance to store (ADR-036). In [BalanceMode.OPENING] it's [entered]. In
 * [BalanceMode.CURRENT] the user typed the balance they see today, so the opening balance is
 * shifted by the same amount: `entered − (balance − opening)`. Null on overflow.
 */
internal fun openingBalanceFor(
    mode: BalanceMode,
    entered: Money,
    original: AccountWithBalance?,
): Money? = when {
    mode == BalanceMode.OPENING || original == null -> entered
    // Without transactions the balance is the opening balance, in whatever currency is chosen.
    original.balance == original.account.openingBalance -> entered
    else -> runCatching {
        entered - (original.balance - original.account.openingBalance)
    }.getOrNull()
}
