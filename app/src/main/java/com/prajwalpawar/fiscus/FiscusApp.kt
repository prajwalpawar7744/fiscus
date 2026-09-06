package com.prajwalpawar.fiscus

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.prajwalpawar.fiscus.ui.navigation.FiscusDestination
import com.prajwalpawar.fiscus.ui.navigation.FiscusNavigationBar
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.prajwalpawar.fiscus.data.local.SettingsDataStore
import com.prajwalpawar.fiscus.data.model.AppBarScrollPreference
import com.prajwalpawar.fiscus.data.model.AppSettings
import com.prajwalpawar.fiscus.ui.components.FiscusTopAppBar
import com.prajwalpawar.fiscus.ui.screens.home.HomeScreen
import com.prajwalpawar.fiscus.ui.screens.settings.SettingsScreen
import androidx.compose.runtime.*
import com.prajwalpawar.fiscus.ui.screens.transaction.AddTransactionSheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FiscusApp () {
    val navController = rememberNavController()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val context = LocalContext.current
    val settingsDataStore = SettingsDataStore(context.applicationContext)
    val settings by settingsDataStore.settings.collectAsStateWithLifecycle(
        initialValue = AppSettings(),
        lifecycle = LocalLifecycleOwner.current.lifecycle
    )

    val scrollBehavior = when (settings.appBarScroll) {
       AppBarScrollPreference.PINNED ->
            TopAppBarDefaults.pinnedScrollBehavior()

        AppBarScrollPreference.ENTER_ALWAYS ->
            TopAppBarDefaults.enterAlwaysScrollBehavior()

        AppBarScrollPreference.EXIT_UNTIL_COLLAPSED ->
            TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    }

    var showAddTransactionSheet by rememberSaveable {
        mutableStateOf(false)
    }

    Scaffold(
        modifier = Modifier.nestedScroll(
          scrollBehavior.nestedScrollConnection
        ),
        topBar = {
            FiscusTopAppBar(
                title = when {
                    currentDestination
                        ?.route
                        ?.startsWith(FiscusDestination.Home.route) == true -> {
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
                    } else -> {
                        "Fiscus"
                    }
                },
                preference = settings.appBar,
                scrollBehavior = scrollBehavior
            )
        },
        bottomBar = {
            FiscusNavigationBar(
                navController = navController,
                currentDestination = currentDestination,
                preference = settings.bottomBar
            )
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = FiscusDestination.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(FiscusDestination.Home.route) {
                HomeScreen(
                    onAddTransactionClick = {
                        showAddTransactionSheet = true
                    }
                )

                if (showAddTransactionSheet) {
                    AddTransactionSheet(
                        onDismissRequest = {
                            showAddTransactionSheet = false
                        }
                    )
                }
            }

            composable(FiscusDestination.Transactions.route) {
                Text("Transactions")
            }

            composable(FiscusDestination.Analysis.route) {
                Text("Analysis")
            }

            composable(FiscusDestination.Settings.route) {
                SettingsScreen()
            }
        }
    }
}