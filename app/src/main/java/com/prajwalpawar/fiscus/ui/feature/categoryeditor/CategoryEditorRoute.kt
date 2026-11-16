package com.prajwalpawar.fiscus.ui.feature.categoryeditor

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalResources
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.prajwalpawar.fiscus.ui.navigation.CategoryEditorNavKey

/**
 * Stateful entry point for the category editor: connects [CategoryEditorViewModel] to
 * [CategoryEditorScreen]. On save it confirms with a haptic and hands the confirmation text to
 * [onFinished].
 */
@Composable
fun CategoryEditorRoute(
    navKey: CategoryEditorNavKey,
    onBack: () -> Unit,
    onFinished: (message: String?) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CategoryEditorViewModel =
        hiltViewModel<CategoryEditorViewModel, CategoryEditorViewModel.Factory>(
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
    CategoryEditorScreen(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onBack = onBack,
        modifier = modifier,
    )
}
