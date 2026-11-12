package com.prajwalpawar.fiscus.ui.feature.insights

import androidx.annotation.StringRes
import com.prajwalpawar.fiscus.R

/** Immutable UI state for the Insights screen. */
data class InsightsUiState(
    @StringRes val emptyMessage: Int = R.string.insights_empty,
)
