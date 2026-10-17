package com.prajwalpawar.fiscus.ui.navigation

import androidx.compose.material3.Icon
import androidx.compose.material3.ShortNavigationBar
import androidx.compose.material3.ShortNavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import com.prajwalpawar.fiscus.data.model.BottomBarPreference

@Composable
fun FiscusNavigationBar(
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

    ShortNavigationBar {
        destinations.forEach { destination ->
            val selected = currentDestination
                ?.hierarchy
                ?.any { it.route == destination.route } == true

            val showLabel = when (preference) {
                BottomBarPreference.SHOW_LABELS -> true
                BottomBarPreference.ICONS_ONLY -> false
                BottomBarPreference.SHOW_LABELS_WHEN_SELECTED -> selected
            }

            ShortNavigationBarItem(
                selected = selected,
                onClick = {
                    navController.navigateToTab(destination)
                },
                icon = {
                    Icon(
                        imageVector = if (selected) {
                            destination.selectedIcon
                        } else {
                            destination.unselectedIcon
                        },
                        // A visible label already names the item; only describe bare icons.
                        contentDescription = if (showLabel) null else destination.label
                    )
                },
                label = if (showLabel) {
                    {
                        Text(destination.label)
                    }
                } else {
                    null
                }
            )
        }
    }
}
