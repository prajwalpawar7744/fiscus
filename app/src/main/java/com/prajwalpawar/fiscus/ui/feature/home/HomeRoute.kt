package com.prajwalpawar.fiscus.ui.feature.home

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.prajwalpawar.fiscus.model.TransactionType

/** Stateful entry point for Home: connects [HomeViewModel] to the stateless [HomeScreen]. */
@Composable
fun HomeRoute(
    snackbarHostState: SnackbarHostState,
    onOpenSettings: () -> Unit,
    onAddTransaction: (TransactionType) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    HomeScreen(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onOpenSettings = onOpenSettings,
        onAddTransaction = onAddTransaction,
        modifier = modifier,
    )
}
