package com.prajwalpawar.fiscus.ui

import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteItem
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldDefaults
import androidx.compose.material3.adaptive.navigationsuite.rememberNavigationSuiteScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.prajwalpawar.fiscus.ui.feature.budgets.BudgetsRoute
import com.prajwalpawar.fiscus.ui.feature.home.HomeRoute
import com.prajwalpawar.fiscus.ui.feature.insights.InsightsRoute
import com.prajwalpawar.fiscus.ui.feature.settings.SettingsRoute
import com.prajwalpawar.fiscus.ui.feature.transactions.TransactionsRoute
import com.prajwalpawar.fiscus.ui.navigation.BudgetsNavKey
import com.prajwalpawar.fiscus.ui.navigation.HomeNavKey
import com.prajwalpawar.fiscus.ui.navigation.InsightsNavKey
import com.prajwalpawar.fiscus.ui.navigation.SettingsNavKey
import com.prajwalpawar.fiscus.ui.navigation.TopLevelDestination
import com.prajwalpawar.fiscus.ui.navigation.TransactionsNavKey
import com.prajwalpawar.fiscus.ui.navigation.rememberFiscusNavigator

/**
 * App root: adaptive Expressive navigation (ShortNavigationBar on compact widths,
 * WideNavigationRail on larger ones) around a Navigation 3 [NavDisplay] (ADR-015).
 */
@Composable
fun FiscusApp(modifier: Modifier = Modifier) {
    val navigator = rememberFiscusNavigator()
    val currentTopLevel = navigator.currentTopLevel
    val navigationSuiteState = rememberNavigationSuiteScaffoldState()

    // Navigation is only shown on tab destinations; pushed screens (Settings) are full screen.
    LaunchedEffect(currentTopLevel) {
        if (currentTopLevel != null) navigationSuiteState.show() else navigationSuiteState.hide()
    }

    NavigationSuiteScaffold(
        modifier = modifier,
        state = navigationSuiteState,
        // Expressive types (ShortNavigationBar / WideNavigationRail), not calculateFromAdaptiveInfo.
        navigationSuiteType = NavigationSuiteScaffoldDefaults.navigationSuiteType(
            currentWindowAdaptiveInfo(),
        ),
        navigationItems = {
            TopLevelDestination.entries.forEach { destination ->
                val selected = destination == currentTopLevel
                NavigationSuiteItem(
                    selected = selected,
                    onClick = { navigator.navigateToTopLevel(destination) },
                    icon = {
                        Icon(
                            painter = painterResource(
                                if (selected) destination.selectedIcon else destination.icon,
                            ),
                            contentDescription = null, // the label describes the item
                        )
                    },
                    label = { Text(stringResource(destination.label)) },
                )
            }
        },
    ) {
        NavDisplay(
            backStack = navigator.backStack,
            onBack = navigator::goBack,
            entryDecorators = listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator(),
            ),
            entryProvider = entryProvider {
                val openSettings = { navigator.navigate(SettingsNavKey) }
                entry<HomeNavKey> { HomeRoute(onOpenSettings = openSettings) }
                entry<TransactionsNavKey> { TransactionsRoute(onOpenSettings = openSettings) }
                entry<BudgetsNavKey> { BudgetsRoute(onOpenSettings = openSettings) }
                entry<InsightsNavKey> { InsightsRoute(onOpenSettings = openSettings) }
                entry<SettingsNavKey> { SettingsRoute(onBack = navigator::goBack) }
            },
        )
    }
}
