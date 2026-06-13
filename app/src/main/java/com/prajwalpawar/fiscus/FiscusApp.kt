package com.prajwalpawar.fiscus

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.prajwalpawar.fiscus.navigation.FiscusDestination
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment

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
        bottomBar = {
            NavigationBar {
                destinations.forEach { destination ->
                    NavigationBarItem(
                        selected = destination == selectedDestination,
                        onClick = {
                            selectedDestination = destination
                        },
                        icon = {
                            Icon(
                                imageVector =
                                    if (destination == selectedDestination) {
                                        destination.selectedIcon
                                    } else {
                                        destination.unSelectedIcon
                                    },
                                contentDescription = destination.label
                            )
                        },
                        label = {
                            Text(destination.label)
                        }
                    )
                }
            }
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