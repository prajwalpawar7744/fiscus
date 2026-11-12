package com.prajwalpawar.fiscus.ui.feature.home

import androidx.annotation.StringRes
import com.prajwalpawar.fiscus.R

/** Immutable UI state for the Home screen. */
data class HomeUiState(
    /** Hero balance (hero moment 1, ADR-010). Placeholder until accounts exist (M2). */
    @StringRes val balance: Int = R.string.home_balance_placeholder,
    @StringRes val emptyMessage: Int = R.string.home_empty,
)
