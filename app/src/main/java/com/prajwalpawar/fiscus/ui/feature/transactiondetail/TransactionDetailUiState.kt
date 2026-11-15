package com.prajwalpawar.fiscus.ui.feature.transactiondetail

import androidx.annotation.StringRes
import com.prajwalpawar.fiscus.model.Account
import com.prajwalpawar.fiscus.model.Category
import com.prajwalpawar.fiscus.model.Transaction
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime

/** Immutable UI state for the transaction detail screen. */
data class TransactionDetailUiState(
    /** From the nav key, known before loading: links the page to its list item (ADR-028). */
    val transactionId: Long = 0L,
    val isLoading: Boolean = true,
    val transaction: Transaction? = null,
    /** When it happened, in the user's time zone (converted with the injected `TimeZone`). */
    val occurredAt: LocalDateTime? = null,
    /** The user's current day (injected clock), for the "Today · 7:30 pm" subtitle. */
    val today: LocalDate? = null,
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
