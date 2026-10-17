package com.prajwalpawar.fiscus.ui.navigation

import androidx.navigation.NavController

// Switch tabs the way the bottom bar does: one copy of each tab, each keeping its own state.
fun NavController.navigateToTab(destination: FiscusDestination) {
    navigate(destination.route) {
        popUpTo(graph.startDestinationId) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}
