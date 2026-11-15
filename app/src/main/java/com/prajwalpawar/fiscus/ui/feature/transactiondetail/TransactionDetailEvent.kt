package com.prajwalpawar.fiscus.ui.feature.transactiondetail

/** User actions on the transaction detail screen. */
sealed interface TransactionDetailEvent {
    data object DeleteRequested : TransactionDetailEvent
    data object DeleteDismissed : TransactionDetailEvent
    data object DeleteConfirmed : TransactionDetailEvent
}
