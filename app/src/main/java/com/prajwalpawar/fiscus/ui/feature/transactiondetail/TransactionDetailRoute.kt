package com.prajwalpawar.fiscus.ui.feature.transactiondetail

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalResources
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.prajwalpawar.fiscus.ui.navigation.TransactionDetailNavKey

/**
 * Stateful entry point for the detail screen: connects [TransactionDetailViewModel] to
 * [TransactionDetailScreen]. After a delete it confirms with a haptic and hands the confirmation
 * text to [onFinished].
 */
@Composable
fun TransactionDetailRoute(
    navKey: TransactionDetailNavKey,
    snackbarHostState: SnackbarHostState,
    onBack: () -> Unit,
    onEdit: (transactionId: Long) -> Unit,
    onFinished: (message: String?) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TransactionDetailViewModel =
        hiltViewModel<TransactionDetailViewModel, TransactionDetailViewModel.Factory>(
            creationCallback = { factory -> factory.create(navKey) },
        ),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val haptics = LocalHapticFeedback.current
    val resources = LocalResources.current
    LaunchedEffect(uiState.isFinished) {
        if (!uiState.isFinished) return@LaunchedEffect
        val message = uiState.resultMessage?.let(resources::getString)
        if (message != null) haptics.performHapticFeedback(HapticFeedbackType.Confirm)
        onFinished(message)
    }
    TransactionDetailScreen(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onEvent = viewModel::onEvent,
        onBack = onBack,
        onEdit = { onEdit(navKey.transactionId) },
        modifier = modifier,
    )
}
