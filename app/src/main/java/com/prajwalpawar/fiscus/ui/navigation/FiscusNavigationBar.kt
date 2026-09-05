package com.prajwalpawar.fiscus.ui.navigation

import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.compose.material3.Icon
import com.prajwalpawar.fiscus.data.model.BottomBarPreference

@Composable
fun FiscusNavigationBar (
    navController: NavController,
    currentDestination: NavDestination?,
    preference: BottomBarPreference
) {
    val destinations = listOf(
        FiscusDestination.Home,
        FiscusDestination.Transactions,
        FiscusDestination.Analysis,
        FiscusDestination.Settings
    )

    NavigationBar {
        destinations.forEach { destination ->
            val selected = currentDestination
                ?.hierarchy
                ?.any { it.route == destination.route } == true

            NavigationBarItem(
                selected = selected,
                onClick = {
                    navController.navigate(destination.route) {
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = {
                    Icon(
                        imageVector = if (selected) {
                            destination.selectedIcon
                        } else {
                            destination.unselectedIcon
                        },
                        contentDescription = destination.label
                    )
                },
                label = when (preference) {
                    BottomBarPreference.SHOW_LABELS -> {
                        {
                            Text(destination.label)
                        }
                    }

                    BottomBarPreference.ICONS_ONLY -> {
                        null
                    }

                    BottomBarPreference.SHOW_LABELS_WHEN_SELECTED -> {
                        if (selected) {
                            {
                                Text(destination.label)
                            }
                        } else {
                            null
                        }
                    }
                }
            )
        }
    }
}