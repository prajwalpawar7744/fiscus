package com.prajwalpawar.fiscus

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.prajwalpawar.fiscus.navigation.FiscusDestination
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import com.prajwalpawar.fiscus.navigation.AppBarType
import com.prajwalpawar.fiscus.ui.components.FiscusNavigationBar
import com.prajwalpawar.fiscus.ui.components.FiscusTopAppBar

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

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            FiscusTopAppBar(
                title = selectedDestination.label,
                appBarType = AppBarType.LARGE
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
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = selectedDestination.label
            )
        }
    }
}