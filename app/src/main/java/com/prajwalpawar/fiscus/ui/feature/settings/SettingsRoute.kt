package com.prajwalpawar.fiscus.ui.feature.settings

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Entry point for Settings. Its rows only navigate for now, so there is no ViewModel or UI state
 * yet (ADR-040); settings with values (M5: theme, app lock) bring them back with the usual
 * feature pattern.
 */
@Composable
fun SettingsRoute(
    onBack: () -> Unit,
    onOpenAccounts: () -> Unit,
    onOpenCategories: () -> Unit,
    modifier: Modifier = Modifier,
) {
    SettingsScreen(
        onBack = onBack,
        onOpenAccounts = onOpenAccounts,
        onOpenCategories = onOpenCategories,
        modifier = modifier,
    )
}
