package com.prajwalpawar.fiscus.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.prajwalpawar.fiscus.R
import com.prajwalpawar.fiscus.model.TransactionListEntry
import com.prajwalpawar.fiscus.model.TransactionType

/**
 * The supporting line of a [TransactionListItem]: "Food · Cash" for income and expense,
 * "Cash → Bank" for a transfer. Removed accounts or categories read "Unknown".
 */
@Composable
fun transactionSupportingText(entry: TransactionListEntry): String {
    val unknown = stringResource(R.string.detail_unknown)
    val account = entry.account?.name ?: unknown
    return if (entry.transaction.type == TransactionType.TRANSFER) {
        val toAccount = entry.toAccount?.name ?: unknown
        stringResource(R.string.transactions_supporting_transfer, account, toAccount)
    } else {
        stringResource(R.string.transactions_supporting, entry.category?.name ?: unknown, account)
    }
}
