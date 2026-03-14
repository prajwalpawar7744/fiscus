package com.prajwalpawar.budgetear.ui.screens.transactions

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prajwalpawar.budgetear.ui.screens.dashboard.TransactionItem
import com.prajwalpawar.budgetear.ui.utils.EmptyState
import com.prajwalpawar.budgetear.ui.utils.formatCurrency
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*
import kotlinx.coroutines.launch
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.text.style.TextOverflow
import com.prajwalpawar.budgetear.ui.components.ConfirmationDialog

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun TransactionsScreen(
    viewModel: TransactionsViewModel,
    addTransactionViewModel: AddTransactionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showBottomSheet by remember { mutableStateOf(false) }

    var transactionToDelete by remember { mutableStateOf<com.prajwalpawar.budgetear.domain.model.Transaction?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    LaunchedEffect(showBottomSheet) {
        if (!showBottomSheet) {
            // addTransactionViewModel.resetState() // Optionally reset
        }
    }
    val monthFormatter = remember { DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault()) }
    val dateHeaderFormatter = remember { DateTimeFormatter.ofPattern("EEEE, MMM dd", Locale.getDefault()) }

    val listItems = remember(uiState.groupedTransactions) {
        val items = mutableListOf<TransactionsListItem>()
        uiState.groupedTransactions.forEach { (monthYear, dateGroups) ->
            items.add(TransactionsListItem.MonthHeader(monthYear))
            dateGroups.forEach { (date, transactions) ->
                items.add(TransactionsListItem.DateHeader(date.format(dateHeaderFormatter)))
                transactions.forEach { transaction ->
                    items.add(TransactionsListItem.TransactionRow(transaction))
                }
            }
        }
        items
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Transactions", fontWeight = FontWeight.ExtraBold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Refined Filter Section
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                shape = MaterialTheme.shapes.extraLarge,
                tonalElevation = 2.dp
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Search Bar Styled Field
                    OutlinedTextField(
                        value = uiState.searchText,
                        onValueChange = { viewModel.onSearchTextChanged(it) },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Search by title or note...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                        trailingIcon = {
                            if (uiState.searchText.isNotEmpty()) {
                                IconButton(onClick = { viewModel.onSearchTextChanged("") }) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear")
                                }
                            }
                        },
                        singleLine = true,
                        shape = MaterialTheme.shapes.large,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = Color.Transparent,
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        )
                    )

                    // Compact Dropdown Filters
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Category Dropdown
                        var categoryExpanded by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(
                            expanded = categoryExpanded,
                            onExpandedChange = { categoryExpanded = !categoryExpanded },
                            modifier = Modifier.weight(1f)
                        ) {
                            val selectedCategory = uiState.allCategories.find { it.id == uiState.selectedCategoryId }
                            OutlinedTextField(
                                value = selectedCategory?.name ?: "All Categories",
                                onValueChange = {},
                                readOnly = true,
                                maxLines = 1,
                                label = { Text("Category", style = MaterialTheme.typography.labelSmall) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                                    focusedBorderColor = MaterialTheme.colorScheme.primary
                                ),
                                modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true),
                                shape = MaterialTheme.shapes.medium,
                                textStyle = MaterialTheme.typography.bodyMedium
                            )
                            ExposedDropdownMenu(
                                expanded = categoryExpanded,
                                onDismissRequest = { categoryExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("All Categories", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                                    onClick = {
                                        viewModel.onCategorySelected(null)
                                        categoryExpanded = false
                                    },
                                    leadingIcon = { Icon(Icons.Default.Category, contentDescription = null) }
                                )
                                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                                uiState.allCategories.forEach { category ->
                                    DropdownMenuItem(
                                        text = { Text(category.name, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                                        onClick = {
                                            viewModel.onCategorySelected(category.id)
                                            categoryExpanded = false
                                        },
                                        leadingIcon = { 
                                            // Optional: Display a small color circle if available
                                            Box(modifier = Modifier.size(12.dp).background(Color(category.color), CircleShape))
                                        }
                                    )
                                }
                            }
                        }

                        // Time Range Dropdown
                        var timeExpanded by remember { mutableStateOf(false) }
                        var showDatePicker by remember { mutableStateOf(false) }
                        
                        ExposedDropdownMenuBox(
                            expanded = timeExpanded,
                            onExpandedChange = { timeExpanded = !timeExpanded },
                            modifier = Modifier.weight(1f)
                        ) {
                            val timeLabel = when (uiState.selectedTimeRange) {
                                TimeRange.CUSTOM -> if (uiState.startDate != null && uiState.endDate != null) {
                                    "${uiState.startDate} - ${uiState.endDate}"
                                } else "Custom Range"
                                else -> uiState.selectedTimeRange.name.lowercase().replaceFirstChar { it.uppercase() }.replace("_", " ")
                            }
                            
                            OutlinedTextField(
                                value = timeLabel,
                                onValueChange = {},
                                readOnly = true,
                                maxLines = 1,
                                label = { Text("Time Period", style = MaterialTheme.typography.labelSmall) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = timeExpanded) },
                                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                                    focusedBorderColor = MaterialTheme.colorScheme.primary
                                ),
                                modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true),
                                shape = MaterialTheme.shapes.medium,
                                textStyle = MaterialTheme.typography.bodyMedium
                            )
                            ExposedDropdownMenu(
                                expanded = timeExpanded,
                                onDismissRequest = { timeExpanded = false }
                            ) {
                                TimeRange.entries.forEach { range ->
                                    DropdownMenuItem(
                                        text = { Text(range.name.lowercase().replaceFirstChar { it.uppercase() }.replace("_", " "), maxLines = 1, overflow = TextOverflow.Ellipsis) },
                                        onClick = {
                                            viewModel.onTimeRangeSelected(range)
                                            timeExpanded = false
                                            if (range == TimeRange.CUSTOM) {
                                                showDatePicker = true
                                            }
                                        },
                                        leadingIcon = { 
                                            val icon = when(range) {
                                                TimeRange.TODAY -> Icons.Default.Today
                                                TimeRange.THIS_WEEK -> Icons.Default.DateRange
                                                TimeRange.THIS_MONTH -> Icons.Default.CalendarMonth
                                                TimeRange.THIS_YEAR -> Icons.Default.CalendarToday
                                                TimeRange.CUSTOM -> Icons.Default.EditCalendar
                                                else -> Icons.Default.History
                                            }
                                            Icon(icon, contentDescription = null)
                                        }
                                    )
                                }
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
                                    modifier = Modifier.fillMaxWidth().height(450.dp),
                                    title = { Text("Select Date Range", modifier = Modifier.padding(16.dp)) }
                                )
                            }
                        }
                    }
                }
            }

            if (listItems.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    EmptyState(
                        message = if (uiState.searchText.isNotEmpty() || uiState.selectedCategoryId != null || uiState.selectedTimeRange != TimeRange.ALL) 
                                 "No matching transactions" else "No transactions found",
                        icon = Icons.AutoMirrored.Filled.ReceiptLong
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(
                        items = listItems,
                        key = { item ->
                            when (item) {
                                is TransactionsListItem.MonthHeader -> "month_${item.monthYear}"
                                is TransactionsListItem.DateHeader -> "date_${item.dateText}"
                                is TransactionsListItem.TransactionRow -> "trans_${item.transaction.id ?: item.transaction.hashCode()}"
                            }
                        },
                        contentType = { item ->
                            when (item) {
                                is TransactionsListItem.MonthHeader -> "month"
                                is TransactionsListItem.DateHeader -> "date"
                                is TransactionsListItem.TransactionRow -> "transaction"
                            }
                        }
                    ) { item ->
                        when (item) {
                            is TransactionsListItem.MonthHeader -> {
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.95f),
                                    tonalElevation = 2.dp
                                ) {
                                    Text(
                                        text = item.monthYear,
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                            is TransactionsListItem.DateHeader -> {
                                Text(
                                    text = item.dateText,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            is TransactionsListItem.TransactionRow -> {
                                SwipeableTransactionItem(
                                    transaction = item.transaction,
                                    category = uiState.categories[item.transaction.categoryId],
                                    currencyCode = uiState.currency,
                                    onEdit = {
                                        addTransactionViewModel.setTransactionForEdit(item.transaction)
                                        showBottomSheet = true
                                    },
                                    onDelete = {
                                        transactionToDelete = item.transaction
                                        showDeleteDialog = true
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDeleteDialog && transactionToDelete != null) {
        ConfirmationDialog(
            onDismissRequest = { 
                showDeleteDialog = false
                transactionToDelete = null
            },
            onConfirm = {
                transactionToDelete?.let { transaction ->
                    addTransactionViewModel.setTransactionForEdit(transaction)
                    addTransactionViewModel.deleteTransaction()
                    scope.launch {
                        snackbarHostState.showSnackbar("Transaction deleted")
                    }
                }
                showDeleteDialog = false
                transactionToDelete = null
            },
            title = "Delete Transaction?",
            text = "Are you sure you want to delete this transaction? This action cannot be undone.",
            confirmButtonText = "Delete",
            icon = Icons.Default.Delete
        )
    }
    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            sheetState = sheetState,
            dragHandle = { BottomSheetDefaults.DragHandle() }
        ) {
            AddTransactionScreen(
                viewModel = addTransactionViewModel,
                onDismiss = {
                    scope.launch { 
                        sheetState.hide()
                    }.invokeOnCompletion {
                        if (!sheetState.isVisible) {
                            showBottomSheet = false
                        }
                    }
                }
            )
        }
    }
}

sealed class TransactionsListItem {
    data class MonthHeader(val monthYear: String) : TransactionsListItem()
    data class DateHeader(val dateText: String) : TransactionsListItem()
    data class TransactionRow(val transaction: com.prajwalpawar.budgetear.domain.model.Transaction) : TransactionsListItem()
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeableTransactionItem(
    transaction: com.prajwalpawar.budgetear.domain.model.Transaction,
    category: com.prajwalpawar.budgetear.domain.model.Category?,
    currencyCode: String,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState()

    LaunchedEffect(dismissState.currentValue) {
        if (dismissState.currentValue == SwipeToDismissBoxValue.EndToStart) {
            onDelete()
            dismissState.snapTo(SwipeToDismissBoxValue.Settled)
        }
    }

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            val color = when (dismissState.dismissDirection) {
                SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.errorContainer
                else -> Color.Transparent
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color)
                    .padding(horizontal = 24.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                if (dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        },
        enableDismissFromEndToStart = true,
        enableDismissFromStartToEnd = false
    ) {
        TransactionItem(
            transaction = transaction,
            category = category,
            currencyCode = currencyCode,
            onClick = onEdit
        )
    }
}
