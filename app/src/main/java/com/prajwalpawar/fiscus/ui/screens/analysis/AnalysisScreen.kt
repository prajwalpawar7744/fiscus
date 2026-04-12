package com.prajwalpawar.fiscus.ui.screens.analysis

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.prajwalpawar.fiscus.ui.utils.EmptyState
import com.prajwalpawar.fiscus.ui.utils.formatCurrency
import com.prajwalpawar.fiscus.ui.utils.getCategoryIcon
import com.prajwalpawar.fiscus.ui.utils.rememberFiscusHaptic
import com.prajwalpawar.fiscus.ui.utils.staggeredVerticalFadeIn
import com.prajwalpawar.fiscus.domain.model.TransactionType
import java.time.Instant
import java.time.ZoneId
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import com.prajwalpawar.fiscus.ui.utils.fiscusClickable
import androidx.compose.ui.input.nestedscroll.nestedScroll

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalysisScreen(
    viewModel: AnalysisViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val haptic = rememberFiscusHaptic()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())
    
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            if (uiState.topBarStyle == "longtopbar") {
                LargeTopAppBar(
                    title = { Text("Analysis", fontWeight = FontWeight.Bold) },
                    colors = TopAppBarDefaults.largeTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface,
                        scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer
                    ),
                    scrollBehavior = scrollBehavior
                )
            } else {
                TopAppBar(
                    title = { Text("Analysis", fontWeight = FontWeight.Bold) },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface,
                        scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer
                    ),
                    scrollBehavior = scrollBehavior
                )
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                var visible by remember { mutableStateOf(false) }

                LaunchedEffect(Unit) { visible = true }

                AnimatedVisibility(
                    visible = visible,
                    enter = if (uiState.areAnimationsEnabled) fadeIn(tween(300)) else fadeIn(snap())
                ) {

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Spending Overview",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                            SegmentedButton(
                                selected = uiState.granularity == AnalysisGranularity.DAILY,
                                onClick = {
                                    haptic.click()
                                    viewModel.setGranularity(AnalysisGranularity.DAILY)
                                },
                                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                                label = {
                                    Text(
                                        "Daily",
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                }
                            )
                            SegmentedButton(
                                selected = uiState.granularity == AnalysisGranularity.MONTHLY,
                                onClick = {
                                    haptic.click()
                                    viewModel.setGranularity(AnalysisGranularity.MONTHLY)
                                },
                                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                                label = {
                                    Text(
                                        "Monthly",
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                }
                            )
                        }

                        val scale by animateFloatAsState(
                            targetValue = 1f,
                            animationSpec = if (uiState.areAnimationsEnabled) tween(300) else snap()
                        )

                        // Advanced Filters Surface
                        Surface(
                            modifier = Modifier.fillMaxWidth()
                                .graphicsLayer {
                                    if (uiState.areAnimationsEnabled) {
                                        scaleX = scale
                                        scaleY = scale
                                    }
                                },
                            color = MaterialTheme.colorScheme.surfaceContainerLow,
                            shape = MaterialTheme.shapes.extraLarge,
                            tonalElevation = 2.dp
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // First row: Transaction Type & Time Range
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // Transaction Type Dropdown
                                    var typeExpanded by remember { mutableStateOf(false) }
                                    ExposedDropdownMenuBox(
                                        expanded = typeExpanded,
                                        onExpandedChange = { typeExpanded = !typeExpanded },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        OutlinedTextField(
                                            value = when (uiState.selectedTransactionType) {
                                                TransactionType.EXPENSE -> "Expenses"
                                                TransactionType.INCOME -> "Income"
                                                TransactionType.TRANSFER -> "Transfers"
                                                null -> "All Types"
                                            },
                                            onValueChange = {},
                                            readOnly = true,
                                            maxLines = 1,
                                            label = {
                                                Text(
                                                    "Type",
                                                    style = MaterialTheme.typography.labelSmall
                                                )
                                            },
                                            trailingIcon = {
                                                ExposedDropdownMenuDefaults.TrailingIcon(
                                                    expanded = typeExpanded
                                                )
                                            },
                                            modifier = Modifier.menuAnchor(
                                                ExposedDropdownMenuAnchorType.PrimaryNotEditable,
                                                true
                                            ),
                                            shape = MaterialTheme.shapes.medium,
                                            textStyle = MaterialTheme.typography.bodySmall,
                                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                                            )
                                        )
                                        ExposedDropdownMenu(
                                            expanded = typeExpanded,
                                            onDismissRequest = { typeExpanded = false }
                                        ) {
                                            DropdownMenuItem(
                                                text = { Text("Expenses") },
                                                onClick = {
                                                    viewModel.onTransactionTypeSelected(
                                                        TransactionType.EXPENSE
                                                    ); typeExpanded = false
                                                },
                                                leadingIcon = {
                                                    Icon(
                                                        Icons.Default.ArrowDownward,
                                                        contentDescription = null,
                                                        tint = Color.Red
                                                    )
                                                }
                                            )
                                            DropdownMenuItem(
                                                text = { Text("Income") },
                                                onClick = {
                                                    viewModel.onTransactionTypeSelected(
                                                        TransactionType.INCOME
                                                    ); typeExpanded = false
                                                },
                                                leadingIcon = {
                                                    Icon(
                                                        Icons.Default.ArrowUpward,
                                                        contentDescription = null,
                                                        tint = Color.Green
                                                    )
                                                }
                                            )
                                            DropdownMenuItem(
                                                text = { Text("All") },
                                                onClick = {
                                                    viewModel.onTransactionTypeSelected(null); typeExpanded =
                                                    false
                                                },
                                                leadingIcon = {
                                                    Icon(
                                                        Icons.Default.HorizontalRule,
                                                        contentDescription = null
                                                    )
                                                }
                                            )
                                        }
                                    }

                                    // Time Range Dropdown
                                    var timeExpanded by remember { mutableStateOf(false) }
                                    var showDatePicker by remember { mutableStateOf(false) }
                                    ExposedDropdownMenuBox(
                                        expanded = timeExpanded,
                                        onExpandedChange = { timeExpanded = !timeExpanded },
                                        modifier = Modifier.weight(1.2f)
                                    ) {
                                        val timeLabel = when (uiState.selectedTimeRange) {
                                            TimeRange.CUSTOM -> if (uiState.startDate != null && uiState.endDate != null) {
                                                "${uiState.startDate} - ${uiState.endDate}"
                                            } else "Custom"

                                            else -> uiState.selectedTimeRange.name.lowercase()
                                                .replaceFirstChar { it.uppercase() }
                                                .replace("_", " ")
                                        }
                                        OutlinedTextField(
                                            value = timeLabel,
                                            onValueChange = {},
                                            readOnly = true,
                                            maxLines = 1,
                                            label = {
                                                Text(
                                                    "Period",
                                                    style = MaterialTheme.typography.labelSmall
                                                )
                                            },
                                            trailingIcon = {
                                                ExposedDropdownMenuDefaults.TrailingIcon(
                                                    expanded = timeExpanded
                                                )
                                            },
                                            modifier = Modifier.menuAnchor(
                                                ExposedDropdownMenuAnchorType.PrimaryNotEditable,
                                                true
                                            ),
                                            shape = MaterialTheme.shapes.medium,
                                            textStyle = MaterialTheme.typography.bodySmall,
                                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                                            )
                                        )
                                        ExposedDropdownMenu(
                                            expanded = timeExpanded,
                                            onDismissRequest = { timeExpanded = false }
                                        ) {
                                            TimeRange.entries.forEach { range ->
                                                DropdownMenuItem(
                                                    text = {
                                                        Text(
                                                            range.name.lowercase()
                                                                .replaceFirstChar { it.uppercase() }
                                                                .replace("_", " ")
                                                        )
                                                    },
                                                    onClick = {
                                                        viewModel.onTimeRangeSelected(range)
                                                        timeExpanded = false
                                                        if (range == TimeRange.CUSTOM) showDatePicker =
                                                            true
                                                    },
                                                    leadingIcon = {
                                                        Icon(
                                                            when (range) {
                                                                TimeRange.TODAY -> Icons.Default.Today
                                                                TimeRange.THIS_WEEK -> Icons.Default.DateRange
                                                                TimeRange.THIS_MONTH -> Icons.Default.CalendarMonth
                                                                TimeRange.THIS_YEAR -> Icons.Default.CalendarToday
                                                                TimeRange.CUSTOM -> Icons.Default.EditCalendar
                                                                else -> Icons.Default.History
                                                            }, contentDescription = null
                                                        )
                                                    }
                                                )
                                            }
                                        }

                                        if (showDatePicker) {
                                            val dateRangePickerState =
                                                rememberDateRangePickerState()
                                            DatePickerDialog(
                                                onDismissRequest = { showDatePicker = false },
                                                confirmButton = {
                                                    TextButton(onClick = {
                                                        val start =
                                                            dateRangePickerState.selectedStartDateMillis?.let {
                                                                Instant.ofEpochMilli(it)
                                                                    .atZone(ZoneId.systemDefault())
                                                                    .toLocalDate()
                                                            }
                                                        val end =
                                                            dateRangePickerState.selectedEndDateMillis?.let {
                                                                Instant.ofEpochMilli(it)
                                                                    .atZone(ZoneId.systemDefault())
                                                                    .toLocalDate()
                                                            }
                                                        viewModel.onDateRangeSelected(start, end)
                                                        showDatePicker = false
                                                    }) { Text("Confirm") }
                                                },
                                                dismissButton = {
                                                    TextButton(onClick = {
                                                        showDatePicker = false
                                                    }) { Text("Cancel") }
                                                }
                                            ) {
                                                DateRangePicker(
                                                    state = dateRangePickerState,
                                                    modifier = Modifier.weight(1f).padding(16.dp),
                                                    title = {
                                                        Text(
                                                            "Select Date Range",
                                                            modifier = Modifier.padding(16.dp)
                                                        )
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }

                                // Second row: Category Dropdown
                                var categoryExpanded by remember { mutableStateOf(false) }
                                ExposedDropdownMenuBox(
                                    expanded = categoryExpanded,
                                    onExpandedChange = { categoryExpanded = !categoryExpanded },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    val selectedCategory =
                                        uiState.allCategories.find { it.id == uiState.selectedCategoryId }
                                    OutlinedTextField(
                                        value = selectedCategory?.name ?: "All Categories",
                                        onValueChange = {},
                                        readOnly = true,
                                        maxLines = 1,
                                        label = {
                                            Text(
                                                "Category Filter",
                                                style = MaterialTheme.typography.labelSmall
                                            )
                                        },
                                        trailingIcon = {
                                            ExposedDropdownMenuDefaults.TrailingIcon(
                                                expanded = categoryExpanded
                                            )
                                        },
                                        modifier = Modifier.fillMaxWidth().menuAnchor(
                                            ExposedDropdownMenuAnchorType.PrimaryNotEditable,
                                            true
                                        ),
                                        shape = MaterialTheme.shapes.medium,
                                        textStyle = MaterialTheme.typography.bodySmall,
                                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                                        )
                                    )
                                    ExposedDropdownMenu(
                                        expanded = categoryExpanded,
                                        onDismissRequest = { categoryExpanded = false }
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text("All Categories") },
                                            onClick = {
                                                viewModel.onCategorySelected(null); categoryExpanded =
                                                false
                                            },
                                            leadingIcon = {
                                                Icon(
                                                    Icons.Default.Category,
                                                    contentDescription = null
                                                )
                                            }
                                        )
                                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                                        uiState.allCategories.forEach { category ->
                                            DropdownMenuItem(
                                                text = {
                                                    Text(
                                                        category.name,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                },
                                                onClick = {
                                                    viewModel.onCategorySelected(category.id); categoryExpanded =
                                                    false
                                                },
                                                leadingIcon = {
                                                    Box(
                                                        modifier = Modifier.size(12.dp).background(
                                                            Color(category.color),
                                                            CircleShape
                                                        )
                                                    )
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AnalysisChartType.entries.forEachIndexed { index, type ->
                            FilterChip(
                                selected = uiState.selectedChartType == type,
                                onClick = {
                                    haptic.click()
                                    viewModel.onChartTypeSelected(type)
                                },
                                label = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = when (type) {
                                                AnalysisChartType.BAR -> Icons.Default.BarChart
                                                AnalysisChartType.LINE -> Icons.AutoMirrored.Filled.ShowChart
                                                AnalysisChartType.PIE -> Icons.Default.PieChart
                                                AnalysisChartType.HEATMAP -> Icons.Default.CalendarViewMonth
                                            },
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(Modifier.width(4.dp))
                                        Text(
                                            type.name.lowercase()
                                                .replaceFirstChar { it.uppercase() },
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    }
                                },
                                shape = MaterialTheme.shapes.medium
                            )
                        }
                    }

                    AnimatedContent(
                        targetState = uiState.selectedChartType,
                        transitionSpec = {
                            if (uiState.areAnimationsEnabled) {
                                fadeIn(tween(300)) togetherWith fadeOut(tween(300))
                            } else {
                                fadeIn(snap()) togetherWith fadeOut(snap())
                            }
                        },
                        label = "chartTransition"
                    ) { type ->
                        val chartModifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp)
                            .padding(top = 16.dp)

                        when (type) {
                            AnalysisChartType.BAR -> {
                                BarChart(
                                    dataPoints = if (uiState.selectedTransactionType == TransactionType.INCOME) uiState.incomeDataPoints else uiState.expenseDataPoints,
                                    modifier = chartModifier,
                                    color = if (uiState.selectedTransactionType == TransactionType.INCOME) Color.Green else MaterialTheme.colorScheme.primary,
                                    currencyCode = uiState.currency,
                                    animationsEnabled = uiState.areAnimationsEnabled
                                )
                            }

                            AnalysisChartType.LINE -> {
                                LineChart(
                                    expensePoints = uiState.expenseDataPoints,
                                    incomePoints = uiState.incomeDataPoints,
                                    showExpense = uiState.selectedTransactionType == TransactionType.EXPENSE || uiState.selectedTransactionType == null,
                                    showIncome = uiState.selectedTransactionType == TransactionType.INCOME || uiState.selectedTransactionType == null,
                                    modifier = chartModifier,
                                    currencyCode = uiState.currency,
                                    animationsEnabled = uiState.areAnimationsEnabled
                                )
                            }

                            AnalysisChartType.PIE -> {
                                PieChart(
                                    breakdown = uiState.categoryBreakdown,
                                    modifier = Modifier.fillMaxSize(),
                                    animationsEnabled = uiState.areAnimationsEnabled
                                )
                            }

                            AnalysisChartType.HEATMAP -> {
                                HeatMapChart(
                                    activityPoints = uiState.activityPoints,
                                    startDate = uiState.effectiveStartDate,
                                    endDate = uiState.effectiveEndDate,
                                    modifier = chartModifier,
                                    animationsEnabled = uiState.areAnimationsEnabled
                                )
                            }
                        }
                    }
                }
            }

            item {
                AnalysisSummaryCard(
                    income = uiState.totalIncome,
                    expense = uiState.totalExpense,
                    currencyCode = uiState.currency,
                    animationsEnabled = uiState.areAnimationsEnabled
                )
            }

            item {
                Text(
                    text = "Expense Breakdown",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
                )
            }

            if (uiState.categoryBreakdown.isEmpty()) {
                item {
                    var emptyVisible by remember { mutableStateOf(false) }

                    LaunchedEffect(Unit) { emptyVisible = true }

                    AnimatedVisibility(
                        visible = emptyVisible,
                        enter = if (uiState.areAnimationsEnabled) fadeIn(tween(400)) + scaleIn(initialScale = 0.85f) else fadeIn(snap())
                    ) {
                        EmptyState(
                            message = "No expenses to analyze yet",
                            icon = Icons.Default.Analytics
                        )
                    }
                }
            } else {
                itemsIndexed(uiState.categoryBreakdown) { index, analysis ->
                    CategoryAnalysisItem(
                        modifier = Modifier.staggeredVerticalFadeIn(index, enabled = uiState.areAnimationsEnabled, initialDelay = 150),
                        analysis = analysis,
                        currencyCode = uiState.currency,
                        animationsEnabled = uiState.areAnimationsEnabled
                    )
                }
            }
        }
    }
}

@Composable
fun AnalysisSummaryCard(
    income: Double,
    expense: Double,
    currencyCode: String,
    animationsEnabled: Boolean = true
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 6.dp),
        shape = MaterialTheme.shapes.extraLarge
    ) {
        Column(modifier = Modifier.padding(28.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Column {
                    Text(
                        text = "Total Spending",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                    )
                    Text(
                        text = formatCurrency(expense, currencyCode),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }

                // Small ratio indicator (Income vs Expense)
                Box(contentAlignment = Alignment.Center) {
                    val ratio = if (income > 0) (expense / (income + expense)).toFloat() else 1f
                    CircularProgressIndicator(
                        progress = { ratio },
                        modifier = Modifier.size(64.dp),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.2f),
                        strokeWidth = 8.dp,
                        strokeCap = StrokeCap.Round
                    )
                }
            }
        }
    }
}

@Composable
fun CategoryAnalysisItem(
    analysis: CategoryAnalysis,
    currencyCode: String,
    modifier: Modifier = Modifier,
    animationsEnabled: Boolean = true
) {
    val haptic = rememberFiscusHaptic()
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 2.dp)
            .clip(MaterialTheme.shapes.medium)
            .fiscusClickable(haptic = haptic, enabledAnimations = animationsEnabled) {
                // Potential detail view navigation
            }
            .clip(MaterialTheme.shapes.medium),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = Color(analysis.category.color).copy(alpha = 0.12f),
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = getCategoryIcon(analysis.category.icon ?: ""),
                        contentDescription = null,
                        tint = Color(analysis.category.color),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = analysis.category.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = formatCurrency(analysis.amount, currencyCode),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error // Since it's an expense breakdown usually
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${analysis.transactionCount} transactions",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${(analysis.percentage * 100).toInt()}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { analysis.percentage },
                    modifier = Modifier.fillMaxWidth().height(4.dp).clip(CircleShape),
                    color = Color(analysis.category.color),
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
        }
    }
}

@Composable
fun BarChart(
    dataPoints: List<TimeDataPoint>,
    modifier: Modifier = Modifier,
    color: Color,
    currencyCode: String,
    animationsEnabled: Boolean = true
) {
    val animatedProgress = remember { Animatable(0f) }
    var selectedIndex by remember { mutableIntStateOf(-1) }
    val haptic = rememberFiscusHaptic()

    LaunchedEffect(dataPoints.size) {
        animatedProgress.snapTo(0f)
        animatedProgress.animateTo(
            1f,
            animationSpec = if (animationsEnabled) tween(350) else snap()
        )
    }

    if (dataPoints.isEmpty()) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text("No data available", style = MaterialTheme.typography.bodyMedium)
        }
        return
    }

    val maxAmount = remember(dataPoints) { dataPoints.maxOfOrNull { it.amount } ?: 1.0 }

    Box(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(dataPoints) {
                    detectTapGestures { offset ->
                        val index = (offset.x / (size.width / dataPoints.size)).toInt().coerceIn(0, dataPoints.size - 1)
                        if (index != selectedIndex) {
                            selectedIndex = index
                            haptic.click()
                        }
                    }
                }
                .pointerInput(dataPoints) {
                    detectDragGestures(
                        onDragEnd = { selectedIndex = -1 },
                        onDragCancel = { selectedIndex = -1 }
                    ) { change, _ ->
                        val index = (change.position.x / size.width * dataPoints.size).toInt().coerceIn(0, dataPoints.size - 1)
                        if (index != selectedIndex) {
                            selectedIndex = index
                            haptic.click()
                        }
                    }
                },
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            dataPoints.forEachIndexed { index, point ->
                val barHeightRatio = (point.amount / maxAmount).toFloat()
                val isSelected = index == selectedIndex

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .drawBehind {
                                val barWidth = if (isSelected) 20.dp.toPx() else 16.dp.toPx()
                                val barHeight = size.height * barHeightRatio * animatedProgress.value
                                drawRoundRect(
                                    color = if (isSelected) color else color.copy(alpha = 0.6f),
                                    topLeft = Offset((size.width - barWidth) / 2, size.height - barHeight),
                                    size = Size(barWidth, barHeight),
                                    cornerRadius = CornerRadius(6.dp.toPx())
                                )
                            }
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    val label = if (point.label.length > 6) point.label.take(3) + ".." else point.label
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 1,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        // Tooltip Overlay
        if (selectedIndex != -1 && selectedIndex < dataPoints.size) {
            val point = dataPoints[selectedIndex]
            Surface(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 8.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = MaterialTheme.shapes.medium,
                tonalElevation = 4.dp,
                shadowElevation = 4.dp
            ) {
                Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(point.label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
                    Text(formatCurrency(point.amount, currencyCode), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun PieChart(
    breakdown: List<CategoryAnalysis>,
    modifier: Modifier = Modifier,
    animationsEnabled: Boolean = true
) {
    val animatedSweep = remember { Animatable(0f) }
    var selectedCategory by remember { mutableStateOf<CategoryAnalysis?>(null) }
    val haptic = rememberFiscusHaptic()

    LaunchedEffect(breakdown) {
        animatedSweep.snapTo(0f)
        animatedSweep.animateTo(1f, if (animationsEnabled) tween(350) else snap())
    }

    if (breakdown.isEmpty()) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text("No data for pie chart", style = MaterialTheme.typography.bodyMedium)
        }
        return
    }

    val surfaceColor = MaterialTheme.colorScheme.surface

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(
            modifier = Modifier
                .size(200.dp)
                .pointerInput(breakdown) {
                    detectTapGestures { offset ->
                        val centerX = size.width / 2
                        val centerY = size.height / 2
                        val angle = Math.toDegrees(Math.atan2((offset.y - centerY).toDouble(), (offset.x - centerX).toDouble())).toFloat()
                        val normalizedAngle = (angle + 90 + 360) % 360
                        
                        var currentAngle = 0f
                        val found = breakdown.find { analysis ->
                            val sweepAngle = analysis.percentage * 360f
                            val isWithin = normalizedAngle >= currentAngle && normalizedAngle < currentAngle + sweepAngle
                            currentAngle += sweepAngle
                            isWithin
                        }
                        
                        if (found != null) {
                            selectedCategory = if (selectedCategory?.category?.id == found.category.id) null else found
                            if (selectedCategory != null) haptic.click()
                        } else {
                            selectedCategory = null
                        }
                    }
                }
        ) {
            var startAngle = -90f
            breakdown.forEach { analysis ->
                val sweepAngle = analysis.percentage * 360f * animatedSweep.value
                val isSelected = selectedCategory?.category?.id == analysis.category.id
                drawArc(
                    color = Color(analysis.category.color).copy(alpha = if (isSelected) 1f else 0.8f),
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = true,
                    size = size,
                    style = if (isSelected) Stroke(width = 8.dp.toPx()) else androidx.compose.ui.graphics.drawscope.Fill
                )
                if (!isSelected) {
                    drawArc(
                        color = Color(analysis.category.color),
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = true,
                        size = size
                    )
                }
                startAngle += sweepAngle
            }
            // Draw center hole for donut style
            drawCircle(
                color = surfaceColor,
                radius = size.minDimension / 4f
            )
        }

        // Overlay tooltip
        selectedCategory?.let { analysis ->
            Surface(
                modifier = Modifier.padding(bottom = 220.dp),
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = MaterialTheme.shapes.medium,
                tonalElevation = 4.dp
            ) {
                Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(analysis.category.name, style = MaterialTheme.typography.labelSmall)
                    Text("${(analysis.percentage * 100).toInt()}%", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun LineChart(
    expensePoints: List<TimeDataPoint>,
    incomePoints: List<TimeDataPoint>,
    showExpense: Boolean,
    showIncome: Boolean,
    modifier: Modifier = Modifier,
    currencyCode: String,
    animationsEnabled: Boolean = true
) {
    val allPoints = (if (showExpense) expensePoints else emptyList()) + (if (showIncome) incomePoints else emptyList())
    if (allPoints.isEmpty() || (showExpense && expensePoints.size < 2 && !showIncome) || (showIncome && incomePoints.size < 2 && !showExpense)) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text("No data for trend graph", style = MaterialTheme.typography.bodyMedium)
        }
        return
    }

    val progress = remember { Animatable(0f) }
    var selectedIndex by remember { mutableIntStateOf(-1) }
    val haptic = rememberFiscusHaptic()
    var componentSize by remember { mutableStateOf(IntSize.Zero) }

    LaunchedEffect(allPoints) {
        progress.snapTo(0f)
        progress.animateTo(1f, if (animationsEnabled) tween(350) else snap())
    }

    val maxAmount = remember(allPoints) { 
        val m = allPoints.maxOfOrNull { it.amount } ?: 1.0 
        if (m == 0.0) 1.0 else m
    }
    val primaryColor = MaterialTheme.colorScheme.primary
    val incomeColor = Color(0xFF4CAF50)
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f)

    Box(modifier = modifier.onSizeChanged { componentSize = it }) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 24.dp, start = 12.dp, end = 12.dp, top = 8.dp)
                .pointerInput(expensePoints, incomePoints) {
                    detectDragGestures(
                        onDragEnd = { selectedIndex = -1 },
                        onDragCancel = { selectedIndex = -1 }
                    ) { change, _ ->
                        val pointsToUse = if (showExpense) expensePoints else incomePoints
                        val index = (change.position.x / size.width * (pointsToUse.size - 1)).toInt().coerceIn(0, pointsToUse.size - 1)
                        if (index != selectedIndex) {
                            selectedIndex = index
                            haptic.click()
                        }
                    }
                }
                .pointerInput(expensePoints, incomePoints) {
                    detectTapGestures { offset ->
                        val pointsToUse = if (showExpense) expensePoints else incomePoints
                        val index = (offset.x / size.width * (pointsToUse.size - 1)).toInt().coerceIn(0, pointsToUse.size - 1)
                        if (index != selectedIndex) {
                            selectedIndex = index
                            haptic.click()
                        }
                    }
                }
        ) {
            val width = size.width
            val height = size.height
            
            // Draw horizontal grid lines
            val gridLines = 4
            for (i in 0..gridLines) {
                val y = height * (i.toFloat() / gridLines)
                drawLine(
                    color = onSurfaceVariant,
                    start = Offset(0f, y),
                    end = Offset(width, y),
                    strokeWidth = 1.dp.toPx()
                )
            }

            if (showExpense && expensePoints.size >= 2) {
                drawTimeLine(expensePoints, maxAmount, primaryColor, progress.value)
            }

            if (showIncome && incomePoints.size >= 2) {
                drawTimeLine(incomePoints, maxAmount, incomeColor, progress.value, drawDots = !showExpense)
            }

            val strokeColor = primaryColor.copy(alpha = 0.4f)
            val indicatorColor = primaryColor
            
            // Draw selection indicator
            if (selectedIndex != -1) {
                val pointsToUse = if (showExpense) expensePoints else incomePoints
                val spacing = width / (pointsToUse.size - 1)
                val x = selectedIndex * spacing
                
                drawLine(
                    color = strokeColor,
                    start = Offset(x, 0f),
                    end = Offset(x, height),
                    strokeWidth = 1.dp.toPx(),
                    pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
                )
                
                drawCircle(
                    color = indicatorColor,
                    radius = 6.dp.toPx(),
                    center = Offset(x, height - (pointsToUse[selectedIndex].amount / maxAmount).toFloat() * height * progress.value)
                )
            }
        }

        // Tooltip Overlay
        if (selectedIndex != -1) {
            val pointsToUse = if (showExpense) expensePoints else incomePoints
            if (selectedIndex < pointsToUse.size) {
                val point = pointsToUse[selectedIndex]
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = MaterialTheme.shapes.medium,
                    tonalElevation = 4.dp
                ) {
                    Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(point.label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
                        Text(formatCurrency(point.amount, currencyCode), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawTimeLine(
    points: List<TimeDataPoint>,
    maxAmount: Double,
    color: Color,
    progress: Float,
    drawDots: Boolean = true
) {
    val width = size.width
    val height = size.height
    val spacing = width / (points.size - 1)
    
    val path = Path()
    val fillPath = Path()
    
    points.forEachIndexed { index, point ->
        val x = index * spacing
        val y = height - (point.amount / maxAmount).toFloat() * height * progress
        
        if (index == 0) {
            path.moveTo(x, y)
            fillPath.moveTo(x, height)
            fillPath.lineTo(x, y)
        } else {
            path.lineTo(x, y)
            fillPath.lineTo(x, y)
        }
        
        if (index == points.size - 1) {
            fillPath.lineTo(x, height)
            fillPath.close()
        }
        
        if (drawDots) {
            drawCircle(
                color = color,
                radius = 3.dp.toPx(),
                center = Offset(x, y)
            )
        }
    }
    
    drawPath(
        path = path,
        color = color,
        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
    )
    
    drawPath(
        path = fillPath,
        brush = androidx.compose.ui.graphics.Brush.verticalGradient(
            colors = listOf(color.copy(alpha = 0.2f), Color.Transparent)
        )
    )
}

@Composable
fun HeatMapChart(
    activityPoints: List<ActivityPoint>,
    startDate: java.time.LocalDate?,
    endDate: java.time.LocalDate?,
    modifier: Modifier = Modifier,
    animationsEnabled: Boolean = true
) {
    val actualStart = startDate ?: java.time.LocalDate.now().minusWeeks(7)
    val actualEnd = endDate ?: java.time.LocalDate.now()
    
    // We want a 7-row grid (Days of week).
    // Start from the beginning of the week of actualStart to keep it aligned
    val firstDayOfWeek = actualStart.minusDays(actualStart.dayOfWeek.value.toLong() - 1)
    val totalDays = java.time.temporal.ChronoUnit.DAYS.between(firstDayOfWeek, actualEnd).toInt() + 1
    val columns = (totalDays + 6) / 7
    
    val intensityMap = activityPoints.associate { it.date to it.intensity }
    var selectedDate by remember { mutableStateOf<java.time.LocalDate?>(null) }
    val haptic = rememberFiscusHaptic()

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val rangeLabel = if (startDate != null && endDate != null) {
            "${startDate.format(java.time.format.DateTimeFormatter.ofPattern("MMM dd"))} - ${endDate.format(java.time.format.DateTimeFormatter.ofPattern("MMM dd"))}"
        } else "Activity Map"
        
        androidx.compose.animation.AnimatedContent(
            targetState = selectedDate,
            transitionSpec = {
                (androidx.compose.animation.fadeIn() + androidx.compose.animation.scaleIn())
                    .togetherWith(androidx.compose.animation.fadeOut() + androidx.compose.animation.scaleOut())
            },
            label = "titleTransition"
        ) { date ->
            if (date != null) {
                val intensity = intensityMap[date] ?: 0f
                Surface(
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    shape = MaterialTheme.shapes.extraSmall,
                ) {
                    Text(
                        text = "${date.format(java.time.format.DateTimeFormatter.ofPattern("dd MMM yyyy"))}: ${(intensity * 100).toInt()}% Activity",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                Text(
                    rangeLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Box {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    repeat(columns) { col ->
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            repeat(7) { row ->
                                val dayOffset = col * 7 + row
                                val currentDay = firstDayOfWeek.plusDays(dayOffset.toLong())
                                
                                if (!currentDay.isAfter(actualEnd) && !currentDay.isBefore(firstDayOfWeek)) {
                                    val intensity = intensityMap[currentDay] ?: 0f
                                    val isOutsideFilter = startDate != null && (currentDay.isBefore(startDate) || currentDay.isAfter(actualEnd))
                                    
                                    val baseColor = MaterialTheme.colorScheme.primary
                                    val cellColor = when {
                                        isOutsideFilter -> MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.2f)
                                        intensity > 0 -> baseColor.copy(alpha = (0.2f + (intensity * 0.8f)).coerceIn(0f, 1f))
                                        else -> MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.5f)
                                    }

                                    var scale by remember { mutableFloatStateOf(0f) }
                                    LaunchedEffect(col, row) {
                                        if (animationsEnabled) {
                                            kotlinx.coroutines.delay((col) * 10L)
                                            scale = 1f
                                        } else {
                                            scale = 1f
                                        }
                                    }

                                    val animatedScale by androidx.compose.animation.core.animateFloatAsState(
                                        targetValue = scale,
                                        animationSpec = androidx.compose.animation.core.spring(
                                            dampingRatio = androidx.compose.animation.core.Spring.DampingRatioLowBouncy
                                        ),
                                        label = "cellScale"
                                    )

                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .graphicsLayer {
                                                scaleX = animatedScale
                                                scaleY = animatedScale
                                            }
                                            .clip(MaterialTheme.shapes.extraSmall)
                                            .background(cellColor)
                                    )
                                } else {
                                    // Placeholder for alignment
                                    Spacer(modifier = Modifier.size(24.dp))
                                }
                            }
                        }
                    }
                }

                // Invisible overlay to capture taps for the entire grid
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .pointerInput(firstDayOfWeek, actualEnd, intensityMap) {
                            detectTapGestures { offset ->
                                val cellWidth = 24.dp.toPx()
                                val spacing = 4.dp.toPx()
                                val totalCellSize = cellWidth + spacing

                                val col = (offset.x / totalCellSize).toInt().coerceIn(0, columns - 1)
                                val row = (offset.y / totalCellSize).toInt().coerceIn(0, 6)

                                val clickedDate = firstDayOfWeek.plusDays((col * 7 + row).toLong())
                                if (!clickedDate.isAfter(actualEnd) && (startDate == null || !clickedDate.isBefore(startDate))) {
                                    selectedDate = if (selectedDate == clickedDate) null else clickedDate
                                    if (selectedDate != null) haptic.click()
                                }
                            }
                        }
                )
            }
        }


        
        Row(
            modifier = Modifier.padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text("Less", style = MaterialTheme.typography.labelSmall)
            repeat(5) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f * it + 0.1f))
                )
            }
            Text("More", style = MaterialTheme.typography.labelSmall)
        }
    }
}
