package com.prajwalpawar.fiscus.ui.feature.accountdetail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalResources
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.prajwalpawar.fiscus.ui.components.LocalSnackbarHostState
import com.prajwalpawar.fiscus.ui.navigation.AccountDetailNavKey

/**
 * Stateful entry point for an account's page: connects [AccountDetailViewModel] to
 * [AccountDetailScreen]. Archive confirmations show here and are then cleared (state, not
 * events); after a delete it confirms with a haptic and hands the text to [onFinished].
 */
@Composable
fun AccountDetailRoute(
    navKey: AccountDetailNavKey,
    onBack: () -> Unit,
    onEdit: (accountId: Long) -> Unit,
    onFinished: (message: String?) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AccountDetailViewModel =
        hiltViewModel<AccountDetailViewModel, AccountDetailViewModel.Factory>(
            creationCallback = { factory -> factory.create(navKey) },
        ),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val haptics = LocalHapticFeedback.current
    val resources = LocalResources.current
    val snackbarHostState = LocalSnackbarHostState.current
    LaunchedEffect(uiState.isFinished) {
        if (!uiState.isFinished) return@LaunchedEffect
        val message = uiState.resultMessage?.let(resources::getString)
        if (message != null) haptics.performHapticFeedback(HapticFeedbackType.Confirm)
        onFinished(message)
    }
    LaunchedEffect(uiState.userMessage) {
        val message = uiState.userMessage ?: return@LaunchedEffect
        haptics.performHapticFeedback(HapticFeedbackType.Confirm)
        viewModel.onEvent(AccountDetailEvent.UserMessageShown)
        snackbarHostState.showSnackbar(resources.getString(message))
    }
    AccountDetailScreen(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onBack = onBack,
        onEdit = { onEdit(navKey.accountId) },
        modifier = modifier,
    )
}
