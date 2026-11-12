package com.prajwalpawar.fiscus.ui.feature.transactions

import androidx.annotation.StringRes
import com.prajwalpawar.fiscus.R

/** Immutable UI state for the Transactions screen. */
data class TransactionsUiState(
    @StringRes val emptyMessage: Int = R.string.transactions_empty,
)
