package com.prajwalpawar.fiscus.ui.feature.budgets

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/** Stateful entry point for Budgets: connects [BudgetsViewModel] to the stateless [BudgetsScreen]. */
@Composable
fun BudgetsRoute(
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: BudgetsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    BudgetsScreen(
        uiState = uiState,
        onOpenSettings = onOpenSettings,
        modifier = modifier,
    )
}
