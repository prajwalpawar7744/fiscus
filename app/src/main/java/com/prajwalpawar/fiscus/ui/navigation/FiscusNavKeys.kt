package com.prajwalpawar.fiscus.ui.navigation

import androidx.navigation3.runtime.NavKey
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
