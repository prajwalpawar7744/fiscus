package com.prajwalpawar.fiscus.ui.components

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.prajwalpawar.fiscus.navigation.FiscusDestination

@Composable
fun FiscusNavigationBar (
    destinations: List<FiscusDestination>,
    selectedDestination: FiscusDestination,
    onDestinationSelected: (FiscusDestination) -> Unit
) {
    NavigationBar {
        destinations.forEach { destination ->
            NavigationBarItem(
                selected = destination == selectedDestination,
                onClick = {
                    onDestinationSelected(destination)
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