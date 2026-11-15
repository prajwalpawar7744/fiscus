package com.prajwalpawar.fiscus.ui.feature.transactiondetail

import androidx.annotation.StringRes
import com.prajwalpawar.fiscus.model.Account
import com.prajwalpawar.fiscus.model.Category
import com.prajwalpawar.fiscus.model.Transaction

/** Immutable UI state for the transaction detail screen. */
data class TransactionDetailUiState(
    val isLoading: Boolean = true,
    val transaction: Transaction? = null,
    val account: Account? = null,
    /** Destination account; only for transfers. */
    val toAccount: Account? = null,
    /** Null for transfers, or when the category was removed. */
    val category: Category? = null,
    val showDeleteConfirmation: Boolean = false,
    /** True once the transaction was deleted (or no longer exists); the screen then closes. */
    val isFinished: Boolean = false,
    /** Confirmation to show after closing ("Transaction deleted"); null if nothing changed. */
    @StringRes val resultMessage: Int? = null,
)
