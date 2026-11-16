package com.prajwalpawar.fiscus.ui.feature.accounteditor

import com.prajwalpawar.fiscus.model.AccountType

/** User actions on the account editor. */
sealed interface AccountEditorEvent {
    data class NameChanged(val name: String) : AccountEditorEvent
    data class TypeChanged(val type: AccountType) : AccountEditorEvent
    data class DefaultChanged(val isDefault: Boolean) : AccountEditorEvent
    data class BalanceModeChanged(val mode: BalanceMode) : AccountEditorEvent
    data class AmountChanged(val text: String) : AccountEditorEvent
    data class InDebtChanged(val inDebt: Boolean) : AccountEditorEvent
    data object Save : AccountEditorEvent
}
