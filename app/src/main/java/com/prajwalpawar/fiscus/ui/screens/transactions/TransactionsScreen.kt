package com.prajwalpawar.fiscus.ui.screens.transactions

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialShapes
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.prajwalpawar.fiscus.ui.components.FiscusEmptyState

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TransactionsScreen(
    modifier: Modifier = Modifier
) {
    FiscusEmptyState(
        icon = Icons.AutoMirrored.Filled.ReceiptLong,
        title = "No transactions yet",
        message = "Transactions you add will be listed here.",
        shape = MaterialShapes.Clover4Leaf,
        modifier = modifier
    )
}
