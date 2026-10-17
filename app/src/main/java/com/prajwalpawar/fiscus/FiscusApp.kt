package com.prajwalpawar.fiscus

import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallExtendedFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import androidx.navigation.compose.rememberNavController
import com.prajwalpawar.fiscus.data.local.FiscusDatabaseProvider
import com.prajwalpawar.fiscus.data.model.AppBarScrollPreference
import com.prajwalpawar.fiscus.data.model.AppSettings
import com.prajwalpawar.fiscus.data.model.CategoryType
import com.prajwalpawar.fiscus.data.model.TransactionType
import com.prajwalpawar.fiscus.data.repository.AccountRepository
import com.prajwalpawar.fiscus.data.repository.CategoryRepository
import com.prajwalpawar.fiscus.data.repository.TransactionRepository
import com.prajwalpawar.fiscus.ui.components.FiscusTopAppBar
import com.prajwalpawar.fiscus.ui.components.monthLabel
import com.prajwalpawar.fiscus.ui.components.ImeInsetsRepair
import com.prajwalpawar.fiscus.ui.navigation.FiscusDestination
import com.prajwalpawar.fiscus.ui.navigation.FiscusNavigationBar
import com.prajwalpawar.fiscus.ui.navigation.ContainerKeys
import com.prajwalpawar.fiscus.ui.navigation.FiscusRoutes
import com.prajwalpawar.fiscus.ui.navigation.LocalSharedTransitionScope
import com.prajwalpawar.fiscus.ui.navigation.containerTransform
import com.prajwalpawar.fiscus.ui.navigation.fiscusComposable
import com.prajwalpawar.fiscus.ui.navigation.navigateToTab
import com.prajwalpawar.fiscus.ui.screens.manage.AccountEditorScreen
import com.prajwalpawar.fiscus.ui.screens.manage.AccountEditorViewModelFactory
import com.prajwalpawar.fiscus.ui.screens.manage.CategoryEditorScreen
import com.prajwalpawar.fiscus.ui.screens.manage.CategoryEditorViewModelFactory
import com.prajwalpawar.fiscus.ui.screens.manage.ManageAccountsScreen
import com.prajwalpawar.fiscus.ui.screens.manage.ManageCategoriesScreen
import com.prajwalpawar.fiscus.ui.screens.manage.ManageViewModelFactory
import com.prajwalpawar.fiscus.ui.screens.analysis.AnalysisScreen
import com.prajwalpawar.fiscus.ui.screens.analysis.AnalysisViewModelFactory
import com.prajwalpawar.fiscus.ui.screens.home.HomeScreen
import com.prajwalpawar.fiscus.ui.screens.home.HomeViewModelFactory
import com.prajwalpawar.fiscus.ui.screens.settings.SettingsScreen
import com.prajwalpawar.fiscus.ui.screens.transaction.AddTransactionSheet
import com.prajwalpawar.fiscus.ui.screens.transaction.TransactionDetailsScreen
import com.prajwalpawar.fiscus.ui.screens.transaction.TransactionDetailsViewModelFactory
import com.prajwalpawar.fiscus.ui.screens.transaction.TransactionViewModel
import com.prajwalpawar.fiscus.ui.screens.transaction.TransactionViewModelFactory
import com.prajwalpawar.fiscus.ui.screens.transactions.TransactionSearchScreen
import com.prajwalpawar.fiscus.ui.screens.transactions.TransactionSearchViewModelFactory
import com.prajwalpawar.fiscus.ui.screens.transactions.TransactionsScreen
import com.prajwalpawar.fiscus.ui.screens.transactions.TransactionsViewModelFactory
import com.prajwalpawar.fiscus.ui.theme.FiscusMotion
import java.time.YearMonth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FiscusApp (
    settings: AppSettings
) {
    val navController = rememberNavController()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val context = LocalContext.current

    val scrollBehavior = when (settings.appBarScroll) {
       AppBarScrollPreference.PINNED ->
            TopAppBarDefaults.pinnedScrollBehavior()

        AppBarScrollPreference.ENTER_ALWAYS ->
            TopAppBarDefaults.enterAlwaysScrollBehavior()

        AppBarScrollPreference.EXIT_UNTIL_COLLAPSED ->
            TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    }

    // Shrink the FAB to its icon while scrolling down, restore it when scrolling up.
    var fabExpanded by rememberSaveable {
        mutableStateOf(true)
    }

    val fabScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                if (available.y < 0f) fabExpanded = false
                if (available.y > 0f) fabExpanded = true
                return Offset.Zero
            }
        }
    }

    var showAddTransactionSheet by rememberSaveable {
        mutableStateOf(false)
    }

    val database = remember(context) {
        FiscusDatabaseProvider.getDatabase(context)
    }

    val accountRepository = remember(database) {
        AccountRepository(database.accountDao())
    }

    val categoryRepository = remember(database) {
        CategoryRepository(database.categoryDao())
    }

    val transactionRepository = remember(database) {
        TransactionRepository(database, database.transactionDao())
    }

    val transactionViewModelFactory = remember(
        accountRepository,
        categoryRepository,
        transactionRepository
    ) {
        TransactionViewModelFactory(
            accountRepository = accountRepository,
            categoryRepository = categoryRepository,
            transactionRepository = transactionRepository
        )
    }

    // Activity-scoped, so the draft survives visits to other pages.
    val transactionViewModel: TransactionViewModel = viewModel(
        factory = transactionViewModelFactory
    )

    // Where a new account from the sheet should land: the "To" row or the main one.
    var newAccountIsTransferTarget by rememberSaveable {
        mutableStateOf(false)
    }

    val route = currentDestination?.route

    val onHome = route?.startsWith(FiscusDestination.Home.route) == true
    val onTransactions = route?.startsWith(FiscusDestination.Transactions.route) == true
    val onSearch = route == FiscusRoutes.TRANSACTION_SEARCH

    // Tabs show the navigation bar; pages opened on top of them show a back arrow instead.
    val isTopLevel = route == null || listOf(
        FiscusDestination.Home,
        FiscusDestination.Transactions,
        FiscusDestination.Analysis,
        FiscusDestination.Settings
    ).any { route.startsWith(it.route) }

    val motionScheme = MaterialTheme.motionScheme

    // Keeps the keyboard's padding from sticking after a back-swipe closes it.
    ImeInsetsRepair()

    val currentMonthLabel = remember {
        monthLabel(YearMonth.now())
    }

    Scaffold(
        modifier = Modifier
            .nestedScroll(scrollBehavior.nestedScrollConnection)
            .nestedScroll(fabScrollConnection),
        topBar = {
            // The search page draws its own search field as the header.
            if (!onSearch) {
                FiscusTopAppBar(
                    title = when {
                        onHome -> {
                                "Home"
                            }

                        currentDestination
                            ?.route
                            ?.startsWith(FiscusDestination.Transactions.route) == true -> {
                            "Transactions"
                        }

                        currentDestination
                            ?.route
                            ?.startsWith(FiscusDestination.Analysis.route) == true -> {
                            "Analysis"
                        }

                        currentDestination
                            ?.route
                            ?.startsWith(FiscusDestination.Settings.route) == true -> {
                            "Settings"
                        }

                        route?.startsWith(FiscusRoutes.NEW_CATEGORY_BASE) == true -> {
                            "New category"
                        }

                        route?.startsWith(FiscusRoutes.NEW_ACCOUNT) == true -> {
                            "New account"
                        }

                        route?.startsWith(FiscusRoutes.EDIT_CATEGORY_BASE) == true -> {
                            "Edit category"
                        }

                        route?.startsWith(FiscusRoutes.EDIT_ACCOUNT_BASE) == true -> {
                            "Edit account"
                        }

                        route == FiscusRoutes.MANAGE_CATEGORIES -> {
                            "Categories"
                        }

                        route == FiscusRoutes.MANAGE_ACCOUNTS -> {
                            "Accounts"
                        }

                        route?.startsWith(FiscusRoutes.TRANSACTION_DETAILS_BASE) == true -> {
                            "Transaction"
                        } else -> {
                            "Fiscus"
                        }
                    },
                    preference = settings.appBar,
                    scrollBehavior = scrollBehavior,
                    // Home's figures are for this month, so say which one.
                    subtitle = if (onHome) currentMonthLabel else null,
                    onNavigateUp = if (isTopLevel) {
                        null
                    } else {
                        { navController.navigateUp() }
                    },
                    actions = {
                        if (onTransactions) {
                            IconButton(
                                onClick = { navController.navigate(FiscusRoutes.TRANSACTION_SEARCH) }
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Search,
                                    contentDescription = "Search transactions"
                                )
                            }
                        }
                    }
                )
            }
        },
        bottomBar = {
            if (isTopLevel) {
                FiscusNavigationBar(
                    navController = navController,
                    currentDestination = currentDestination,
                    preference = settings.bottomBar
                )
            }
        },
        floatingActionButton = {
            if (onHome || onTransactions) {
                SmallExtendedFloatingActionButton(
                    onClick = {
                        showAddTransactionSheet = true
                    },
                    expanded = fabExpanded,
                    icon = {
                        // The text names the action; collapsed, the icon needs its own label.
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = if (fabExpanded) null else "Add transaction"
                        )
                    },
                    text = {
                        Text("Add transaction")
                    }
                )
            }
        }
    ) { innerPadding ->
        // Lets a tapped row or card grow into the page it opens (container transform).
        SharedTransitionLayout {
            CompositionLocalProvider(LocalSharedTransitionScope provides this) {
                NavHost(
                    navController = navController,
                    startDestination = FiscusDestination.Home.route,
                    modifier = Modifier.padding(innerPadding),
                    // Fade through between tabs, timed by the Expressive motion scheme.
                    enterTransition = {
                        fadeIn(motionScheme.defaultEffectsSpec()) +
                                scaleIn(
                                    animationSpec = motionScheme.defaultSpatialSpec(),
                                    initialScale = FiscusMotion.ENTER_SCALE
                                )
                    },
                    exitTransition = {
                        fadeOut(motionScheme.fastEffectsSpec())
                    }
                ) {
                    fiscusComposable(FiscusDestination.Home.route) {
                        HomeScreen(
                            viewModel = viewModel(
                                factory = HomeViewModelFactory(accountRepository, transactionRepository)
                            ),
                            onSeeAllClick = {
                                navController.navigateToTab(FiscusDestination.Transactions)
                            },
                            onAddAccountClick = {
                                navController.navigate(FiscusRoutes.NEW_ACCOUNT)
                            },
                            onAccountClick = { id ->
                                navController.navigate(FiscusRoutes.editAccount(id))
                            },
                            onTransactionClick = { id ->
                                navController.navigate(FiscusRoutes.transactionDetails(id))
                            }
                        )
                    }

                    fiscusComposable(FiscusDestination.Transactions.route) {
                        TransactionsScreen(
                            viewModel = viewModel(
                                factory = TransactionsViewModelFactory(
                                    transactionRepository = transactionRepository,
                                    accountRepository = accountRepository,
                                    categoryRepository = categoryRepository
                                )
                            ),
                            onTransactionClick = { id ->
                                navController.navigate(FiscusRoutes.transactionDetails(id))
                            }
                        )
                    }

                    fiscusComposable(FiscusRoutes.TRANSACTION_SEARCH) {
                        TransactionSearchScreen(
                            viewModel = viewModel(
                                factory = TransactionSearchViewModelFactory(transactionRepository)
                            ),
                            onBack = { navController.navigateUp() },
                            onResultClick = { id ->
                                navController.navigate(FiscusRoutes.transactionDetails(id))
                            }
                        )
                    }

                    fiscusComposable(FiscusDestination.Analysis.route) {
                        AnalysisScreen(
                            viewModel = viewModel(
                                factory = AnalysisViewModelFactory(transactionRepository)
                            ),
                            onTransactionClick = { id ->
                                navController.navigate(FiscusRoutes.transactionDetails(id))
                            }
                        )
                    }

                    fiscusComposable(FiscusDestination.Settings.route) {
                        SettingsScreen(
                            onCategoriesClick = {
                                navController.navigate(FiscusRoutes.MANAGE_CATEGORIES)
                            },
                            onAccountsClick = {
                                navController.navigate(FiscusRoutes.MANAGE_ACCOUNTS)
                            }
                        )
                    }

                    fiscusComposable(
                        route = FiscusRoutes.NEW_CATEGORY,
                        arguments = listOf(
                            navArgument(FiscusRoutes.TYPE_ARG) {
                                type = NavType.StringType
                                defaultValue = CategoryType.EXPENSE.name
                            }
                        )
                    ) { entry ->
                        val initialType = entry.arguments
                            ?.getString(FiscusRoutes.TYPE_ARG)
                            ?.let { runCatching { CategoryType.valueOf(it) }.getOrNull() }
                            ?: CategoryType.EXPENSE

                        CategoryEditorScreen(
                            modifier = Modifier.containerTransform(ContainerKeys.newCategory(initialType.name)),
                            viewModel = viewModel(
                                factory = CategoryEditorViewModelFactory(categoryRepository, initialType)
                            ),
                            onDone = { navController.popBackStack() },
                            onSaved = { id, type ->
                                // Opened from the sheet: pick the new category in the draft.
                                if (showAddTransactionSheet) {
                                    transactionViewModel.setTransactionType(
                                        if (type == CategoryType.INCOME) {
                                            TransactionType.INCOME
                                        } else {
                                            TransactionType.EXPENSE
                                        }
                                    )
                                    transactionViewModel.selectCategory(id)
                                }
                                navController.popBackStack()
                            }
                        )
                    }

                    fiscusComposable(FiscusRoutes.NEW_ACCOUNT) {
                        AccountEditorScreen(
                            modifier = Modifier.containerTransform(ContainerKeys.NEW_ACCOUNT),
                            viewModel = viewModel(
                                factory = AccountEditorViewModelFactory(accountRepository)
                            ),
                            onDone = { navController.popBackStack() },
                            onSaved = { id ->
                                // Opened from the sheet: pick the new account in the row it came from.
                                if (showAddTransactionSheet) {
                                    if (newAccountIsTransferTarget) {
                                        transactionViewModel.selectToAccount(id)
                                    } else {
                                        transactionViewModel.selectAccount(id)
                                    }
                                }
                                navController.popBackStack()
                            }
                        )
                    }

                    fiscusComposable(
                        route = FiscusRoutes.TRANSACTION_DETAILS,
                        arguments = listOf(
                            navArgument(FiscusRoutes.ID_ARG) { type = NavType.LongType }
                        )
                    ) { entry ->
                        val id = entry.arguments?.getLong(FiscusRoutes.ID_ARG) ?: 0L

                        TransactionDetailsScreen(
                            modifier = Modifier.containerTransform(ContainerKeys.transaction(id)),
                            viewModel = viewModel(
                                factory = TransactionDetailsViewModelFactory(transactionRepository, id)
                            ),
                            onEditClick = { transactionId ->
                                // Open the sheet once the transaction is loaded into it.
                                transactionViewModel.startEditing(transactionId) {
                                    showAddTransactionSheet = true
                                }
                            },
                            onDeleted = { navController.popBackStack() }
                        )
                    }

                    fiscusComposable(FiscusRoutes.MANAGE_CATEGORIES) {
                        ManageCategoriesScreen(
                            modifier = Modifier.containerTransform(ContainerKeys.MANAGE_CATEGORIES),
                            viewModel = viewModel(
                                factory = ManageViewModelFactory(categoryRepository, accountRepository)
                            ),
                            onCategoryClick = { id ->
                                navController.navigate(FiscusRoutes.editCategory(id))
                            },
                            onAddCategoryClick = { type ->
                                navController.navigate(FiscusRoutes.newCategory(type))
                            }
                        )
                    }

                    fiscusComposable(FiscusRoutes.MANAGE_ACCOUNTS) {
                        ManageAccountsScreen(
                            modifier = Modifier.containerTransform(ContainerKeys.MANAGE_ACCOUNTS),
                            viewModel = viewModel(
                                factory = ManageViewModelFactory(categoryRepository, accountRepository)
                            ),
                            onAccountClick = { id ->
                                navController.navigate(FiscusRoutes.editAccount(id))
                            },
                            onAddAccountClick = {
                                navController.navigate(FiscusRoutes.NEW_ACCOUNT)
                            }
                        )
                    }

                    fiscusComposable(
                        route = FiscusRoutes.EDIT_CATEGORY,
                        arguments = listOf(
                            navArgument(FiscusRoutes.ID_ARG) { type = NavType.LongType }
                        )
                    ) { entry ->
                        val id = entry.arguments?.getLong(FiscusRoutes.ID_ARG) ?: 0L

                        CategoryEditorScreen(
                            modifier = Modifier.containerTransform(ContainerKeys.category(id)),
                            viewModel = viewModel(
                                factory = CategoryEditorViewModelFactory(
                                    categoryRepository = categoryRepository,
                                    initialType = CategoryType.EXPENSE,
                                    editingId = id
                                )
                            ),
                            onSaved = { _, _ -> navController.popBackStack() },
                            onDone = { navController.popBackStack() }
                        )
                    }

                    fiscusComposable(
                        route = FiscusRoutes.EDIT_ACCOUNT,
                        arguments = listOf(
                            navArgument(FiscusRoutes.ID_ARG) { type = NavType.LongType }
                        )
                    ) { entry ->
                        val id = entry.arguments?.getLong(FiscusRoutes.ID_ARG) ?: 0L

                        AccountEditorScreen(
                            modifier = Modifier.containerTransform(ContainerKeys.account(id)),
                            viewModel = viewModel(
                                factory = AccountEditorViewModelFactory(accountRepository, editingId = id)
                            ),
                            onSaved = { navController.popBackStack() },
                            onDone = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }

    // The sheet also opens over a transaction's details page, to edit it.
    val sheetCanShow = isTopLevel ||
            route?.startsWith(FiscusRoutes.TRANSACTION_DETAILS_BASE) == true

    // The sheet steps aside while a page is open on top and returns with the draft intact.
    if (showAddTransactionSheet && sheetCanShow) {
        AddTransactionSheet(
            viewModel = transactionViewModel,
            onDismissRequest = {
                showAddTransactionSheet = false
                // Closing an edit discards its changes and brings back any "add" draft.
                transactionViewModel.cancelEditing()
            },
            onAddCategory = { type ->
                navController.navigate(FiscusRoutes.newCategory(type))
            },
            onAddAccount = { asTransferTarget ->
                newAccountIsTransferTarget = asTransferTarget
                navController.navigate(FiscusRoutes.NEW_ACCOUNT)
            }
        )
    }
}
