package com.prajwalpawar.fiscus.ui.screens.transactions

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterListOff
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.prajwalpawar.fiscus.data.model.Account
import com.prajwalpawar.fiscus.data.model.Category
import com.prajwalpawar.fiscus.data.model.CategoryType
import com.prajwalpawar.fiscus.data.model.PeriodTotals
import com.prajwalpawar.fiscus.data.model.TransactionType
import com.prajwalpawar.fiscus.ui.components.FiscusConnectedToggleGroup
import com.prajwalpawar.fiscus.ui.components.FiscusDropdownMenu
import com.prajwalpawar.fiscus.ui.components.FiscusEmptyState
import com.prajwalpawar.fiscus.ui.components.FiscusPeriodSwitcher
import com.prajwalpawar.fiscus.ui.components.FiscusTransactionRow
import com.prajwalpawar.fiscus.ui.components.transactionsByDay
import com.prajwalpawar.fiscus.ui.navigation.ContainerKeys
import com.prajwalpawar.fiscus.ui.navigation.containerTransform
import com.prajwalpawar.fiscus.ui.theme.FiscusSize
import com.prajwalpawar.fiscus.ui.theme.FiscusSpacing
import com.prajwalpawar.fiscus.util.formatMoney
import androidx.compose.material.icons.automirrored.filled.ReceiptLong

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TransactionsScreen(
    viewModel: TransactionsViewModel,
    onTransactionClick: (id: Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = FiscusSize.fabClearance),
        verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)
    ) {
        item(key = "month") {
            FiscusPeriodSwitcher(
                period = uiState.period,
                onPeriodChange = viewModel::setPeriod
            )
        }

        item(key = "summary") {
            PeriodSummary(
                totals = uiState.totals,
                isFiltered = uiState.filters.isActive,
                modifier = Modifier.padding(horizontal = FiscusSpacing.md)
            )
        }

        item(key = "filters") {
            FiltersBar(
                state = uiState,
                onTypeSelected = viewModel::setTypeFilter,
                onAccountSelected = viewModel::setAccountFilter,
                onCategorySelected = viewModel::setCategoryFilter,
                onClear = viewModel::clearFilters
            )
        }

        when {
            uiState.isLoading -> item(key = "loading") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(FiscusSpacing.xl),
                    contentAlignment = Alignment.Center
                ) {
                    LoadingIndicator()
                }
            }

            uiState.filteredOut -> item(key = "filteredOut") {
                NoMatches(onClear = viewModel::clearFilters)
            }

            uiState.transactions.isEmpty() -> item(key = "empty") {
                FiscusEmptyState(
                    icon = Icons.AutoMirrored.Filled.ReceiptLong,
                    title = "Nothing in ${uiState.period.label()}",
                    message = "Transactions dated in this period will be listed here.",
                    modifier = Modifier.fillParentMaxHeight(0.6f)
                )
            }

            else -> transactionsByDay(uiState.transactions) { transaction, shapes ->
                FiscusTransactionRow(
                    transaction = transaction,
                    shapes = shapes,
                    onClick = { onTransactionClick(transaction.id) },
                    modifier = Modifier
                        .padding(horizontal = FiscusSpacing.md)
                        .containerTransform(ContainerKeys.transaction(transaction.id))
                )
            }
        }
    }
}

// Figures for exactly the transactions listed below, filters included.
@Composable
private fun PeriodSummary(
    totals: PeriodTotals,
    isFiltered: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainer
    ) {
        Column(
            modifier = Modifier.padding(FiscusSpacing.md),
            verticalArrangement = Arrangement.spacedBy(FiscusSpacing.sm)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(FiscusSpacing.sm)) {
                SummaryFigure("Income", totals.income, Modifier.weight(1f))
                SummaryFigure("Spent", totals.expense, Modifier.weight(1f))
                SummaryFigure("Net", totals.income - totals.expense, Modifier.weight(1f))
            }

            // Says what the numbers cover, so a filtered total isn't mistaken for the whole period.
            Text(
                text = when {
                    isFiltered && totals.count == 1 -> "1 matching transaction"
                    isFiltered -> "${totals.count} matching transactions"
                    totals.count == 1 -> "1 transaction"
                    else -> "${totals.count} transactions"
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun SummaryFigure(label: String, amount: Long, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = formatMoney(amount),
            style = MaterialTheme.typography.titleMediumEmphasized,
            maxLines = 1,
            autoSize = TextAutoSize.StepBased(
                maxFontSize = MaterialTheme.typography.titleMediumEmphasized.fontSize
            )
        )
    }
}

@Composable
private fun FiltersBar(
    state: TransactionsUiState,
    onTypeSelected: (TransactionType?) -> Unit,
    onAccountSelected: (Long?) -> Unit,
    onCategorySelected: (Long?) -> Unit,
    onClear: () -> Unit
) {
    Column(
        modifier = Modifier.padding(top = FiscusSpacing.md, bottom = FiscusSpacing.sm),
        verticalArrangement = Arrangement.spacedBy(FiscusSpacing.sm)
    ) {
        FiscusConnectedToggleGroup(
            options = listOf(null, TransactionType.EXPENSE, TransactionType.INCOME, TransactionType.TRANSFER),
            selectedOption = state.filters.type,
            onOptionSelected = onTypeSelected,
            optionLabel = { type ->
                when (type) {
                    null -> "All"
                    TransactionType.EXPENSE -> "Expense"
                    TransactionType.INCOME -> "Income"
                    TransactionType.TRANSFER -> "Transfer"
                }
            },
            modifier = Modifier.padding(horizontal = FiscusSpacing.md)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = FiscusSpacing.md),
            horizontalArrangement = Arrangement.spacedBy(FiscusSpacing.sm)
        ) {
            MenuFilterChip(
                label = "Account",
                options = state.accounts,
                selected = state.accounts.firstOrNull { it.id == state.filters.accountId },
                allLabel = "All accounts",
                optionLabel = { it.name },
                onSelected = { onAccountSelected(it?.id) }
            )

            if (state.filters.type != TransactionType.TRANSFER) {
                MenuFilterChip(
                    label = "Category",
                    options = state.categories,
                    selected = state.categories.firstOrNull { it.id == state.filters.categoryId },
                    allLabel = "All categories",
                    // Both types have an "Other", so say which one when all types are shown.
                    optionLabel = { category -> categoryLabel(category, showType = state.filters.type == null) },
                    onSelected = { onCategorySelected(it?.id) }
                )
            }

            if (state.filters.isActive) {
                AssistChip(
                    onClick = onClear,
                    label = { Text("Clear") },
                    leadingIcon = {
                        Icon(Icons.Filled.FilterListOff, contentDescription = null)
                    }
                )
            }
        }
    }
}

// A filter chip that opens a menu; shows the chosen option's name once one is picked.
@Composable
private fun <T : Any> MenuFilterChip(
    label: String,
    options: List<T>,
    selected: T?,
    allLabel: String,
    optionLabel: (T) -> String,
    onSelected: (T?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        FilterChip(
            selected = selected != null,
            onClick = { expanded = true },
            label = { Text(selected?.let(optionLabel) ?: label) },
            trailingIcon = {
                Icon(Icons.Filled.ArrowDropDown, contentDescription = null)
            }
        )

        FiscusDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            items = listOf<T?>(null) + options,
            selectedItem = selected,
            onItemSelected = { option ->
                onSelected(option)
                expanded = false
            },
            itemLabel = { option ->
                Text(option?.let(optionLabel) ?: allLabel)
            }
        )
    }
}

private fun categoryLabel(category: Category, showType: Boolean): String {
    if (!showType) return category.name
    val type = if (category.type == CategoryType.INCOME) "Income" else "Expense"
    return "${category.name} · $type"
}

@Composable
private fun NoMatches(onClear: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(FiscusSpacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(FiscusSpacing.sm)
    ) {
        Text(
            text = "No transactions match these filters",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
        TextButton(onClick = onClear) {
            Text("Clear filters")
        }
    }
}
