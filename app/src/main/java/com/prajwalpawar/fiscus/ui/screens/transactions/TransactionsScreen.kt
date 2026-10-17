package com.prajwalpawar.fiscus.ui.screens.transactions

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.prajwalpawar.fiscus.ui.components.FiscusEmptyState

@Composable
fun TransactionsScreen(
    modifier: Modifier = Modifier
) {
    FiscusEmptyState(
        icon = Icons.AutoMirrored.Filled.ReceiptLong,
        title = "No transactions yet",
        message = "Transactions you add will be listed here.",
        modifier = modifier
    )
}
