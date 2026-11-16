package com.prajwalpawar.fiscus.ui

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteItem
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldDefaults
import androidx.compose.material3.adaptive.navigationsuite.rememberNavigationSuiteScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.prajwalpawar.fiscus.model.TransactionType
import com.prajwalpawar.fiscus.ui.components.LocalSharedTransitionScope
import com.prajwalpawar.fiscus.ui.components.LocalSnackbarHostState
import com.prajwalpawar.fiscus.ui.components.navVisibilityScopeDecorator
import com.prajwalpawar.fiscus.ui.feature.budgets.BudgetsRoute
import com.prajwalpawar.fiscus.ui.feature.accountdetail.AccountDetailRoute
import com.prajwalpawar.fiscus.ui.feature.accounteditor.AccountEditorRoute
import com.prajwalpawar.fiscus.ui.feature.accounts.AccountsRoute
import com.prajwalpawar.fiscus.ui.feature.categories.CategoriesRoute
import com.prajwalpawar.fiscus.ui.feature.categorydetail.CategoryDetailRoute
import com.prajwalpawar.fiscus.ui.feature.categoryeditor.CategoryEditorRoute
import com.prajwalpawar.fiscus.ui.feature.home.HomeRoute
import com.prajwalpawar.fiscus.ui.feature.insights.InsightsRoute
import com.prajwalpawar.fiscus.ui.feature.settings.SettingsRoute
import com.prajwalpawar.fiscus.ui.feature.transactiondetail.TransactionDetailRoute
import com.prajwalpawar.fiscus.ui.feature.transactioneditor.TransactionEditorRoute
import com.prajwalpawar.fiscus.ui.feature.transactions.TransactionsRoute
import com.prajwalpawar.fiscus.ui.navigation.AccountDetailNavKey
import com.prajwalpawar.fiscus.ui.navigation.AccountEditorNavKey
import com.prajwalpawar.fiscus.ui.navigation.AccountsNavKey
import com.prajwalpawar.fiscus.ui.navigation.CategoriesNavKey
import com.prajwalpawar.fiscus.ui.navigation.CategoryDetailNavKey
import com.prajwalpawar.fiscus.ui.navigation.CategoryEditorNavKey
import com.prajwalpawar.fiscus.ui.navigation.BudgetsNavKey
import com.prajwalpawar.fiscus.ui.navigation.HomeNavKey
import com.prajwalpawar.fiscus.ui.navigation.InsightsNavKey
import com.prajwalpawar.fiscus.ui.navigation.SettingsNavKey
import com.prajwalpawar.fiscus.ui.navigation.TopLevelDestination
import com.prajwalpawar.fiscus.ui.navigation.TransactionDetailNavKey
import com.prajwalpawar.fiscus.ui.navigation.TransactionEditorNavKey
import com.prajwalpawar.fiscus.ui.navigation.TransactionsNavKey
import com.prajwalpawar.fiscus.ui.navigation.rememberFiscusNavigator
import kotlinx.coroutines.launch

/**
 * App root: adaptive Expressive navigation (ShortNavigationBar on compact widths,
 * WideNavigationRail on larger ones) around a Navigation 3 [NavDisplay] (ADR-015).
 */
@Composable
fun FiscusApp(modifier: Modifier = Modifier) {
    val motion = MaterialTheme.motionScheme
    val navigator = rememberFiscusNavigator()
    val currentTopLevel = navigator.currentTopLevel
    val navigationSuiteState = rememberNavigationSuiteScaffoldState()
    // One host for confirmations ("Expense added"), provided app-wide (ADR-020).
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    // Closes a pushed screen, then confirms what happened on the screen underneath.
    val finishWith: (String?) -> Unit = { message ->
        navigator.goBack()
        if (message != null) scope.launch { snackbarHostState.showSnackbar(message) }
    }

    // Navigation is only shown on tab destinations; pushed screens (Settings) are full screen.
    LaunchedEffect(currentTopLevel) {
        if (currentTopLevel != null) navigationSuiteState.show() else navigationSuiteState.hide()
    }

    CompositionLocalProvider(LocalSnackbarHostState provides snackbarHostState) {
        NavigationSuiteScaffold(
            modifier = modifier,
            state = navigationSuiteState,
            // Expressive types (ShortNavigationBar / WideNavigationRail), not calculateFromAdaptiveInfo.
            navigationSuiteType = NavigationSuiteScaffoldDefaults.navigationSuiteType(
                currentWindowAdaptiveInfoV2(),
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
            // Shared bounds let a list item grow into its page and shrink back (ADR-028).
            SharedTransitionLayout {
                CompositionLocalProvider(LocalSharedTransitionScope provides this) {
                    NavDisplay(
                        backStack = navigator.backStack,
                        onBack = navigator::goBack,
                        entryDecorators = listOf(
                            rememberSaveableStateHolderNavEntryDecorator(),
                            rememberViewModelStoreNavEntryDecorator(),
                            navVisibilityScopeDecorator(),
                        ),
                        // Same cross-fade as Navigation 3's default, timed by the theme's
                        // M3 Expressive effects springs instead of its 700 ms tween (ADR-030).
                        transitionSpec = { pageCrossFade(motion) },
                        popTransitionSpec = { pageCrossFade(motion) },
                        predictivePopTransitionSpec = { _ -> pageCrossFade(motion) },
                        entryProvider = entryProvider {
                            val openSettings = { navigator.navigate(SettingsNavKey) }
                            val addTransaction = { type: TransactionType ->
                                navigator.navigate(TransactionEditorNavKey(type = type))
                            }
                            entry<HomeNavKey> {
                                HomeRoute(
                                    onOpenSettings = openSettings,
                                    onAddTransaction = addTransaction,
                                    onOpenTransaction = { id ->
                                        navigator.navigate(TransactionDetailNavKey(id))
                                    },
                                    onOpenAccount = { id ->
                                        navigator.navigate(AccountDetailNavKey(id))
                                    },
                                    onManageAccounts = { navigator.navigate(AccountsNavKey) },
                                    onSeeAllTransactions = {
                                        navigator.navigateToTopLevel(
                                            TopLevelDestination.TRANSACTIONS,
                                        )
                                    },
                                )
                            }
                            entry<TransactionsNavKey> {
                                TransactionsRoute(
                                    onOpenSettings = openSettings,
                                    onAddTransaction = addTransaction,
                                    onOpenTransaction = { id ->
                                        navigator.navigate(TransactionDetailNavKey(id))
                                    },
                                )
                            }
                            entry<BudgetsNavKey> { BudgetsRoute(onOpenSettings = openSettings) }
                            entry<InsightsNavKey> { InsightsRoute(onOpenSettings = openSettings) }
                            entry<SettingsNavKey> {
                                SettingsRoute(
                                    onBack = navigator::goBack,
                                    onOpenAccounts = { navigator.navigate(AccountsNavKey) },
                                    onOpenCategories = { navigator.navigate(CategoriesNavKey) },
                                )
                            }
                            entry<CategoriesNavKey> {
                                CategoriesRoute(
                                    onBack = navigator::goBack,
                                    onOpenCategory = { id ->
                                        navigator.navigate(CategoryDetailNavKey(id))
                                    },
                                    onAddCategory = { kind ->
                                        navigator.navigate(CategoryEditorNavKey(kind = kind))
                                    },
                                )
                            }
                            entry<CategoryDetailNavKey> { key ->
                                CategoryDetailRoute(
                                    navKey = key,
                                    onBack = navigator::goBack,
                                    onEdit = { id ->
                                        navigator.navigate(CategoryEditorNavKey(categoryId = id))
                                    },
                                    onFinished = finishWith,
                                )
                            }
                            entry<CategoryEditorNavKey> { key ->
                                CategoryEditorRoute(
                                    navKey = key,
                                    onBack = navigator::goBack,
                                    onFinished = finishWith,
                                )
                            }
                            entry<TransactionEditorNavKey> { key ->
                                TransactionEditorRoute(
                                    navKey = key,
                                    onBack = navigator::goBack,
                                    onFinished = finishWith,
                                )
                            }
                            entry<AccountsNavKey> {
                                AccountsRoute(
                                    onBack = navigator::goBack,
                                    onOpenAccount = { id ->
                                        navigator.navigate(AccountDetailNavKey(id))
                                    },
                                    onAddAccount = { navigator.navigate(AccountEditorNavKey()) },
                                )
                            }
                            entry<AccountDetailNavKey> { key ->
                                AccountDetailRoute(
                                    navKey = key,
                                    onBack = navigator::goBack,
                                    onEdit = { id ->
                                        navigator.navigate(AccountEditorNavKey(accountId = id))
                                    },
                                    onFinished = finishWith,
                                )
                            }
                            entry<AccountEditorNavKey> { key ->
                                AccountEditorRoute(
                                    navKey = key,
                                    onBack = navigator::goBack,
                                    onFinished = finishWith,
                                )
                            }
                            entry<TransactionDetailNavKey> { key ->
                                TransactionDetailRoute(
                                    navKey = key,
                                    onBack = navigator::goBack,
                                    onEdit = { id ->
                                        navigator.navigate(TransactionEditorNavKey(transactionId = id))
                                    },
                                    onFinished = finishWith,
                                )
                            }
                        },
                    )
                }
            }
        }
    }
}

/**
 * Page-to-page motion (ADR-030): the entering page fades in on the default effects spring while
 * the leaving one fades out on the fast one (exits are quicker than enters, M3 motion guidance).
 */
private fun pageCrossFade(motion: MotionScheme): ContentTransform =
    fadeIn(motion.defaultEffectsSpec()) togetherWith fadeOut(motion.fastEffectsSpec())
