package com.prajwalpawar.budgetear

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
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
import com.prajwalpawar.budgetear.ui.screens.transactions.TransactionsScreen
import com.prajwalpawar.budgetear.ui.screens.transactions.TransactionsViewModel
import com.prajwalpawar.budgetear.ui.screens.analysis.AnalysisScreen
import com.prajwalpawar.budgetear.ui.screens.analysis.AnalysisViewModel
import com.prajwalpawar.budgetear.ui.screens.settings.SettingsScreen
import com.prajwalpawar.budgetear.ui.screens.settings.SettingsViewModel
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Settings
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

import androidx.fragment.app.FragmentActivity
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import java.util.concurrent.Executor

@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    private val dashboardViewModel: DashboardViewModel by viewModels()
    private val settingsViewModel: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val settingsState by settingsViewModel.uiState.collectAsState()
            var isAuthenticated by remember { mutableStateOf(!settingsState.isBiometricEnabled) }
            
            // Re-check authentication if biometric setting changes to true
            LaunchedEffect(settingsState.isBiometricEnabled) {
                if (settingsState.isBiometricEnabled) {
                    isAuthenticated = false
                    showBiometricPrompt { authenticated ->
                        isAuthenticated = authenticated
                    }
                } else {
                    isAuthenticated = true
                }
            }

            val darkTheme = when (settingsState.themeMode) {
                "light" -> false
                "dark" -> true
                else -> isSystemInDarkTheme()
            }
            
            BudgetearTheme(darkTheme = darkTheme) {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    if (isAuthenticated) {
                        BudgetearAppContent(dashboardViewModel, settingsViewModel)
                    } else {
                        // Optional: Show a locked screen or just empty surface
                        Box(contentAlignment = Alignment.Center) {
                            Button(onClick = { 
                                showBiometricPrompt { authenticated ->
                                    isAuthenticated = authenticated
                                }
                            }) {
                                Text("Unlock Budgetear")
                            }
                        }
                    }
                }
            }
        }
    }

    private fun showBiometricPrompt(onResult: (Boolean) -> Unit) {
        val executor: Executor = ContextCompat.getMainExecutor(this)
        val biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    onResult(true)
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    onResult(false)
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    onResult(false)
                }
            })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Biometric login for Budgetear")
            .setSubtitle("Log in using your biometric credential")
            .setNegativeButtonText("Cancel")
            .build()

        biometricPrompt.authenticate(promptInfo)
    }
}

@Composable
fun BudgetearAppContent(
    dashboardViewModel: DashboardViewModel,
    settingsViewModel: SettingsViewModel
) {
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
                    navController.navigate("transactions") {
                        popUpTo("dashboard") { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = { Icon(Icons.Default.History, contentDescription = "Transactions") },
                label = { Text("Transactions") }
            )
            item(
                selected = currentDestination == "analysis",
                onClick = {
                    currentDestination = "analysis"
                    navController.navigate("analysis") {
                        popUpTo("dashboard") { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = { Icon(Icons.Default.Analytics, contentDescription = "Analysis") },
                label = { Text("Analysis") }
            )
            item(
                selected = currentDestination == "settings",
                onClick = {
                    currentDestination = "settings"
                    navController.navigate("settings") {
                        popUpTo("dashboard") { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
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
                    viewModel = dashboardViewModel,
                    onSeeAllTransactions = {
                        currentDestination = "transactions"
                        navController.navigate("transactions")
                    }
                )
            }
            composable("transactions") {
                val transactionsViewModel: TransactionsViewModel = hiltViewModel()
                TransactionsScreen(viewModel = transactionsViewModel)
            }
            composable("analysis") {
                val analysisViewModel: AnalysisViewModel = hiltViewModel()
                AnalysisScreen(viewModel = analysisViewModel)
            }
            composable("settings") {
                SettingsScreen(viewModel = settingsViewModel)
            }
        }
    }
}