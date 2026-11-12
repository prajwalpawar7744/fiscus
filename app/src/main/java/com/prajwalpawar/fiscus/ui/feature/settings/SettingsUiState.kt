package com.prajwalpawar.fiscus.ui.feature.settings

import androidx.annotation.StringRes
import com.prajwalpawar.fiscus.R

/** Immutable UI state for the Settings screen. */
data class SettingsUiState(
    @StringRes val emptyMessage: Int = R.string.settings_empty,
)
