package com.prajwalpawar.fiscus.ui.feature.transactioneditor

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalResources
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.prajwalpawar.fiscus.ui.navigation.TransactionEditorNavKey

/**
 * Stateful entry point for the editor: connects [TransactionEditorViewModel] to
 * [TransactionEditorScreen]. On a successful save it confirms with a haptic (the "satisfying
 * save" of hero moment 2, ADR-010) and hands the confirmation text to [onFinished].
 */
@Composable
fun TransactionEditorRoute(
    navKey: TransactionEditorNavKey,
    onBack: () -> Unit,
    onFinished: (message: String?) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TransactionEditorViewModel =
        hiltViewModel<TransactionEditorViewModel, TransactionEditorViewModel.Factory>(
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
    LaunchedEffect(uiState.amountError) {
        if (uiState.amountError != null) haptics.performHapticFeedback(HapticFeedbackType.Reject)
    }
    TransactionEditorScreen(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onBack = onBack,
        modifier = modifier,
    )
}
