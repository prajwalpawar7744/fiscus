package com.prajwalpawar.fiscus.ui.screens.analysis

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.prajwalpawar.fiscus.data.model.CategoryTotal
import com.prajwalpawar.fiscus.data.model.MonthTotals
import com.prajwalpawar.fiscus.data.model.TransactionType
import com.prajwalpawar.fiscus.ui.components.FiscusConnectedToggleGroup
import com.prajwalpawar.fiscus.ui.components.FiscusListIcon
import com.prajwalpawar.fiscus.ui.components.FiscusPeriodSwitcher
import com.prajwalpawar.fiscus.ui.components.FiscusSectionHeader
import com.prajwalpawar.fiscus.ui.components.FiscusTransactionRow
import com.prajwalpawar.fiscus.ui.components.categoryIcon
import com.prajwalpawar.fiscus.ui.components.monthLabel
import com.prajwalpawar.fiscus.ui.navigation.ContainerKeys
import com.prajwalpawar.fiscus.ui.navigation.containerTransform
import com.prajwalpawar.fiscus.ui.theme.FiscusSize
import com.prajwalpawar.fiscus.ui.theme.FiscusSpacing
import com.prajwalpawar.fiscus.util.Period
import com.prajwalpawar.fiscus.util.formatMoney
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AnalysisScreen(
    viewModel: AnalysisViewModel,
    onTransactionClick: (id: Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = FiscusSpacing.lg),
        verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)
    ) {
        item(key = "period") {
            FiscusPeriodSwitcher(
                period = uiState.period,
                onPeriodChange = viewModel::setPeriod
            )
        }

        if (uiState.isLoading) {
            item(key = "loading") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(FiscusSpacing.xl),
                    contentAlignment = Alignment.Center
                ) {
                    LoadingIndicator()
                }
            }
            return@LazyColumn
        }

        item(key = "tiles") {
            HeadlineTiles(
                state = uiState,
                modifier = Modifier.padding(horizontal = FiscusSpacing.md)
            )
        }

        item(key = "categoriesHeader") {
            SectionHeader("By category")
        }

        item(key = "categories") {
            CategoryBreakdown(
                type = uiState.categoryType,
                categories = uiState.categories,
                total = uiState.categoryTotal,
                period = uiState.period,
                onTypeSelected = viewModel::setCategoryType,
                modifier = Modifier.padding(horizontal = FiscusSpacing.md)
            )
        }

        item(key = "trendHeader") {
            SectionHeader("Spending by month")
        }

        item(key = "trend") {
            SpendingTrend(
                trend = uiState.trend,
                period = uiState.period,
                periodSpent = uiState.spent,
                highlighted = uiState.highlightedMonths,
                onMonthClick = viewModel::selectMonth,
                modifier = Modifier.padding(horizontal = FiscusSpacing.md)
            )
        }

        if (uiState.largestExpenses.isNotEmpty()) {
            item(key = "largestHeader") {
                SectionHeader("Biggest expenses")
            }

            itemsIndexed(
                items = uiState.largestExpenses,
                key = { _, transaction -> transaction.id }
            ) { index, transaction ->
                FiscusTransactionRow(
                    transaction = transaction,
                    shapes = ListItemDefaults.segmentedShapes(
                        index = index,
                        count = uiState.largestExpenses.size
                    ),
                    onClick = { onTransactionClick(transaction.id) },
                    showDate = true,
                    modifier = Modifier
                        .padding(horizontal = FiscusSpacing.md)
                        .containerTransform(ContainerKeys.transaction(transaction.id))
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    FiscusSectionHeader(
        title = title,
        modifier = Modifier.padding(
            start = FiscusSpacing.md,
            end = FiscusSpacing.md,
            top = FiscusSpacing.lg,
            bottom = FiscusSpacing.sm
        )
    )
}

// Four headline numbers as stat tiles: the numbers are the chart here.
@Composable
private fun HeadlineTiles(
    state: AnalysisUiState,
    modifier: Modifier = Modifier
) {
    val period = state.period
    // "September", "the day before", or "the previous 15 days".
    val previousName = when {
        period.isWholeMonth -> period.previous().start.month
            .getDisplayName(TextStyle.FULL, Locale.getDefault())
        period.isSingleDay -> "the day before"
        else -> "the previous ${period.dayCount} days"
    }
    val includesToday = !LocalDate.now().isBefore(period.start) && !LocalDate.now().isAfter(period.end)

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(FiscusSpacing.sm)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(FiscusSpacing.sm)) {
            StatTile(
                label = "Spent",
                value = formatMoney(state.spent),
                caption = "Income ${formatMoney(state.income)}",
                modifier = Modifier.weight(1f)
            )

            // The words carry the direction, so it never relies on color.
            val change = state.changeVsPrevious
            StatTile(
                label = if (period.isWholeMonth) "Vs last month" else "Vs previous period",
                value = when {
                    change == null -> "—"
                    change > 0 -> "+$change%"
                    else -> "$change%"
                },
                caption = when {
                    change == null -> "Nothing spent in $previousName"
                    change > 0 -> "More than $previousName"
                    change < 0 -> "Less than $previousName"
                    else -> "Same as $previousName"
                },
                modifier = Modifier.weight(1f)
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(FiscusSpacing.sm)) {
            StatTile(
                label = "Daily average",
                value = formatMoney(state.dailyAverage),
                caption = if (includesToday && !period.isSingleDay) "Per day so far" else "Per day",
                modifier = Modifier.weight(1f)
            )

            val rate = state.savingsRate
            StatTile(
                label = "Savings rate",
                value = rate?.let { "$it%" } ?: "—",
                caption = when {
                    rate == null -> "No income in this period"
                    rate < 0 -> "Spent more than earned"
                    else -> "Of income kept"
                },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun StatTile(
    label: String,
    value: String,
    caption: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.semantics(mergeDescendants = true) {},
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainer
    ) {
        Column(
            modifier = Modifier.padding(FiscusSpacing.md),
            verticalArrangement = Arrangement.spacedBy(FiscusSpacing.xs)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            val valueStyle = MaterialTheme.typography.titleLargeEmphasized
            Text(
                text = value,
                style = valueStyle,
                maxLines = 1,
                autoSize = TextAutoSize.StepBased(maxFontSize = valueStyle.fontSize)
            )

            Text(
                text = caption,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// Ranked bars in one color: length carries the amount, the label names the category.
@Composable
private fun CategoryBreakdown(
    type: TransactionType,
    categories: List<CategoryTotal>,
    total: Long,
    period: Period,
    onTypeSelected: (TransactionType) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(FiscusSpacing.md)
    ) {
        FiscusConnectedToggleGroup(
            options = listOf(TransactionType.EXPENSE, TransactionType.INCOME),
            selectedOption = type,
            onOptionSelected = onTypeSelected,
            optionLabel = { if (it == TransactionType.EXPENSE) "Expense" else "Income" }
        )

        if (categories.isEmpty()) {
            Text(
                text = if (type == TransactionType.EXPENSE) {
                    "No expenses in ${period.label()}"
                } else {
                    "No income in ${period.label()}"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            return@Column
        }

        // The largest category spans the full width; the rest scale to it.
        val largest = categories.maxOf { it.total }.coerceAtLeast(1L)

        categories.forEach { category ->
            CategoryBar(
                category = category,
                fraction = category.total.toFloat() / largest,
                share = if (total > 0) (category.total * 100f / total).roundToInt() else 0
            )
        }
    }
}

@Composable
private fun CategoryBar(
    category: CategoryTotal,
    fraction: Float,
    share: Int
) {
    val animatedFraction by animateFloatAsState(
        targetValue = fraction,
        animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec(),
        label = "categoryBar"
    )
    val amount = formatMoney(category.total)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            // One spoken summary instead of separate name, amount and bar.
            .semantics(mergeDescendants = true) {
                contentDescription = "${category.name}, $amount, $share percent"
            },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(FiscusSpacing.md)
    ) {
        FiscusListIcon(icon = categoryIcon(category.iconKey))

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(FiscusSpacing.xs)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = category.name,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                // Values sit beside the bar in text colors, never inside it.
                Text(
                    text = "$amount · $share%",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Grows from a square baseline to a rounded data end.
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedFraction.coerceIn(0f, 1f))
                    .height(FiscusSize.chartBarThickness)
                    .background(
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(
                            topEnd = FiscusSize.chartMarkCorner,
                            bottomEnd = FiscusSize.chartMarkCorner
                        )
                    )
            )
        }
    }
}

// Spending per month with the period's months emphasized and the rest muted, so what
// matters stands out without needing a second hue.
@Composable
private fun SpendingTrend(
    trend: List<MonthTotals>,
    period: Period,
    periodSpent: Long,
    highlighted: Set<YearMonth>,
    onMonthClick: (YearMonth) -> Unit,
    modifier: Modifier = Modifier
) {
    val largest = trend.maxOfOrNull { it.expense }?.coerceAtLeast(1L) ?: 1L

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainer
    ) {
        Column(
            modifier = Modifier.padding(FiscusSpacing.md),
            verticalArrangement = Arrangement.spacedBy(FiscusSpacing.sm)
        ) {
            // Only the period gets a value label; tap a month to see that one.
            Text(
                text = "${formatMoney(periodSpent)} in ${period.label()}",
                style = MaterialTheme.typography.titleMedium
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(FiscusSize.trendChartHeight),
                verticalAlignment = Alignment.Bottom
            ) {
                trend.forEach { month ->
                    TrendColumn(
                        month = month,
                        fraction = month.expense.toFloat() / largest,
                        isSelected = month.month in highlighted,
                        onClick = { onMonthClick(month.month) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // One solid hairline baseline; no gridlines needed with a labelled value.
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            Row(modifier = Modifier.fillMaxWidth()) {
                trend.forEach { month ->
                    Text(
                        text = month.month.month.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                        style = MaterialTheme.typography.labelMedium,
                        color = if (month.month in highlighted) {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun TrendColumn(
    month: MonthTotals,
    fraction: Float,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val animatedFraction by animateFloatAsState(
        targetValue = fraction,
        animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec(),
        label = "trendColumn"
    )

    // The whole slot is the tap target, much larger than the column itself.
    Box(
        modifier = modifier
            .fillMaxHeight()
            .clickable(
                role = Role.Button,
                onClickLabel = "Show ${monthLabel(month.month)}",
                onClick = onClick
            )
            .semantics {
                selected = isSelected
                contentDescription = "${monthLabel(month.month)}, spent ${formatMoney(month.expense)}"
            },
        contentAlignment = Alignment.BottomCenter
    ) {
        if (month.expense > 0L) {
            Box(
                modifier = Modifier
                    .width(FiscusSize.chartColumnWidth)
                    // Keep tiny months visible as a sliver rather than vanishing.
                    .fillMaxHeight(abs(animatedFraction).coerceIn(0.02f, 1f))
                    .background(
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.outlineVariant
                        },
                        shape = RoundedCornerShape(
                            topStart = FiscusSize.chartMarkCorner,
                            topEnd = FiscusSize.chartMarkCorner
                        )
                    )
            )
        }
    }
}
