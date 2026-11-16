package com.prajwalpawar.fiscus.ui.feature.accounteditor

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalResources
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.prajwalpawar.fiscus.ui.navigation.AccountEditorNavKey

/**
 * Stateful entry point for the account editor: connects [AccountEditorViewModel] to
 * [AccountEditorScreen]. On save it confirms with a haptic and hands the confirmation text to
 * [onFinished]; an amount that doesn't parse gets a reject haptic (ADR-019 item 5).
 */
@Composable
fun AccountEditorRoute(
    navKey: AccountEditorNavKey,
    onBack: () -> Unit,
    onFinished: (message: String?) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AccountEditorViewModel =
        hiltViewModel<AccountEditorViewModel, AccountEditorViewModel.Factory>(
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
    AccountEditorScreen(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onBack = onBack,
        modifier = modifier,
    )
}
