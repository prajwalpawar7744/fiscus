package com.prajwalpawar.fiscus.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import com.prajwalpawar.fiscus.data.model.TransactionType
import com.prajwalpawar.fiscus.ui.theme.FiscusSize
import com.prajwalpawar.fiscus.ui.theme.FiscusSpacing
import com.prajwalpawar.fiscus.util.formatMoney

// An amount with a direction arrow instead of a sign: up for income (balance rises),
// down for expense, sideways for transfers. Matches Home's Income/Spent tiles.
@Composable
fun FiscusTransactionAmount(
    type: TransactionType,
    amount: Long,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.titleSmall,
    arrowSize: Dp = FiscusSize.amountArrow
) {
    val color = when (type) {
        TransactionType.INCOME -> MaterialTheme.colorScheme.primary
        TransactionType.EXPENSE -> MaterialTheme.colorScheme.onSurface
        TransactionType.TRANSFER -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    CompositionLocalProvider(LocalContentColor provides color) {
        Row(
            modifier = modifier,
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(FiscusSpacing.xs)
        ) {
            // The arrow replaces the sign, so it carries the meaning for screen readers.
            Icon(
                imageVector = when (type) {
                    TransactionType.INCOME -> Icons.Rounded.ArrowUpward
                    TransactionType.EXPENSE -> Icons.Rounded.ArrowDownward
                    TransactionType.TRANSFER -> Icons.Rounded.SwapHoriz
                },
                contentDescription = when (type) {
                    TransactionType.INCOME -> "Income"
                    TransactionType.EXPENSE -> "Expense"
                    TransactionType.TRANSFER -> "Transfer"
                },
                modifier = Modifier.size(arrowSize)
            )

            Text(
                text = formatMoney(amount),
                style = style
            )
        }
    }
}
