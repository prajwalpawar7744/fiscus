package com.prajwalpawar.budgetear

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.prajwalpawar.budgetear.ui.theme.BudgetearTheme
import dagger.hilt.android.AndroidEntryPoint

import androidx.compose.material3.*
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.*
import com.prajwalpawar.budgetear.ui.screens.dashboard.DashboardScreen
import com.prajwalpawar.budgetear.ui.screens.dashboard.DashboardViewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val dashboardViewModel: DashboardViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BudgetearTheme {
                BudgetearAppContent(dashboardViewModel)
            }
        }
    }
}

@Composable
fun BudgetearAppContent(dashboardViewModel: DashboardViewModel) {
    val navController = rememberNavController()
    var currentDestination by remember { mutableStateOf("dashboard") }

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            item(
                selected = currentDestination == "dashboard",
                onClick = {
                    currentDestination = "dashboard"
                    navController.navigate("dashboard")
                },
                icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
                label = { Text("Dashboard") }
            )
            item(
                selected = currentDestination == "transactions",
                onClick = {
                    currentDestination = "transactions"
                    // navController.navigate("transactions")
                },
                icon = { Icon(Icons.Default.History, contentDescription = "Transactions") },
                label = { Text("Transactions") }
            )
            item(
                selected = currentDestination == "settings",
                onClick = {
                    currentDestination = "settings"
                    // navController.navigate("settings")
                },
                icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                label = { Text("Settings") }
            )
        }
    ) {
        NavHost(
            navController = navController,
            startDestination = "dashboard",
            modifier = Modifier.fillMaxSize()
        ) {
            composable("dashboard") {
                DashboardScreen(
                    viewModel = dashboardViewModel
                )
            }
        }
    }
}