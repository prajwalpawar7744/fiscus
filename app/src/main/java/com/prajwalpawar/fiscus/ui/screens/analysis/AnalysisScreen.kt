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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearWavyProgressIndicator
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
import kotlin.math.ceil
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

        // A single day has no trend: the tiles already say everything a one-column chart would.
        uiState.trendScale?.let { scale ->
            item(key = "trendHeader") {
                SectionHeader(
                    if (scale == TrendScale.DAILY) "Spending by day" else "Spending by month"
                )
            }

            item(key = "trend") {
                SpendingTrend(
                    buckets = uiState.trend,
                    scale = scale,
                    period = uiState.period,
                    periodSpent = uiState.spent,
                    onBucketClick = viewModel::setPeriod,
                    modifier = Modifier.padding(horizontal = FiscusSpacing.md)
                )
            }
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
            // For one day the daily average would just repeat "Spent", so count instead.
            if (period.isSingleDay) {
                StatTile(
                    label = "Transactions",
                    value = state.transactionCount.toString(),
                    caption = "On this day",
                    modifier = Modifier.weight(1f)
                )
            } else {
                StatTile(
                    label = "Daily average",
                    value = formatMoney(state.dailyAverage),
                    caption = if (includesToday) "Per day so far" else "Per day",
                    modifier = Modifier.weight(1f)
                )
            }

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

// Ranked by amount, one wavy meter per category showing its share of the total.
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


        categories.forEach { category ->
            CategoryBar(
                category = category,
                // A meter of the category's share of the whole: a full track means 100%.
                fraction = if (total > 0) category.total.toFloat() / total else 0f,
                share = if (total > 0) (category.total * 100f / total).roundToInt() else 0
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
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

            // Expressive wavy meter; the track is a lighter tone of the same colour.
            LinearWavyProgressIndicator(
                // The spring can overshoot; keep the indicator inside its track.
                progress = { animatedFraction.coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

// Spending per bucket (day or month). In the month-context view the selected month is
// emphasised and the rest muted, so it stands out without a second hue.
@Composable
private fun SpendingTrend(
    buckets: List<TrendBucket>,
    scale: TrendScale,
    period: Period,
    periodSpent: Long,
    onBucketClick: (Period) -> Unit,
    modifier: Modifier = Modifier
) {
    val largest = buckets.maxOfOrNull { it.expense }?.coerceAtLeast(1L) ?: 1L
    // Label every n-th column so labels never crowd, however many columns there are.
    val labelEvery = ceil(buckets.size / MAX_TREND_LABELS.toDouble()).toInt().coerceAtLeast(1)

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainer
    ) {
        Column(
            modifier = Modifier.padding(FiscusSpacing.md),
            verticalArrangement = Arrangement.spacedBy(FiscusSpacing.sm)
        ) {
            // Only the period gets a value label; tap a column to open that day or month.
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
                buckets.forEach { bucket ->
                    TrendColumn(
                        bucket = bucket,
                        description = bucketDescription(bucket),
                        fraction = bucket.expense.toFloat() / largest,
                        onClick = { onBucketClick(bucket.period) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // One solid hairline baseline; no gridlines needed with a labelled value.
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            Row(modifier = Modifier.fillMaxWidth()) {
                buckets.forEachIndexed { index, bucket ->
                    Text(
                        text = if (index % labelEvery == 0) bucketLabel(bucket, scale) else "",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (bucket.isHighlighted) {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        softWrap = false,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

// "Oct" for a month column, "18" for a day column.
private fun bucketLabel(bucket: TrendBucket, scale: TrendScale): String {
    return if (scale == TrendScale.DAILY) {
        bucket.period.start.dayOfMonth.toString()
    } else {
        bucket.period.start.month.getDisplayName(TextStyle.SHORT, Locale.getDefault())
    }
}

// "October 2026" or "18 Oct 2026", for screen readers and the tap label.
private fun bucketDescription(bucket: TrendBucket): String = bucket.period.label()

@Composable
private fun TrendColumn(
    bucket: TrendBucket,
    description: String,
    fraction: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val animatedFraction by animateFloatAsState(
        targetValue = fraction,
        animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec(),
        label = "trendColumn"
    )

    // The whole slot is the tap target, larger than the column itself.
    Box(
        modifier = modifier
            .fillMaxHeight()
            .clickable(
                role = Role.Button,
                onClickLabel = "Show $description",
                onClick = onClick
            )
            .semantics {
                selected = bucket.isHighlighted
                contentDescription = "$description, spent ${formatMoney(bucket.expense)}"
            },
        contentAlignment = Alignment.BottomCenter
    ) {
        if (bucket.expense > 0L) {
            Box(
                modifier = Modifier
                    // Narrower than its slot so neighbours stay apart; capped for thin marks.
                    .fillMaxWidth(COLUMN_SLOT_FILL)
                    .widthIn(max = FiscusSize.chartColumnWidth)
                    // Keep tiny amounts visible as a sliver rather than vanishing.
                    .fillMaxHeight(abs(animatedFraction).coerceIn(0.02f, 1f))
                    .background(
                        color = if (bucket.isHighlighted) {
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

// Most axis labels shown at once; beyond this they're thinned to every n-th column.
private const val MAX_TREND_LABELS = 7

// Share of each column's slot the column itself fills.
private const val COLUMN_SLOT_FILL = 0.6f
