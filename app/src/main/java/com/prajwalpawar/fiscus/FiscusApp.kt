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
import com.prajwalpawar.fiscus.ui.components.FiscusTopAppBar

@Composable
fun FiscusApp () {
    val navController = rememberNavController()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

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
                }
            )
        },
        bottomBar = {
            FiscusNavigationBar(
                navController = navController,
                currentDestination = currentDestination
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
                Text("Settings")
            }
        }
    }
}