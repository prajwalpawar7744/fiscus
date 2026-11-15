package com.prajwalpawar.fiscus.ui.navigation

import androidx.navigation3.runtime.NavKey
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class FiscusNavigatorTest {

    private fun navigator() = FiscusNavigator(mutableListOf<NavKey>(HomeNavKey))

    @Test
    fun `starts on Home`() {
        val navigator = navigator()
        assertEquals(listOf(HomeNavKey), navigator.backStack)
        assertEquals(TopLevelDestination.HOME, navigator.currentTopLevel)
    }

    @Test
    fun `selecting a tab puts it on top of Home`() {
        val navigator = navigator()
        navigator.navigateToTopLevel(TopLevelDestination.BUDGETS)
        assertEquals(listOf(HomeNavKey, BudgetsNavKey), navigator.backStack)
        assertEquals(TopLevelDestination.BUDGETS, navigator.currentTopLevel)
    }

    @Test
    fun `switching tabs replaces the previous tab`() {
        val navigator = navigator()
        navigator.navigateToTopLevel(TopLevelDestination.TRANSACTIONS)
        navigator.navigateToTopLevel(TopLevelDestination.INSIGHTS)
        assertEquals(listOf(HomeNavKey, InsightsNavKey), navigator.backStack)
    }

    @Test
    fun `selecting Home clears back to the root`() {
        val navigator = navigator()
        navigator.navigateToTopLevel(TopLevelDestination.BUDGETS)
        navigator.navigate(SettingsNavKey)
        navigator.navigateToTopLevel(TopLevelDestination.HOME)
        assertEquals(listOf(HomeNavKey), navigator.backStack)
    }

    @Test
    fun `Settings is pushed and has no selected tab`() {
        val navigator = navigator()
        navigator.navigateToTopLevel(TopLevelDestination.TRANSACTIONS)
        navigator.navigate(SettingsNavKey)
        assertEquals(listOf(HomeNavKey, TransactionsNavKey, SettingsNavKey), navigator.backStack)
        assertNull(navigator.currentTopLevel)
    }

    @Test
    fun `back pops, but never removes Home`() {
        val navigator = navigator()
        navigator.navigateToTopLevel(TopLevelDestination.BUDGETS)
        navigator.goBack()
        assertEquals(listOf(HomeNavKey), navigator.backStack)
        navigator.goBack()
        assertEquals(listOf(HomeNavKey), navigator.backStack)
    }

    @Test
    fun `transaction detail and editor are pushed with no selected tab`() {
        val navigator = navigator()
        navigator.navigate(TransactionDetailNavKey(transactionId = 7))
        navigator.navigate(TransactionEditorNavKey(transactionId = 7))
        assertNull(navigator.currentTopLevel)
        navigator.goBack()
        assertEquals(listOf(HomeNavKey, TransactionDetailNavKey(7)), navigator.backStack)
    }
}
