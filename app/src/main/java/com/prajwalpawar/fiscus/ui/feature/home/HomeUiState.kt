package com.prajwalpawar.fiscus.ui.feature.home

import com.prajwalpawar.fiscus.model.AccountWithBalance
import com.prajwalpawar.fiscus.model.Money
import com.prajwalpawar.fiscus.model.PeriodSummary
import com.prajwalpawar.fiscus.model.TransactionListEntry

/** Immutable UI state for the Home screen (ADR-033). */
data class HomeUiState(
    val isLoading: Boolean = true,
    /** Sum of every active account's balance; null when the accounts mix currencies (ADR-016). */
    val totalBalance: Money? = null,
    /** Active accounts with their balances, in the order the database returns them. */
    val accounts: List<AccountWithBalance> = emptyList(),
    /** This month's income and expense; null when there are none or currencies are mixed. */
    val monthSummary: PeriodSummary? = null,
    /** The newest transactions, at most [RecentTransactionCount]. */
    val recent: List<TransactionListEntry> = emptyList(),
)

/** How many transactions Home's recent list shows. */
const val RecentTransactionCount = 5
