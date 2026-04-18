package com.prajwalpawar.fiscus

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
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.text.font.FontWeight
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.prajwalpawar.fiscus.ui.utils.FiscusAnimation
import com.prajwalpawar.fiscus.ui.screens.analysis.AnalysisScreen
import com.prajwalpawar.fiscus.ui.screens.analysis.AnalysisViewModel
import com.prajwalpawar.fiscus.ui.screens.dashboard.DashboardScreen
import com.prajwalpawar.fiscus.ui.screens.dashboard.DashboardViewModel
import com.prajwalpawar.fiscus.ui.screens.settings.SettingsScreen
import com.prajwalpawar.fiscus.ui.screens.settings.SettingsViewModel
import com.prajwalpawar.fiscus.ui.screens.transactions.TransactionsScreen
import com.prajwalpawar.fiscus.ui.screens.transactions.TransactionsViewModel
import com.prajwalpawar.fiscus.ui.screens.accounts.AccountsScreen
import com.prajwalpawar.fiscus.ui.screens.accounts.AccountsViewModel
import com.prajwalpawar.fiscus.ui.screens.categories.ManageCategoriesScreen
import com.prajwalpawar.fiscus.ui.screens.categories.ManageCategoriesViewModel
import com.prajwalpawar.fiscus.ui.theme.FiscusTheme
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.Executor

@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    private val dashboardViewModel: DashboardViewModel by viewModels()
    private val settingsViewModel: SettingsViewModel by viewModels()
    private val transactionsViewModel: TransactionsViewModel by viewModels()
    private val analysisViewModel: AnalysisViewModel by viewModels()
    private val accountsViewModel: AccountsViewModel by viewModels()
    private val categoriesViewModel: ManageCategoriesViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        
        var isSettingsLoaded = false
        
        // Keep the splash screen on-screen until the settings are loaded.
        splashScreen.setKeepOnScreenCondition { !isSettingsLoaded }

        enableEdgeToEdge()
        setContent {
            val settingsState by settingsViewModel.uiState.collectAsStateWithLifecycle()
            
            // Mark settings as loaded once we receive the first emission from DataStore
            // Since settingsState is a StateFlow with an initial state, we need to be careful.
            // However, hiltViewModel will trigger the first emission fairly quickly.
            // A more robust way is to check a specific 'isLoaded' flag in ViewModel,
            // but for now, we'll use a side effect.
                LaunchedEffect(settingsState) {
                isSettingsLoaded = true
            }

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

            FiscusTheme(
                darkTheme = darkTheme,
                dynamicColor = settingsState.isDynamicColorEnabled,
                accentColor = settingsState.accentColor,
                borderRadius = settingsState.borderRadius
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (isAuthenticated) {
                        FiscusAppContent(
                            dashboardViewModel = dashboardViewModel,
                            settingsViewModel = settingsViewModel,
                            transactionsViewModel = transactionsViewModel,
                            analysisViewModel = analysisViewModel,
                            accountsViewModel = accountsViewModel,
                            categoriesViewModel = categoriesViewModel
                        )
                    } else {
                        Box(contentAlignment = Alignment.Center) {
                            Button(onClick = {
                                showBiometricPrompt { authenticated ->
                                    isAuthenticated = authenticated
                                }
                            }) {
                                Text("Unlock Fiscus")
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
            .setSubtitle("Access your fiscus")
            .setAllowedAuthenticators(
                BiometricManager.Authenticators.BIOMETRIC_STRONG or
                        BiometricManager.Authenticators.DEVICE_CREDENTIAL
            )
            .build()

        biometricPrompt.authenticate(promptInfo)
    }
}

@Composable
fun FiscusAppContent(
    dashboardViewModel: DashboardViewModel,
    settingsViewModel: SettingsViewModel,
    transactionsViewModel: TransactionsViewModel,
    analysisViewModel: AnalysisViewModel,
    accountsViewModel: AccountsViewModel,
    categoriesViewModel: ManageCategoriesViewModel
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
            val isDashboard = currentDestination == "dashboard"
            item(
                selected = isDashboard,
                onClick = {
                    navController.navigate("dashboard") {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = {
                    Icon(
                        imageVector = if (isDashboard) Icons.Default.Dashboard else Icons.Outlined.Dashboard,
                        contentDescription = "Overview"
                    )
                },
                label = { 
                    Text("Overview", fontWeight = if (isDashboard) FontWeight.SemiBold else FontWeight.Medium) 
                }
            )
            
            val isTransactions = currentDestination == "transactions"
            item(
                selected = isTransactions,
                onClick = {
                    navController.navigate("transactions") {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = {
                    Icon(
                        imageVector = if (isTransactions) Icons.Default.History else Icons.Outlined.History,
                        contentDescription = "History"
                    )
                },
                label = { 
                    Text("History", fontWeight = if (isTransactions) FontWeight.SemiBold else FontWeight.Medium) 
                }
            )
            
            val isAnalysis = currentDestination == "analysis"
            item(
                selected = isAnalysis,
                onClick = {
                    navController.navigate("analysis") {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = {
                    Icon(
                        imageVector = if (isAnalysis) Icons.Default.Analytics else Icons.Outlined.Analytics,
                        contentDescription = "Analysis"
                    )
                },
                label = { 
                    Text("Analysis", fontWeight = if (isAnalysis) FontWeight.SemiBold else FontWeight.Medium) 
                }
            )
            
            val isSettings = currentDestination == "settings"
            item(
                selected = isSettings,
                onClick = {
                    navController.navigate("settings") {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = {
                    Icon(
                        imageVector = if (isSettings) Icons.Default.Settings else Icons.Outlined.Settings,
                        contentDescription = "Settings"
                    )
                },
                label = { 
                    Text("Settings", fontWeight = if (isSettings) FontWeight.SemiBold else FontWeight.Medium) 
                }
            )
        }
    ) {
        NavHost(
            navController = navController,
            startDestination = "dashboard",
            modifier = Modifier.fillMaxSize(),
            enterTransition = { FiscusAnimation.Navigation.Enter },
            exitTransition = { FiscusAnimation.Navigation.Exit },
            popEnterTransition = { FiscusAnimation.Navigation.PopEnter },
            popExitTransition = { FiscusAnimation.Navigation.PopExit }
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
                    },
                    onManageAccounts = {
                        navController.navigate("accounts")
                    }
                )
            }
            composable("accounts") {
                AccountsScreen(
                    viewModel = accountsViewModel,
                    onBack = { navController.popBackStack() }
                )
            }
            composable("transactions") {
                TransactionsScreen(viewModel = transactionsViewModel)
            }
            composable("analysis") {
                AnalysisScreen(viewModel = analysisViewModel)
            }
            composable("settings") {
                SettingsScreen(
                    viewModel = settingsViewModel,
                    onManageCategories = { navController.navigate("categories") }
                )
            }
            composable("categories") {
                ManageCategoriesScreen(
                    viewModel = categoriesViewModel,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}