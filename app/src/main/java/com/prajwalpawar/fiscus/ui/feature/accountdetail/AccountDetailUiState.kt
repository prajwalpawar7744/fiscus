package com.prajwalpawar.fiscus.ui.feature.accountdetail

import androidx.annotation.StringRes
import com.prajwalpawar.fiscus.model.AccountWithBalance

/** Immutable UI state for one account's page (ADR-036). */
data class AccountDetailUiState(
    /** From the nav key, known before loading: links the page to its card or row (ADR-028). */
    val accountId: Long = 0L,
    val isLoading: Boolean = true,
    val account: AccountWithBalance? = null,
    /** How many transactions refer to the account (transfers in included). */
    val transactionCount: Int = 0,
    val showDeleteConfirmation: Boolean = false,
    /** A confirmation to show while staying on the page ("Account archived"); the UI clears it. */
    @StringRes val userMessage: Int? = null,
    /** True once the account was deleted (or no longer exists); the screen then closes. */
    val isFinished: Boolean = false,
    /** Confirmation to show after closing ("Account deleted"); null if nothing changed. */
    @StringRes val resultMessage: Int? = null,
) {
    /** Only an account nothing refers to can be deleted; others are archived (ADR-017, ADR-036). */
    val canDelete: Boolean get() = transactionCount == 0
}
