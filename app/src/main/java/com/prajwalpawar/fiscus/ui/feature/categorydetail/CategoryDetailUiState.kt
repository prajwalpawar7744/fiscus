package com.prajwalpawar.fiscus.ui.feature.categorydetail

import androidx.annotation.StringRes
import com.prajwalpawar.fiscus.model.CategoryWithUsage

/** Immutable UI state for one category's page (ADR-042). */
data class CategoryDetailUiState(
    /** From the nav key, known before loading: links the page to its row (ADR-028). */
    val categoryId: Long = 0L,
    val isLoading: Boolean = true,
    val category: CategoryWithUsage? = null,
    val showDeleteConfirmation: Boolean = false,
    /** A confirmation to show while staying on the page ("Category archived"); the UI clears it. */
    @StringRes val userMessage: Int? = null,
    /** True once the category was deleted (or no longer exists); the screen then closes. */
    val isFinished: Boolean = false,
    /** Confirmation to show after closing ("Category deleted"); null if nothing changed. */
    @StringRes val resultMessage: Int? = null,
) {
    /** Only a category no transaction uses can be deleted; others are archived (ADR-040). */
    val canDelete: Boolean get() = category?.transactionCount == 0
}
