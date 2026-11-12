package com.prajwalpawar.fiscus.ui.feature.transactions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/** Stateful entry point for Transactions: connects [TransactionsViewModel] to the stateless [TransactionsScreen]. */
@Composable
fun TransactionsRoute(
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TransactionsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    TransactionsScreen(
        uiState = uiState,
        onOpenSettings = onOpenSettings,
        modifier = modifier,
    )
}
