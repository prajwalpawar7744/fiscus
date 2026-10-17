package com.prajwalpawar.fiscus.ui.screens.home

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.prajwalpawar.fiscus.ui.components.FiscusEmptyState

// The add-transaction FAB lives in the app Scaffold, so it is placed and inset correctly.
@Composable
fun HomeScreen (
    modifier: Modifier = Modifier
) {
    FiscusEmptyState(
        icon = Icons.Default.AccountBalanceWallet,
        title = "Welcome to Fiscus",
        message = "Tap Add transaction to record your first income or expense.",
        modifier = modifier
    )
}
