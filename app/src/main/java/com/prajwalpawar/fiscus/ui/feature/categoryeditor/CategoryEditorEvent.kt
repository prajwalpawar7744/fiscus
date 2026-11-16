package com.prajwalpawar.fiscus.ui.feature.categoryeditor

import com.prajwalpawar.fiscus.model.CategoryKind

/** User actions on the category editor. */
sealed interface CategoryEditorEvent {
    data class NameChanged(val name: String) : CategoryEditorEvent
    data class KindChanged(val kind: CategoryKind) : CategoryEditorEvent
    data class IconChanged(val iconKey: String) : CategoryEditorEvent
    data object Save : CategoryEditorEvent
}
