package com.prajwalpawar.fiscus.ui.navigation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.navigation3.runtime.NavKey
import com.prajwalpawar.fiscus.R

/**
 * Destinations shown in the navigation bar/rail (ADR-015). Icons are Material Symbols Rounded:
 * filled when selected, outlined otherwise (ADR-010).
 */
enum class TopLevelDestination(
    val navKey: NavKey,
    @StringRes val label: Int,
    @DrawableRes val icon: Int,
    @DrawableRes val selectedIcon: Int,
) {
    HOME(
        navKey = HomeNavKey,
        label = R.string.nav_home,
        icon = R.drawable.ic_home_rounded,
        selectedIcon = R.drawable.ic_home_rounded_filled,
    ),
    TRANSACTIONS(
        navKey = TransactionsNavKey,
        label = R.string.nav_transactions,
        icon = R.drawable.ic_payments_rounded,
        selectedIcon = R.drawable.ic_payments_rounded_filled,
    ),
    BUDGETS(
        navKey = BudgetsNavKey,
        label = R.string.nav_budgets,
        icon = R.drawable.ic_savings_rounded,
        selectedIcon = R.drawable.ic_savings_rounded_filled,
    ),
    INSIGHTS(
        navKey = InsightsNavKey,
        label = R.string.nav_insights,
        icon = R.drawable.ic_bar_chart_rounded,
        selectedIcon = R.drawable.ic_bar_chart_rounded_filled,
    ),
}
