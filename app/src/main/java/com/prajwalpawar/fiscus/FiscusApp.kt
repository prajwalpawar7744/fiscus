package com.prajwalpawar.fiscus

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.prajwalpawar.fiscus.navigation.FiscusDestination
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.lifecycle.viewmodel.compose.viewModel
import com.prajwalpawar.fiscus.navigation.AppBarType
import com.prajwalpawar.fiscus.screens.settings.SettingsScreen
import com.prajwalpawar.fiscus.screens.settings.SettingsViewModel
import com.prajwalpawar.fiscus.ui.components.FiscusNavigationBar
import com.prajwalpawar.fiscus.ui.components.FiscusTopAppBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FiscusApp() {
    val destinations = remember {
        listOf(
            FiscusDestination.Dashboard,
            FiscusDestination.Transactions,
            FiscusDestination.Analysis,
            FiscusDestination.Settings
        )
    }

    var selectedDestination by remember {
        mutableStateOf<FiscusDestination>(
            FiscusDestination.Dashboard
        )
    }

    val settingsViewModel: SettingsViewModel = viewModel()
    val appBarType by settingsViewModel.appBarStyle.collectAsState()
    val currentAppBarType = AppBarType.valueOf(appBarType)

    val scrollBehavior =
        when (currentAppBarType) {
            AppBarType.SMALL -> null
            AppBarType.MEDIUM -> TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
            AppBarType.LARGE -> TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
        }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .let { modifier ->
                if (scrollBehavior != null) {
                    modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
                } else {
                    modifier
                }
            },
        topBar = {
            FiscusTopAppBar(
                title = selectedDestination.label,
                appBarType = currentAppBarType,
                scrollBehavior = scrollBehavior
            )
        },
        bottomBar = {
            FiscusNavigationBar(
                destinations = destinations,
                selectedDestination = selectedDestination,
                onDestinationSelected = {
                    selectedDestination = it
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedDestination) {
                FiscusDestination.Dashboard -> {
                    Text("Dashboard")
                }

                FiscusDestination.Transactions -> {
                    Text("Transactions")
                }

                FiscusDestination.Analysis -> {
                    Text("Analysis")
                }

                FiscusDestination.Settings -> {
                    SettingsScreen()
                }
            }
        }
    }
}