package com.prajwalpawar.fiscus.ui.feature.accounteditor

import androidx.annotation.StringRes
import com.prajwalpawar.fiscus.R
import com.prajwalpawar.fiscus.model.AccountType
import com.prajwalpawar.fiscus.model.Money

/** Immutable UI state for adding or editing an account (ADR-036). */
data class AccountEditorUiState(
    val isLoading: Boolean = true,
    val isNew: Boolean = true,
    /** Required; saved trimmed. */
    val name: String = "",
    val type: AccountType = AccountType.CASH,
    /** Not shown or chosen (ADR-037): the device currency for new accounts, else the account's. */
    val currencyCode: String = "",
    /** Preselected for new transactions; saving it makes every other account non-default. */
    val isDefault: Boolean = false,
    /** Archived accounts can't be the default, so the switch is hidden for them. */
    val canBeDefault: Boolean = true,
    /** Which balance [amountText] is: always [BalanceMode.OPENING] when adding. */
    val balanceMode: BalanceMode = BalanceMode.OPENING,
    /** Unsigned amount as typed; blank means zero. The sign comes from [inDebt]. */
    val amountText: String = "",
    val inDebt: Boolean = false,
    @StringRes val amountError: Int? = null,
    /** In [BalanceMode.CURRENT], the opening balance that saving will store; null otherwise. */
    val openingPreview: Money? = null,
    val isSaving: Boolean = false,
    /** True once saved (or the edited account no longer exists); the screen then closes. */
    val isFinished: Boolean = false,
    @StringRes val resultMessage: Int? = null,
) {
    /** Why Save is disabled, shown above it; null when ready (or while loading or saving). */
    @get:StringRes
    val saveHint: Int?
        get() = when {
            isLoading || isSaving -> null
            name.isBlank() -> R.string.account_hint_name
            else -> null
        }

    val canSave: Boolean
        get() = !isLoading && !isSaving && name.isNotBlank() && currencyCode.isNotEmpty() &&
            amountError == null
}

/** What the balance field holds: the opening balance, or the current one to reconcile to. */
enum class BalanceMode { OPENING, CURRENT }
