package com.prajwalpawar.budgetear.ui.screens.analysis

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.prajwalpawar.budgetear.ui.utils.EmptyState
import com.prajwalpawar.budgetear.ui.utils.formatCurrency
import com.prajwalpawar.budgetear.ui.utils.getCategoryIcon
import com.prajwalpawar.budgetear.domain.model.TransactionType
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.math.*
import androidx.compose.ui.input.nestedscroll.nestedScroll

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalysisScreen(
    viewModel: AnalysisViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())
    
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
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
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
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
                            onClick = { viewModel.setGranularity(AnalysisGranularity.DAILY) },
                            shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                            label = { Text("Daily", style = MaterialTheme.typography.labelMedium) }
                        )
                        SegmentedButton(
                            selected = uiState.granularity == AnalysisGranularity.MONTHLY,
                            onClick = { viewModel.setGranularity(AnalysisGranularity.MONTHLY) },
                            shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                            label = { Text("Monthly", style = MaterialTheme.typography.labelMedium) }
                        )
                    }

                    // Advanced Filters Surface
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
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
                                        value = when(uiState.selectedTransactionType) {
                                            TransactionType.EXPENSE -> "Expenses"
                                            TransactionType.INCOME -> "Income"
                                            null -> "All Types"
                                        },
                                        onValueChange = {},
                                        readOnly = true,
                                        maxLines = 1,
                                        label = { Text("Type", style = MaterialTheme.typography.labelSmall) },
                                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded) },
                                        modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true),
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
                                            onClick = { viewModel.onTransactionTypeSelected(TransactionType.EXPENSE); typeExpanded = false },
                                            leadingIcon = { Icon(Icons.Default.ArrowDownward, contentDescription = null, tint = Color.Red) }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("Income") },
                                            onClick = { viewModel.onTransactionTypeSelected(TransactionType.INCOME); typeExpanded = false },
                                            leadingIcon = { Icon(Icons.Default.ArrowUpward, contentDescription = null, tint = Color.Green) }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("All") },
                                            onClick = { viewModel.onTransactionTypeSelected(null); typeExpanded = false },
                                            leadingIcon = { Icon(Icons.Default.HorizontalRule, contentDescription = null) }
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
                                        else -> uiState.selectedTimeRange.name.lowercase().replaceFirstChar { it.uppercase() }.replace("_", " ")
                                    }
                                    OutlinedTextField(
                                        value = timeLabel,
                                        onValueChange = {},
                                        readOnly = true,
                                        maxLines = 1,
                                        label = { Text("Period", style = MaterialTheme.typography.labelSmall) },
                                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = timeExpanded) },
                                        modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true),
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
                                                text = { Text(range.name.lowercase().replaceFirstChar { it.uppercase() }.replace("_", " ")) },
                                                onClick = {
                                                    viewModel.onTimeRangeSelected(range)
                                                    timeExpanded = false
                                                    if (range == TimeRange.CUSTOM) showDatePicker = true
                                                },
                                                leadingIcon = {
                                                    Icon(when(range) {
                                                        TimeRange.TODAY -> Icons.Default.Today
                                                        TimeRange.THIS_WEEK -> Icons.Default.DateRange
                                                        TimeRange.THIS_MONTH -> Icons.Default.CalendarMonth
                                                        TimeRange.THIS_YEAR -> Icons.Default.CalendarToday
                                                        TimeRange.CUSTOM -> Icons.Default.EditCalendar
                                                        else -> Icons.Default.History
                                                    }, contentDescription = null)
                                                }
                                            )
                                        }
                                    }

                                    if (showDatePicker) {
                                        val dateRangePickerState = rememberDateRangePickerState()
                                        DatePickerDialog(
                                            onDismissRequest = { showDatePicker = false },
                                            confirmButton = {
                                                TextButton(onClick = {
                                                    val start = dateRangePickerState.selectedStartDateMillis?.let {
                                                        Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
                                                    }
                                                    val end = dateRangePickerState.selectedEndDateMillis?.let {
                                                        Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
                                                    }
                                                    viewModel.onDateRangeSelected(start, end)
                                                    showDatePicker = false
                                                }) { Text("Confirm") }
                                            },
                                            dismissButton = {
                                                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
                                            }
                                        ) {
                                            DateRangePicker(
                                                state = dateRangePickerState,
                                                modifier = Modifier.weight(1f).padding(16.dp),
                                                title = { Text("Select Date Range", modifier = Modifier.padding(16.dp)) }
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
                                val selectedCategory = uiState.allCategories.find { it.id == uiState.selectedCategoryId }
                                OutlinedTextField(
                                    value = selectedCategory?.name ?: "All Categories",
                                    onValueChange = {},
                                    readOnly = true,
                                    maxLines = 1,
                                    label = { Text("Category Filter", style = MaterialTheme.typography.labelSmall) },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                                    modifier = Modifier.fillMaxWidth().menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true),
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
                                        onClick = { viewModel.onCategorySelected(null); categoryExpanded = false },
                                        leadingIcon = { Icon(Icons.Default.Category, contentDescription = null) }
                                    )
                                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                                    uiState.allCategories.forEach { category ->
                                        DropdownMenuItem(
                                            text = { Text(category.name, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                                            onClick = { viewModel.onCategorySelected(category.id); categoryExpanded = false },
                                            leadingIcon = {
                                                Box(modifier = Modifier.size(12.dp).background(Color(category.color), CircleShape))
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        AnalysisChartType.entries.forEachIndexed { index, type ->
                            SegmentedButton(
                                selected = uiState.selectedChartType == type,
                                onClick = { viewModel.onChartTypeSelected(type) },
                                shape = SegmentedButtonDefaults.itemShape(index = index, count = AnalysisChartType.entries.size),
                                label = { 
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = when(type) {
                                                AnalysisChartType.BAR -> Icons.Default.BarChart
                                                AnalysisChartType.LINE -> Icons.AutoMirrored.Filled.ShowChart
                                                AnalysisChartType.PIE -> Icons.Default.PieChart
                                            },
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(Modifier.width(4.dp))
                                        Text(type.name.lowercase().replaceFirstChar { it.uppercase() }, style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                            )
                        }
                    }

                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ),
                        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
                        shape = MaterialTheme.shapes.large
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(260.dp)
                                .padding(16.dp)
                        ) {
                            when (uiState.selectedChartType) {
                                AnalysisChartType.BAR -> {
                                    BarChart(
                                        dataPoints = if (uiState.selectedTransactionType == TransactionType.INCOME) uiState.incomeDataPoints else uiState.expenseDataPoints,
                                        modifier = Modifier.fillMaxSize(),
                                        color = if (uiState.selectedTransactionType == TransactionType.INCOME) Color.Green else MaterialTheme.colorScheme.primary
                                    )
                                }
                                AnalysisChartType.LINE -> {
                                    LineChart(
                                        expensePoints = uiState.expenseDataPoints,
                                        incomePoints = uiState.incomeDataPoints,
                                        showExpense = uiState.selectedTransactionType == TransactionType.EXPENSE || uiState.selectedTransactionType == null,
                                        showIncome = uiState.selectedTransactionType == TransactionType.INCOME || uiState.selectedTransactionType == null,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                                AnalysisChartType.PIE -> {
                                    PieChart(
                                        breakdown = uiState.categoryBreakdown,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                AnalysisSummaryCard(
                    income = uiState.totalIncome,
                    expense = uiState.totalExpense,
                    currencyCode = uiState.currency
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
                    EmptyState(
                        message = "No expenses to analyze yet",
                        icon = Icons.Default.Analytics
                    )
                }
            } else {
                items(uiState.categoryBreakdown) { analysis ->
                    CategoryAnalysisItem(
                        analysis = analysis,
                        currencyCode = uiState.currency
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
    currencyCode: String
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
    currencyCode: String
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 2.dp)
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
    color: Color
) {
    if (dataPoints.isEmpty()) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text("No data available", style = MaterialTheme.typography.bodyMedium)
        }
        return
    }

    val maxAmount = remember(dataPoints) { dataPoints.maxOfOrNull { it.amount } ?: 1.0 }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        dataPoints.forEach { point ->
            val barHeightRatio = (point.amount / maxAmount).toFloat()
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .drawBehind {
                            val barHeight = size.height * barHeightRatio
                            drawRoundRect(
                                color = color,
                                topLeft = Offset(0f, size.height - barHeight),
                                size = Size(size.width, barHeight),
                                cornerRadius = CornerRadius(4.dp.toPx())
                            )
                        }
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = point.label,
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun PieChart(
    breakdown: List<CategoryAnalysis>,
    modifier: Modifier = Modifier
) {
    if (breakdown.isEmpty()) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text("No data for pie chart", style = MaterialTheme.typography.bodyMedium)
        }
        return
    }

    val surfaceColor = MaterialTheme.colorScheme.surface

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(200.dp)) {
            var startAngle = -90f
            breakdown.forEach { analysis ->
                val sweepAngle = analysis.percentage * 360f
                drawArc(
                    color = Color(analysis.category.color),
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = true,
                    size = size
                )
                startAngle += sweepAngle
            }
            // Draw center hole for donut style
            drawCircle(
                color = surfaceColor,
                radius = size.minDimension / 4f
            )
        }
    }
}

@Composable
fun LineChart(
    expensePoints: List<TimeDataPoint>,
    incomePoints: List<TimeDataPoint>,
    showExpense: Boolean,
    showIncome: Boolean,
    modifier: Modifier = Modifier
) {
    val allPoints = (if (showExpense) expensePoints else emptyList()) + (if (showIncome) incomePoints else emptyList())
    if (allPoints.isEmpty() || (showExpense && expensePoints.size < 2 && !showIncome) || (showIncome && incomePoints.size < 2 && !showExpense)) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text("No data for trend graph", style = MaterialTheme.typography.bodyMedium)
        }
        return
    }

    val maxAmount = remember(allPoints) { allPoints.maxOfOrNull { it.amount } ?: 1.0 }
    val primaryColor = MaterialTheme.colorScheme.primary
    val incomeColor = Color.Green

    Canvas(modifier = modifier.padding(horizontal = 24.dp, vertical = 16.dp)) {
        val width = size.width
        val height = size.height
        
        if (showExpense && expensePoints.size >= 2) {
            val spacing = width / (expensePoints.size - 1)
            val path = Path()
            expensePoints.forEachIndexed { index, point ->
                val x = index * spacing
                val y = height - (point.amount / maxAmount).toFloat() * height
                if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
                drawCircle(primaryColor, radius = 3.dp.toPx(), center = Offset(x, y))
            }
            drawPath(path, primaryColor, style = Stroke(width = 2.dp.toPx()))
        }

        if (showIncome && incomePoints.size >= 2) {
            val spacing = width / (incomePoints.size - 1)
            val path = Path()
            incomePoints.forEachIndexed { index, point ->
                val x = index * spacing
                val y = height - (point.amount / maxAmount).toFloat() * height
                if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
                drawCircle(incomeColor, radius = 3.dp.toPx(), center = Offset(x, y))
            }
            drawPath(path, incomeColor, style = Stroke(width = 2.dp.toPx()))
        }
    }
}
