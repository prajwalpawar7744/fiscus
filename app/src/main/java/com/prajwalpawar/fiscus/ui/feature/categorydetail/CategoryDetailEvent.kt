package com.prajwalpawar.fiscus.ui.feature.categorydetail

/** User actions on a category's page. */
sealed interface CategoryDetailEvent {
    data object ArchiveToggled : CategoryDetailEvent
    data object DeleteRequested : CategoryDetailEvent
    data object DeleteDismissed : CategoryDetailEvent
    data object DeleteConfirmed : CategoryDetailEvent
    data object UserMessageShown : CategoryDetailEvent
}
