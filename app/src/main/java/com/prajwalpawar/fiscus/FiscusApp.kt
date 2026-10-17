package com.prajwalpawar.fiscus

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallExtendedFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
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
import androidx.navigation.compose.composable
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
import com.prajwalpawar.fiscus.ui.navigation.FiscusDestination
import com.prajwalpawar.fiscus.ui.navigation.FiscusNavigationBar
import com.prajwalpawar.fiscus.ui.navigation.FiscusRoutes
import com.prajwalpawar.fiscus.ui.navigation.navigateToTab
import com.prajwalpawar.fiscus.ui.screens.manage.AccountEditorScreen
import com.prajwalpawar.fiscus.ui.screens.manage.AccountEditorViewModelFactory
import com.prajwalpawar.fiscus.ui.screens.manage.CategoryEditorScreen
import com.prajwalpawar.fiscus.ui.screens.manage.CategoryEditorViewModelFactory
import com.prajwalpawar.fiscus.ui.screens.manage.ManageAccountsScreen
import com.prajwalpawar.fiscus.ui.screens.manage.ManageCategoriesScreen
import com.prajwalpawar.fiscus.ui.screens.manage.ManageViewModelFactory
import com.prajwalpawar.fiscus.ui.screens.analysis.AnalysisScreen
import com.prajwalpawar.fiscus.ui.screens.home.HomeScreen
import com.prajwalpawar.fiscus.ui.screens.home.HomeViewModelFactory
import com.prajwalpawar.fiscus.ui.screens.settings.SettingsScreen
import com.prajwalpawar.fiscus.ui.screens.transaction.AddTransactionSheet
import com.prajwalpawar.fiscus.ui.screens.transaction.TransactionViewModel
import com.prajwalpawar.fiscus.ui.screens.transaction.TransactionViewModelFactory
import com.prajwalpawar.fiscus.ui.screens.transactions.TransactionsScreen
import com.prajwalpawar.fiscus.ui.theme.FiscusMotion
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

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

    // Tabs show the navigation bar; pages opened on top of them show a back arrow instead.
    val isTopLevel = route == null || listOf(
        FiscusDestination.Home,
        FiscusDestination.Transactions,
        FiscusDestination.Analysis,
        FiscusDestination.Settings
    ).any { route.startsWith(it.route) }

    val motionScheme = MaterialTheme.motionScheme

    val currentMonthLabel = remember {
        YearMonth.now().format(DateTimeFormatter.ofPattern("LLLL yyyy", Locale.getDefault()))
    }

    Scaffold(
        modifier = Modifier
            .nestedScroll(scrollBehavior.nestedScrollConnection)
            .nestedScroll(fabScrollConnection),
        topBar = {
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
                }
            )
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
            if (onHome) {
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
            composable(FiscusDestination.Home.route) {
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
                    }
                )
            }

            composable(FiscusDestination.Transactions.route) {
                TransactionsScreen()
            }

            composable(FiscusDestination.Analysis.route) {
                AnalysisScreen()
            }

            composable(FiscusDestination.Settings.route) {
                SettingsScreen(
                    onCategoriesClick = {
                        navController.navigate(FiscusRoutes.MANAGE_CATEGORIES)
                    },
                    onAccountsClick = {
                        navController.navigate(FiscusRoutes.MANAGE_ACCOUNTS)
                    }
                )
            }

            composable(
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

            composable(FiscusRoutes.NEW_ACCOUNT) {
                AccountEditorScreen(
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

            composable(FiscusRoutes.MANAGE_CATEGORIES) {
                ManageCategoriesScreen(
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

            composable(FiscusRoutes.MANAGE_ACCOUNTS) {
                ManageAccountsScreen(
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

            composable(
                route = FiscusRoutes.EDIT_CATEGORY,
                arguments = listOf(
                    navArgument(FiscusRoutes.ID_ARG) { type = NavType.LongType }
                )
            ) { entry ->
                val id = entry.arguments?.getLong(FiscusRoutes.ID_ARG) ?: 0L

                CategoryEditorScreen(
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

            composable(
                route = FiscusRoutes.EDIT_ACCOUNT,
                arguments = listOf(
                    navArgument(FiscusRoutes.ID_ARG) { type = NavType.LongType }
                )
            ) { entry ->
                val id = entry.arguments?.getLong(FiscusRoutes.ID_ARG) ?: 0L

                AccountEditorScreen(
                    viewModel = viewModel(
                        factory = AccountEditorViewModelFactory(accountRepository, editingId = id)
                    ),
                    onSaved = { navController.popBackStack() },
                    onDone = { navController.popBackStack() }
                )
            }
        }
    }

    // The sheet steps aside while a page is open on top and returns with the draft intact.
    if (showAddTransactionSheet && isTopLevel) {
        AddTransactionSheet(
            viewModel = transactionViewModel,
            onDismissRequest = {
                showAddTransactionSheet = false
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
