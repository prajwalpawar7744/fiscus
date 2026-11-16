package com.prajwalpawar.fiscus.ui.feature.categories

import com.prajwalpawar.fiscus.model.CategoryWithUsage
import com.prajwalpawar.fiscus.model.CategoryKind

/**
 * Immutable UI state for the Categories page (ADR-040): the chosen kind's categories and their
 * usage (ADR-041).
 */
data class CategoriesUiState(
    val isLoading: Boolean = true,
    val kind: CategoryKind = CategoryKind.EXPENSE,
    val active: List<CategoryWithUsage> = emptyList(),
    val archived: List<CategoryWithUsage> = emptyList(),
)
