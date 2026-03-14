package com.prajwalpawar.budgetear.ui.screens.analysis

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
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
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalysisScreen(
    viewModel: AnalysisViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Analysis", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
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
                        shape = MaterialTheme.shapes.large,
                        tonalElevation = 1.dp
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
                BarChart(
                    dataPoints = uiState.expenseDataPoints,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .padding(vertical = 8.dp),
                    color = MaterialTheme.colorScheme.primary
                )
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
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 8.dp)
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
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Surface(
                    color = Color(analysis.category.color).copy(alpha = 0.1f),
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = getCategoryIcon(analysis.category.icon),
                            contentDescription = null,
                            tint = Color(analysis.category.color),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = analysis.category.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = formatCurrency(analysis.amount, currencyCode),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${(analysis.percentage * 100).toInt()}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        LinearProgressIndicator(
            progress = { analysis.percentage },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp),
            color = Color(analysis.category.color),
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
            strokeCap = StrokeCap.Round
        )
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
