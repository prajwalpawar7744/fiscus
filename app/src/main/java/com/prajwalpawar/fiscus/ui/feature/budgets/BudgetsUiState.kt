package com.prajwalpawar.fiscus.ui.feature.budgets

import androidx.annotation.StringRes
import com.prajwalpawar.fiscus.R

/** Immutable UI state for the Budgets screen. */
data class BudgetsUiState(
    @StringRes val emptyMessage: Int = R.string.budgets_empty,
)
