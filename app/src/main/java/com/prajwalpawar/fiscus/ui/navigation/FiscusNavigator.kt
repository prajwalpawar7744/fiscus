package com.prajwalpawar.fiscus.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack

/**
 * Back-stack rules (ADR-015):
 * - Home is always the root.
 * - Selecting a tab resets the stack to `[Home]` or `[Home, tab]`, so Back from a tab returns
 *   to Home and Back from Home leaves the app.
 * - Other destinations (e.g. Settings) are pushed on top and popped with Back.
 */
class FiscusNavigator(val backStack: MutableList<NavKey>) {

    /** The tab to highlight, or null when a pushed (non-tab) destination is on top. */
    val currentTopLevel: TopLevelDestination?
        get() = TopLevelDestination.entries.firstOrNull { it.navKey == backStack.lastOrNull() }

    fun navigateToTopLevel(destination: TopLevelDestination) {
        backStack.clear()
        backStack.add(HomeNavKey)
        if (destination.navKey != HomeNavKey) backStack.add(destination.navKey)
    }

    fun navigate(key: NavKey) {
        backStack.add(key)
    }

    fun goBack() {
        if (backStack.size > 1) backStack.removeAt(backStack.lastIndex)
    }
}

@Composable
fun rememberFiscusNavigator(): FiscusNavigator {
    val backStack = rememberNavBackStack(HomeNavKey)
    return remember(backStack) { FiscusNavigator(backStack) }
}
