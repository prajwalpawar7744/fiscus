package com.prajwalpawar.fiscus.ui.navigation

import androidx.navigation3.runtime.NavKey
import com.prajwalpawar.fiscus.model.TransactionType
import kotlinx.serialization.Serializable

// Navigation 3 keys: one per destination. Serializable so the back stack survives process death.

@Serializable
data object HomeNavKey : NavKey

@Serializable
data object TransactionsNavKey : NavKey

@Serializable
data object BudgetsNavKey : NavKey

@Serializable
data object InsightsNavKey : NavKey

@Serializable
data object SettingsNavKey : NavKey

/** Add ([transactionId] null, starting as [type]) or edit an existing transaction. */
@Serializable
data class TransactionEditorNavKey(
    val transactionId: Long? = null,
    val type: TransactionType = TransactionType.EXPENSE,
) : NavKey

/** Read-only view of one transaction, with edit and delete actions. */
@Serializable
data class TransactionDetailNavKey(val transactionId: Long) : NavKey

/** Every account, active and archived, with Add (ADR-036). */
@Serializable
data object AccountsNavKey : NavKey

/** One account: balance hero, its transactions, archive/delete and edit (ADR-036). */
@Serializable
data class AccountDetailNavKey(val accountId: Long) : NavKey

/** Add ([accountId] null) or edit an account (ADR-036). */
@Serializable
data class AccountEditorNavKey(val accountId: Long? = null) : NavKey
