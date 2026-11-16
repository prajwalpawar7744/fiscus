package com.prajwalpawar.fiscus.ui.feature.categories

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.prajwalpawar.fiscus.model.CategoryKind

/**
 * Stateful entry point for the Categories page: connects [CategoriesViewModel] to
 * [CategoriesScreen].
 */
@Composable
fun CategoriesRoute(
    onBack: () -> Unit,
    onOpenCategory: (Long) -> Unit,
    onAddCategory: (CategoryKind) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CategoriesViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    CategoriesScreen(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onBack = onBack,
        onOpenCategory = onOpenCategory,
        onAddCategory = { onAddCategory(uiState.kind) },
        modifier = modifier,
    )
}
