package com.prajwalpawar.fiscus.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.ListItemShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import com.prajwalpawar.fiscus.data.model.TransactionDetails
import com.prajwalpawar.fiscus.data.model.TransactionType
import com.prajwalpawar.fiscus.ui.theme.FiscusSpacing
import com.prajwalpawar.fiscus.util.storedDateToLocalDate
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

// One transaction as a segmented list row: category badge, title, where the money went,
// and the amount with its direction arrow. Used by Home, Transactions and search.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FiscusTransactionRow(
    transaction: TransactionDetails,
    shapes: ListItemShapes,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    // Search results mix days, so they show the date above the title.
    showDate: Boolean = false
) {
    val isTransfer = transaction.type == TransactionType.TRANSFER

    FiscusSegmentedListItem(
        shapes = shapes,
        modifier = modifier,
        onClick = onClick,
        leadingContent = {
            FiscusListIcon(
                icon = if (isTransfer) {
                    Icons.Rounded.SwapHoriz
                } else {
                    categoryIcon(transaction.categoryIconKey ?: "category")
                }
            )
        },
        overlineContent = if (showDate) {
            { Text(transactionDayLabel(storedDateToLocalDate(transaction.date))) }
        } else {
            null
        },
        content = {
            Text(
                text = transaction.title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        supportingContent = {
            Text(
                text = if (isTransfer) {
                    "${transaction.sourceAccountName.orEmpty()} → ${transaction.destinationAccountName.orEmpty()}"
                } else {
                    "${transaction.categoryName ?: "Uncategorised"} · ${transaction.accountName.orEmpty()}"
                },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        trailingContent = {
            FiscusTransactionAmount(
                type = transaction.type,
                amount = transaction.amount
            )
        }
    )
}

// "Today", "Yesterday", else a medium date like "12 Oct 2026".
fun transactionDayLabel(day: LocalDate): String {
    val today = LocalDate.now()

    return when (day) {
        today -> "Today"
        today.minusDays(1) -> "Yesterday"
        else -> day.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))
    }
}

// Lists [transactions] (newest first) under a header per day, each day as its own
// segmented group. [row] draws one transaction with the shapes for its place in the day.
fun LazyListScope.transactionsByDay(
    transactions: List<TransactionDetails>,
    row: @Composable (transaction: TransactionDetails, shapes: ListItemShapes) -> Unit
) {
    transactions
        .groupBy { storedDateToLocalDate(it.date) }
        .forEach { (day, dayTransactions) ->
            item(key = "day-$day") {
                Text(
                    text = transactionDayLabel(day),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .padding(
                            start = FiscusSpacing.md,
                            top = FiscusSpacing.sm,
                            bottom = FiscusSpacing.xs
                        )
                        .semantics { heading() }
                )
            }

            itemsIndexed(
                items = dayTransactions,
                key = { _, transaction -> transaction.id }
            ) { index, transaction ->
                row(
                    transaction,
                    ListItemDefaults.segmentedShapes(index = index, count = dayTransactions.size)
                )
            }
        }
}
