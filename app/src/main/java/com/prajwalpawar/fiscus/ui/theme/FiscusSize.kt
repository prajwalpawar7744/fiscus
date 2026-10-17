package com.prajwalpawar.fiscus.ui.theme

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// Component dimensions; spacing between elements lives in FiscusSpacing.
object FiscusSize {
    // Matches DatePicker's own minimum width, so it is never squeezed.
    val datePickerMaxWidth: Dp = 360.dp
    val emptyStateShape: Dp = 120.dp
    val emptyStateIcon: Dp = 48.dp
    val listIconContainer: Dp = 40.dp
    // Direction arrow beside a transaction amount, sized to sit with titleSmall text.
    val amountArrow: Dp = 18.dp
    // The same arrow beside a display-size hero amount.
    val heroAmountArrow: Dp = 36.dp
    val accountCardWidth: Dp = 180.dp
    val accountCardHeight: Dp = 148.dp
    // Small FAB (56dp) plus its 16dp margins, so the last list row can scroll clear of it.
    val fabClearance: Dp = 88.dp

    // Charts: thin marks with a small rounded data-end, square at the baseline.
    val chartBarThickness: Dp = 12.dp
    val chartColumnWidth: Dp = 24.dp
    val chartMarkCorner: Dp = 4.dp
    val trendChartHeight: Dp = 140.dp
}
