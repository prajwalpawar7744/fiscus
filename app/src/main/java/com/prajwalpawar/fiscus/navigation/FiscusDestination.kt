package com.prajwalpawar.fiscus.navigation

import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.Settings

sealed class FiscusDestination (
    val label: String,
    val selectedIcon: ImageVector,
    val unSelectedIcon: ImageVector
) {
    data object Dashboard : FiscusDestination(
        label = "Dashboard",
        selectedIcon = Icons.Filled.Home,
        unSelectedIcon = Icons.Outlined.Home
    )

    data object Transactions : FiscusDestination(
        label = "Transactions",
        selectedIcon = Icons.Filled.Payments,
        unSelectedIcon = Icons.Outlined.Payments
    )

    data object Analysis : FiscusDestination(
        label = "Analysis",
        selectedIcon = Icons.Filled.BarChart,
        unSelectedIcon = Icons.Outlined.BarChart
    )

    data object Settings : FiscusDestination(
        label = "Settings",
        selectedIcon = Icons.Filled.Settings,
        unSelectedIcon = Icons.Outlined.Settings
    )
}