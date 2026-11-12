package com.prajwalpawar.fiscus.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle

/**
 * M3 type scale (15 baseline + 15 emphasized styles), kept at Material defaults.
 * Uses the device's font: Material's default typeface is the system sans-serif, which follows
 * the phone's font and OEM font settings. Never bundle or set a custom font (ADR-014).
 */
internal val FiscusTypography = Typography()

/**
 * Tabular (monospaced) figures, so amounts stay aligned and don't shift when they change.
 * Use for every amount, e.g. `MaterialTheme.typography.displayLargeEmphasized.tabularNumbers`.
 */
val TextStyle.tabularNumbers: TextStyle
    get() = copy(fontFeatureSettings = "tnum")
