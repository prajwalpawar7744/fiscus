package com.prajwalpawar.fiscus.ui.feature.accounts

import com.prajwalpawar.fiscus.model.AccountWithBalance

/** Immutable UI state for the Accounts page (ADR-036). */
data class AccountsUiState(
    val isLoading: Boolean = true,
    val active: List<AccountWithBalance> = emptyList(),
    val archived: List<AccountWithBalance> = emptyList(),
)
