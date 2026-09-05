package com.prajwalpawar.fiscus

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.prajwalpawar.fiscus.ui.navigation.FiscusDestination
import com.prajwalpawar.fiscus.ui.navigation.FiscusNavigationBar
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.prajwalpawar.fiscus.data.local.SettingsDataStore
import com.prajwalpawar.fiscus.data.model.AppSettings
import com.prajwalpawar.fiscus.ui.components.FiscusTopAppBar
import com.prajwalpawar.fiscus.ui.screens.settings.SettingsScreen

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

    Scaffold(
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
                preference = settings.appBar
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
                Text("Home")
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