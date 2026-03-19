package com.prajwalpawar.budgetear

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.FragmentActivity
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.prajwalpawar.budgetear.ui.screens.analysis.AnalysisScreen
import com.prajwalpawar.budgetear.ui.screens.analysis.AnalysisViewModel
import com.prajwalpawar.budgetear.ui.screens.dashboard.DashboardScreen
import com.prajwalpawar.budgetear.ui.screens.dashboard.DashboardViewModel
import com.prajwalpawar.budgetear.ui.screens.settings.SettingsScreen
import com.prajwalpawar.budgetear.ui.screens.settings.SettingsViewModel
import com.prajwalpawar.budgetear.ui.screens.transactions.TransactionsScreen
import com.prajwalpawar.budgetear.ui.screens.transactions.TransactionsViewModel
import com.prajwalpawar.budgetear.ui.theme.BudgetearTheme
import dagger.hilt.android.AndroidEntryPoint
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
            val settingsState by settingsViewModel.uiState.collectAsStateWithLifecycle()
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

            BudgetearTheme(
                darkTheme = darkTheme,
                dynamicColor = settingsState.isDynamicColorEnabled
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (isAuthenticated) {
                        BudgetearAppContent(dashboardViewModel, settingsViewModel)
                    } else {
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
        val biometricPrompt = BiometricPrompt(
            this, executor,
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
            .setTitle("Authenticate")
            .setSubtitle("Access your budgetear")
            .setAllowedAuthenticators(
                BiometricManager.Authenticators.BIOMETRIC_STRONG or
                        BiometricManager.Authenticators.DEVICE_CREDENTIAL
            )
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
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination?.route

    NavigationSuiteScaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        navigationSuiteColors = NavigationSuiteDefaults.colors(
            navigationBarContainerColor = MaterialTheme.colorScheme.surfaceContainer,
            navigationBarContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        ),
        navigationSuiteItems = {
            item(
                selected = currentDestination == "dashboard",
                onClick = {
                    navController.navigate("dashboard") {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = {
                    Icon(
                        imageVector = if (currentDestination == "dashboard") Icons.Default.Dashboard else Icons.Default.Dashboard,
                        contentDescription = "Dashboard"
                    )
                },
                label = { Text("Overview") }
            )
            item(
                selected = currentDestination == "transactions",
                onClick = {
                    navController.navigate("transactions") {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = {
                    Icon(
                        imageVector = if (currentDestination == "transactions") Icons.Default.History else Icons.Default.History,
                        contentDescription = "Transactions"
                    )
                },
                label = { Text("History") }
            )
            item(
                selected = currentDestination == "analysis",
                onClick = {
                    navController.navigate("analysis") {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = {
                    Icon(
                        imageVector = if (currentDestination == "analysis") Icons.Default.Analytics else Icons.Default.Analytics,
                        contentDescription = "Analysis"
                    )
                },
                label = { Text("Analysis") }
            )
            item(
                selected = currentDestination == "settings",
                onClick = {
                    navController.navigate("settings") {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = {
                    Icon(
                        imageVector = if (currentDestination == "settings") Icons.Default.Settings else Icons.Default.Settings,
                        contentDescription = "Settings"
                    )
                },
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
                        navController.navigate("transactions") {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
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