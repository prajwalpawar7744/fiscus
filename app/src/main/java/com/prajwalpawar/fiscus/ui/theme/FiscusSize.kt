package com.prajwalpawar.fiscus.ui.theme

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// Component dimensions; spacing between elements lives in FiscusSpacing.
object FiscusSize {
    // Matches DatePicker's own minimum width, so it is never squeezed.
    val datePickerMaxWidth: Dp = 360.dp
    val emptyStateShape: Dp = 120.dp
    val emptyStateIcon: Dp = 48.dp
}
