package com.prajwalpawar.fiscus.ui.feature.accounts

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/** Stateful entry point for the Accounts page: connects [AccountsViewModel] to [AccountsScreen]. */
@Composable
fun AccountsRoute(
    onBack: () -> Unit,
    onOpenAccount: (Long) -> Unit,
    onAddAccount: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AccountsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    AccountsScreen(
        uiState = uiState,
        onBack = onBack,
        onOpenAccount = onOpenAccount,
        onAddAccount = onAddAccount,
        modifier = modifier,
    )
}
