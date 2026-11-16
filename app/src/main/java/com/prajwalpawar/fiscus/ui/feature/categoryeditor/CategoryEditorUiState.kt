package com.prajwalpawar.fiscus.ui.feature.categoryeditor

import androidx.annotation.StringRes
import com.prajwalpawar.fiscus.R
import com.prajwalpawar.fiscus.model.CategoryKind

/** Immutable UI state for adding or editing a category (ADR-040). */
data class CategoryEditorUiState(
    val isLoading: Boolean = true,
    val isNew: Boolean = true,
    /** Required; saved trimmed. */
    val name: String = "",
    val kind: CategoryKind = CategoryKind.EXPENSE,
    /** A key from `CategoryIcons` (a Material Symbols name). */
    val iconKey: String = "",
    /** How many transactions use the edited category; 0 when adding. */
    val transactionCount: Int = 0,
    val archived: Boolean = false,
    val showDeleteConfirmation: Boolean = false,
    /** A confirmation to show while staying on the page ("Category archived"); the UI clears it. */
    @StringRes val userMessage: Int? = null,
    val isSaving: Boolean = false,
    /** True once saved or deleted (or the edited category no longer exists); the screen closes. */
    val isFinished: Boolean = false,
    @StringRes val resultMessage: Int? = null,
) {
    /** A used category keeps its kind: its transactions' type depends on it (ADR-040). */
    val isKindLocked: Boolean get() = transactionCount > 0

    /** Only a category no transaction uses can be deleted; others are archived (ADR-040). */
    val canDelete: Boolean get() = !isNew && transactionCount == 0

    /** Why Save is disabled, shown above it; null when ready (or while loading or saving). */
    @get:StringRes
    val saveHint: Int?
        get() = when {
            isLoading || isSaving -> null
            name.isBlank() -> R.string.category_hint_name
            else -> null
        }

    val canSave: Boolean
        get() = !isLoading && !isSaving && name.isNotBlank() && iconKey.isNotEmpty()
}
