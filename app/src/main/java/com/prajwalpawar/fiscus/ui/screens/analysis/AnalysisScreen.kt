package com.prajwalpawar.fiscus.ui.screens.analysis

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Insights
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.prajwalpawar.fiscus.ui.components.FiscusEmptyState

@Composable
fun AnalysisScreen(
    modifier: Modifier = Modifier
) {
    FiscusEmptyState(
        icon = Icons.Default.Insights,
        title = "Nothing to analyse yet",
        message = "Add a few transactions to see where your money goes.",
        modifier = modifier
    )
}
