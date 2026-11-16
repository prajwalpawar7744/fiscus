package com.prajwalpawar.fiscus.ui.feature.accountdetail

/** User actions on an account's page. */
sealed interface AccountDetailEvent {
    data object ArchiveToggled : AccountDetailEvent
    data object DeleteRequested : AccountDetailEvent
    data object DeleteDismissed : AccountDetailEvent
    data object DeleteConfirmed : AccountDetailEvent
    data object UserMessageShown : AccountDetailEvent
}
