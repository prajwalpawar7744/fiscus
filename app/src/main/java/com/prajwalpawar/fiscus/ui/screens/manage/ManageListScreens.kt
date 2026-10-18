package com.prajwalpawar.fiscus.ui.screens.manage

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.prajwalpawar.fiscus.data.model.AccountBalance
import com.prajwalpawar.fiscus.data.model.CategoryType
import com.prajwalpawar.fiscus.ui.components.FiscusListIcon
import com.prajwalpawar.fiscus.ui.components.FiscusSectionHeader
import com.prajwalpawar.fiscus.ui.components.FiscusSegmentedListItem
import com.prajwalpawar.fiscus.ui.components.accountIcon
import com.prajwalpawar.fiscus.ui.components.accountTypeLabel
import com.prajwalpawar.fiscus.ui.components.categoryIcon
import com.prajwalpawar.fiscus.ui.navigation.ContainerKeys
import com.prajwalpawar.fiscus.ui.navigation.containerTransform
import com.prajwalpawar.fiscus.ui.theme.FiscusSpacing
import com.prajwalpawar.fiscus.util.formatMoney

// One row of a manage list. The "add" row is the last row of an active section.
private data class ManageRow(
    val key: String,
    val icon: ImageVector,
    val title: String,
    val supporting: String?,
    val trailingText: String?,
    val isAddRow: Boolean,
    // Pairs this row with the page it opens for the container transform.
    val transformKey: Any,
    val onClick: () -> Unit
)

@Composable
fun ManageCategoriesScreen(
    viewModel: ManageViewModel,
    onCategoryClick: (id: Long) -> Unit,
    onAddCategoryClick: (type: CategoryType) -> Unit,
    modifier: Modifier = Modifier
) {
    val categories by viewModel.categories.collectAsStateWithLifecycle()

    ManageList(
        sections = categories?.let { all ->
            val (archived, active) = all.partition { it.category.isArchived }

            val activeSections = CategoryType.entries.map { type ->
                val label = if (type == CategoryType.EXPENSE) "Expense" else "Income"

                label to active
                    .filter { it.category.type == type }
                    .map { (category, usage) ->
                        ManageRow(
                            key = "category-${category.id}",
                            transformKey = ContainerKeys.category(category.id),
                            icon = categoryIcon(category.iconKey),
                            title = category.name,
                            supporting = usageLabel(usage, category.isDefault),
                            trailingText = null,
                            isAddRow = false,
                            onClick = { onCategoryClick(category.id) }
                        )
                    } + ManageRow(
                    key = "add-$type",
                    transformKey = ContainerKeys.newCategory(type.name),
                    icon = Icons.Rounded.Add,
                    title = "Add ${label.lowercase()} category",
                    supporting = null,
                    trailingText = null,
                    isAddRow = true,
                    onClick = { onAddCategoryClick(type) }
                )
            }

            val archivedSection = archived.map { (category, usage) ->
                ManageRow(
                    key = "category-${category.id}",
                    transformKey = ContainerKeys.category(category.id),
                    icon = categoryIcon(category.iconKey),
                    title = category.name,
                    supporting = (if (category.type == CategoryType.EXPENSE) "Expense" else "Income") +
                            " · " + usageLabel(usage, isDefault = false),
                    trailingText = null,
                    isAddRow = false,
                    onClick = { onCategoryClick(category.id) }
                )
            }

            activeSections + listOf("Archived" to archivedSection).filter { it.second.isNotEmpty() }
        },
        modifier = modifier
    )
}

@Composable
fun ManageAccountsScreen(
    viewModel: ManageViewModel,
    onAccountClick: (id: Long) -> Unit,
    onAddAccountClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accounts by viewModel.accounts.collectAsStateWithLifecycle()

    ManageList(
        sections = accounts?.let { all ->
            val (archived, active) = all.partition { it.isArchived }

            fun row(account: AccountBalance) = ManageRow(
                key = "account-${account.id}",
                transformKey = ContainerKeys.account(account.id),
                icon = accountIcon(account.type),
                title = account.name,
                supporting = accountTypeLabel(account.type) + " · " +
                        usageLabel(account.usageCount, account.isDefault),
                trailingText = formatMoney(account.balance),
                isAddRow = false,
                onClick = { onAccountClick(account.id) }
            )

            val activeSection = active.map(::row) + ManageRow(
                key = "add-account",
                transformKey = ContainerKeys.NEW_ACCOUNT,
                icon = Icons.Rounded.Add,
                title = "Add account",
                supporting = null,
                trailingText = null,
                isAddRow = true,
                onClick = onAddAccountClick
            )

            listOf("Active" to activeSection) +
                    listOf("Archived" to archived.map(::row)).filter { it.second.isNotEmpty() }
        },
        modifier = modifier
    )
}

// "Default · 3 transactions", "Not used yet".
private fun usageLabel(usageCount: Int, isDefault: Boolean): String {
    val usage = if (usageCount == 0) "Not used yet" else transactionCount(usageCount)
    return if (isDefault) "Default · $usage" else usage
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ManageList(
    // Null while loading.
    sections: List<Pair<String, List<ManageRow>>>?,
    modifier: Modifier = Modifier
) {
    if (sections == null) {
        Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            LoadingIndicator()
        }
        return
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = FiscusSpacing.md,
            end = FiscusSpacing.md,
            bottom = FiscusSpacing.lg
        ),
        verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)
    ) {
        sections.forEach { (title, rows) ->
            manageSection(title, rows)
        }
    }
}

private fun LazyListScope.manageSection(
    title: String,
    rows: List<ManageRow>
) {
    item(key = "header-$title") {
        FiscusSectionHeader(
            title = title,
            modifier = Modifier.padding(
                start = FiscusSpacing.md,
                top = FiscusSpacing.lg,
                bottom = FiscusSpacing.sm
            )
        )
    }

    itemsIndexed(rows, key = { _, row -> row.key }) { index, row ->
        ManageRowItem(row = row, index = index, count = rows.size)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ManageRowItem(
    row: ManageRow,
    index: Int,
    count: Int
) {
    FiscusSegmentedListItem(
        shapes = ListItemDefaults.segmentedShapes(index = index, count = count),
        modifier = Modifier.containerTransform(row.transformKey),
        onClick = row.onClick,
        leadingContent = {
            FiscusListIcon(icon = row.icon)
        },
        content = {
            Text(
                text = row.title,
                color = if (row.isAddRow) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
            )
        },
        supportingContent = row.supporting?.let {
            { Text(it) }
        },
        trailingContent = if (row.isAddRow) {
            null
        } else {
            {
                if (row.trailingText != null) {
                    Text(
                        text = row.trailingText,
                        style = MaterialTheme.typography.titleSmall
                    )
                } else {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                        contentDescription = null
                    )
                }
            }
        }
    )
}
