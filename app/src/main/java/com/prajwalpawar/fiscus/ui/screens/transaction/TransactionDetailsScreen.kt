@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.prajwalpawar.fiscus.ui.screens.transaction

import androidx.compose.material3.IconButtonDefaults
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import com.prajwalpawar.fiscus.util.formatMoney
import com.prajwalpawar.fiscus.ui.components.FiscusSectionHeader
import com.prajwalpawar.fiscus.data.model.TransactionItem
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Notes
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.prajwalpawar.fiscus.data.model.TransactionDetails
import com.prajwalpawar.fiscus.data.model.TransactionType
import com.prajwalpawar.fiscus.ui.components.FiscusListIcon
import com.prajwalpawar.fiscus.ui.components.FiscusSegmentedListItem
import com.prajwalpawar.fiscus.ui.components.FiscusTransactionAmount
import com.prajwalpawar.fiscus.ui.components.categoryIcon
import com.prajwalpawar.fiscus.ui.theme.FiscusShapes
import com.prajwalpawar.fiscus.ui.theme.FiscusSize
import com.prajwalpawar.fiscus.ui.theme.FiscusSpacing
import com.prajwalpawar.fiscus.util.storedDateToLocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TransactionDetailsScreen(
    viewModel: TransactionDetailsViewModel,
    onEditClick: (id: Long) -> Unit,
    onDeleted: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val details = uiState.details

    if (details == null) {
        // Loading, or gone right after a delete while the page closes.
        Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            if (uiState.isLoading) LoadingIndicator()
        }
        return
    }

    var confirmDelete by rememberSaveable { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(
                    start = FiscusSpacing.md,
                    end = FiscusSpacing.md,
                    top = FiscusSpacing.md,
                    // Room for the floating toolbar, so the last row isn't hidden behind it.
                    bottom = FloatingToolbarDefaults.ContainerSize +
                            FloatingToolbarDefaults.ScreenOffset * 2
                ),
            verticalArrangement = Arrangement.spacedBy(FiscusSpacing.lg)
        ) {
            DetailsHero(details)
            DetailsList(details)
            if (uiState.items.isNotEmpty()) ItemsList(uiState.items)
        }

        // Expressive floating toolbar: the main action (Edit) is its FAB, the rest sit inside.
        HorizontalFloatingToolbar(
            expanded = true,
            floatingActionButton = {
                FloatingToolbarDefaults.VibrantFloatingActionButton(
                    onClick = { onEditClick(details.id) }
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Edit,
                        contentDescription = "Edit"
                    )
                }
            },
            colors = FloatingToolbarDefaults.vibrantFloatingToolbarColors(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = FloatingToolbarDefaults.ScreenOffset)
        ) {
            IconButton(
                onClick = { confirmDelete = true },
                shapes = IconButtonDefaults.shapes()
            ) {
                Icon(
                    imageVector = Icons.Rounded.Delete,
                    contentDescription = "Delete"
                )
            }
        }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            icon = {
                Icon(
                    imageVector = Icons.Rounded.Delete,
                    contentDescription = null
                )
            },
            title = {
                Text("Delete this transaction?")
            },
            text = {
                Text("\"${details.title}\" will be removed and your balances updated. This can't be undone.")
            },
            confirmButton = {
                TextButton(
                    shapes = ButtonDefaults.shapes(),
                    onClick = {
                        confirmDelete = false
                        viewModel.delete(onDeleted)
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }, shapes = ButtonDefaults.shapes()) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun DetailsHero(details: TransactionDetails) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(FiscusSpacing.md)
    ) {
        // Decorative; the details list names the category.
        Box(
            modifier = Modifier
                .size(FiscusSize.emptyStateShape)
                .background(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = FiscusShapes.icon
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = transactionIcon(details),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(FiscusSize.emptyStateIcon)
            )
        }

        FiscusTransactionAmount(
            type = details.type,
            amount = details.amount,
            style = MaterialTheme.typography.displayMediumEmphasized,
            arrowSize = FiscusSize.heroAmountArrow
        )

        Text(
            text = details.title,
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )
    }
}

// One labelled fact on the details page.
private data class DetailRow(
    val label: String,
    val value: String,
    val icon: ImageVector
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DetailsList(details: TransactionDetails) {
    val rows = buildList {
        when (details.type) {
            TransactionType.TRANSFER -> {
                add(
                    DetailRow(
                        "From",
                        details.sourceAccountName.orEmpty(),
                        Icons.Rounded.AccountBalanceWallet
                    )
                )
                add(
                    DetailRow(
                        "To",
                        details.destinationAccountName.orEmpty(),
                        Icons.Rounded.AccountBalanceWallet
                    )
                )
            }

            else -> {
                add(
                    DetailRow(
                        label = if (details.type == TransactionType.INCOME) "Income category" else "Expense category",
                        value = details.categoryName ?: "Uncategorised",
                        icon = categoryIcon(details.categoryIconKey ?: "category")
                    )
                )
                add(
                    DetailRow(
                        "Account",
                        details.accountName.orEmpty(),
                        Icons.Rounded.AccountBalanceWallet
                    )
                )
            }
        }

        add(
            DetailRow(
                label = "Date",
                value = storedDateToLocalDate(details.date)
                    .format(DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL)),
                icon = Icons.Rounded.CalendarToday
            )
        )

        details.note?.let { add(DetailRow("Note", it, Icons.AutoMirrored.Rounded.Notes)) }
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)
    ) {
        rows.forEachIndexed { index, row ->
            FiscusSegmentedListItem(
                shapes = ListItemDefaults.segmentedShapes(index = index, count = rows.size),
                leadingContent = {
                    FiscusListIcon(icon = row.icon)
                },
                overlineContent = {
                    Text(row.label)
                },
                content = {
                    Text(row.value)
                }
            )
        }
    }
}

private fun transactionIcon(details: TransactionDetails): ImageVector {
    return if (details.type == TransactionType.TRANSFER) {
        Icons.Rounded.SwapHoriz
    } else {
        categoryIcon(details.categoryIconKey ?: "category")
    }
}

// The itemised breakdown: one static segmented row per item, quantity × price and total.
@Composable
private fun ItemsList(items: List<TransactionItem>) {
    Column(
        verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)
    ) {
        FiscusSectionHeader(
            title = "${items.size} ${if (items.size == 1) "item" else "items"}",
            modifier = Modifier.padding(bottom = FiscusSpacing.xs)
        )

        items.forEachIndexed { index, item ->
            FiscusSegmentedListItem(
                shapes = ListItemDefaults.segmentedShapes(index = index, count = items.size),
                supportingContent = {
                    Text(
                        text = "${minorUnitsToAmountInput(item.quantity)} × ${formatMoney(item.unitPrice)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                trailingContent = {
                    Text(
                        text = formatMoney(item.total),
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                content = {
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            )
        }
    }
}
