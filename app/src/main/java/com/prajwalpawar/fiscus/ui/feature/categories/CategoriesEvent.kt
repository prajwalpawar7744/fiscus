package com.prajwalpawar.fiscus.ui.feature.categories

import com.prajwalpawar.fiscus.model.CategoryKind

/** User actions on the Categories page. */
sealed interface CategoriesEvent {
    data class KindSelected(val kind: CategoryKind) : CategoriesEvent
}
