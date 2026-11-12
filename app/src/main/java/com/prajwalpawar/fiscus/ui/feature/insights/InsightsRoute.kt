package com.prajwalpawar.fiscus.ui.feature.insights

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/** Stateful entry point for Insights: connects [InsightsViewModel] to the stateless [InsightsScreen]. */
@Composable
fun InsightsRoute(
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: InsightsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    InsightsScreen(
        uiState = uiState,
        onOpenSettings = onOpenSettings,
        modifier = modifier,
    )
}
