package com.prajwalpawar.fiscus.ui.screens.transactions

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.prajwalpawar.fiscus.ui.components.ConfirmationDialog
import com.prajwalpawar.fiscus.ui.screens.dashboard.TransactionItem
import com.prajwalpawar.fiscus.ui.utils.EmptyState
import com.prajwalpawar.fiscus.ui.utils.rememberFiscusHaptic
import com.prajwalpawar.fiscus.ui.utils.staggeredVerticalFadeIn
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun TransactionsScreen(
    viewModel: TransactionsViewModel,
    addTransactionViewModel: AddTransactionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val haptic = rememberFiscusHaptic()
    val snackbarHostState = remember { SnackbarHostState() }
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val detailSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showBottomSheet by remember { mutableStateOf(false) }
    var showDetailSheet by remember { mutableStateOf(false) }

    var transactionToDelete by remember {
        mutableStateOf<com.prajwalpawar.fiscus.domain.model.Transaction?>(
            null
        )
    }
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(showBottomSheet) {
        if (!showBottomSheet) {
            // addTransactionViewModel.resetState() // Optionally reset
        }
    }
    remember { DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault()) }
    val dateHeaderFormatter =
        remember { DateTimeFormatter.ofPattern("EEEE, MMM dd", Locale.getDefault()) }

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

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())
    val context = androidx.compose.ui.platform.LocalContext.current

    val reportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv"),
        onResult = { uri ->
            uri?.let {
                scope.launch {
                    try {
                        val csvData = viewModel.exportTransactionsToCsv()
                        context.contentResolver.openOutputStream(it)?.use { output ->
                            output.write(csvData.toByteArray())
                        }
                        snackbarHostState.showSnackbar("Report exported successfully")
                    } catch (e: Exception) {
                        snackbarHostState.showSnackbar("Export failed: ${e.message}")
                    }
                }
            }
        }
    )

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            if (uiState.topBarStyle == "longtopbar") {
                LargeTopAppBar(
                    title = { Text("Transactions", fontWeight = FontWeight.ExtraBold) },
                    colors = TopAppBarDefaults.largeTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface,
                        scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer
                    ),
                    actions = {
                        IconButton(onClick = {
                            haptic.click()
                            reportLauncher.launch("fiscus_report_${System.currentTimeMillis()}.csv")
                        }) {
                            Icon(Icons.Default.FileDownload, contentDescription = "Export Report")
                        }
                    },
                    scrollBehavior = scrollBehavior
                )
            } else {
                TopAppBar(
                    title = { Text("Transactions", fontWeight = FontWeight.ExtraBold) },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface,
                        scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer
                    ),
                    actions = {
                        IconButton(onClick = {
                            haptic.click()
                            reportLauncher.launch("fiscus_report_${System.currentTimeMillis()}.csv")
                        }) {
                            Icon(Icons.Default.FileDownload, contentDescription = "Export Report")
                        }
                    },
                    scrollBehavior = scrollBehavior
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            var filterVisible by remember { mutableStateOf(false) }

            LaunchedEffect(Unit) {
                filterVisible = true
            }

            AnimatedVisibility(
                visible = filterVisible,
                enter = fadeIn(tween(300)),
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
                        val interactionSource = remember { MutableInteractionSource() }
                        val focused by interactionSource.collectIsFocusedAsState()

                        val scale by animateFloatAsState(
                            targetValue = if (focused && uiState.areAnimationsEnabled) 1.02f else 1f,
                            animationSpec = if (uiState.areAnimationsEnabled) tween(200) else snap()
                        )

                        // Search Bar Styled Field
                        OutlinedTextField(
                            value = uiState.searchText,
                            onValueChange = { viewModel.onSearchTextChanged(it) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .graphicsLayer {
                                    if (uiState.areAnimationsEnabled) {
                                        scaleX = scale
                                        scaleY = scale
                                    }
                                },
                            interactionSource = interactionSource,
                            placeholder = { Text("Search by title or note...") },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Search,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            trailingIcon = {
                                if (uiState.searchText.isNotEmpty()) {
                                    IconButton(onClick = {
                                    haptic.click()
                                    viewModel.onSearchTextChanged("")
                                }) {
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
                                val selectedCategory =
                                    uiState.allCategories.find { it.id == uiState.selectedCategoryId }
                                OutlinedTextField(
                                    value = selectedCategory?.name ?: "All Categories",
                                    onValueChange = {},
                                    readOnly = true,
                                    maxLines = 1,
                                    label = {
                                        Text(
                                            "Category",
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    },
                                    trailingIcon = {
                                        ExposedDropdownMenuDefaults.TrailingIcon(
                                            expanded = categoryExpanded
                                        )
                                    },
                                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                                        focusedBorderColor = MaterialTheme.colorScheme.primary
                                    ),
                                    modifier = Modifier.menuAnchor(
                                        ExposedDropdownMenuAnchorType.PrimaryNotEditable,
                                        true
                                    ),
                                    shape = MaterialTheme.shapes.medium,
                                    textStyle = MaterialTheme.typography.bodyMedium
                                )
                                ExposedDropdownMenu(
                                    expanded = categoryExpanded,
                                    onDismissRequest = { categoryExpanded = false }
                                ) {
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                "All Categories",
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        },
                                        onClick = {
                                            viewModel.onCategorySelected(null)
                                            categoryExpanded = false
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
                                                viewModel.onCategorySelected(category.id)
                                                categoryExpanded = false
                                            },
                                            leadingIcon = {
                                                // Optional: Display a small color circle if available
                                                Box(
                                                    modifier = Modifier
                                                        .size(12.dp)
                                                        .background(
                                                            Color(category.color),
                                                            CircleShape
                                                        )
                                                )
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

                                    else -> uiState.selectedTimeRange.name.lowercase()
                                        .replaceFirstChar { it.uppercase() }.replace("_", " ")
                                }

                                OutlinedTextField(
                                    value = timeLabel,
                                    onValueChange = {},
                                    readOnly = true,
                                    maxLines = 1,
                                    label = {
                                        Text(
                                            "Time Period",
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    },
                                    trailingIcon = {
                                        ExposedDropdownMenuDefaults.TrailingIcon(
                                            expanded = timeExpanded
                                        )
                                    },
                                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                                        focusedBorderColor = MaterialTheme.colorScheme.primary
                                    ),
                                    modifier = Modifier.menuAnchor(
                                        ExposedDropdownMenuAnchorType.PrimaryNotEditable,
                                        true
                                    ),
                                    shape = MaterialTheme.shapes.medium,
                                    textStyle = MaterialTheme.typography.bodyMedium
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
                                                        .replace("_", " "),
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            },
                                            onClick = {
                                                viewModel.onTimeRangeSelected(range)
                                                timeExpanded = false
                                                if (range == TimeRange.CUSTOM) {
                                                    showDatePicker = true
                                                }
                                            },
                                            leadingIcon = {
                                                val icon = when (range) {
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
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(450.dp),
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
                }
            }

            if (listItems.isEmpty()) {
                var emptyVisible by remember { mutableStateOf(false) }

                LaunchedEffect(Unit) {
                    emptyVisible = true
                }

                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.animation.AnimatedVisibility(
                        visible = emptyVisible,
                        enter = fadeIn(tween(500)) + scaleIn(initialScale = 0.9f)
                    ) {
                        EmptyState(
                            message = if (uiState.searchText.isNotEmpty() || uiState.selectedCategoryId != null || uiState.selectedTimeRange != TimeRange.ALL)
                                "No matching transactions" else "No transactions found",
                            icon = Icons.AutoMirrored.Filled.ReceiptLong
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    itemsIndexed(
                        items = listItems,
                        key = { _, item ->
                            when (item) {
                                is TransactionsListItem.MonthHeader -> "month_${item.monthYear}"
                                is TransactionsListItem.DateHeader -> "date_${item.dateText}"
                                is TransactionsListItem.TransactionRow -> "trans_${item.transaction.id ?: item.transaction.hashCode()}"
                            }
                        },
                        contentType = { index, item ->
                            when (item) {
                                is TransactionsListItem.MonthHeader -> "month"
                                is TransactionsListItem.DateHeader -> "date"
                                is TransactionsListItem.TransactionRow -> "transaction"
                            }
                        }
                    ) { index, item ->
                        val itemModifier = Modifier.staggeredVerticalFadeIn(index, enabled = uiState.areAnimationsEnabled, initialDelay = 150)

                        Box(modifier = itemModifier) {
                            when (item) {
                                is TransactionsListItem.MonthHeader -> {
                                    Text(
                                        text = item.monthYear,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp, vertical = 20.dp)
                                            .padding(bottom = 8.dp),
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }

                                is TransactionsListItem.DateHeader -> {
                                    Text(
                                        text = item.dateText.uppercase(),
                                        modifier = Modifier.padding(
                                            horizontal = 16.dp,
                                            vertical = 8.dp
                                        ),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                            alpha = 0.8f
                                        ),
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    )
                                }

                                is TransactionsListItem.TransactionRow -> {
                                    SwipeableTransactionItem(
                                        transaction = item.transaction,
                                        category = uiState.categories[item.transaction.categoryId],
                                        account = uiState.accounts[item.transaction.accountId],
                                        toAccount = item.transaction.toAccountId?.let { uiState.accounts[item.transaction.toAccountId] },
                                        currencyCode = uiState.currency,
                                        onEdit = {
                                            haptic.click()
                                            viewModel.onTransactionClick(item.transaction)
                                            showDetailSheet = true
                                        },
                                        onDelete = {
                                            haptic.longClick()
                                            transactionToDelete = item.transaction
                                            showDeleteDialog = true
                                        },
                                        animationsEnabled = uiState.areAnimationsEnabled
                                    )
                                }
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
                (transactionToDelete ?: uiState.selectedTransactionDetail)?.let {
                    addTransactionViewModel.setTransactionForEdit(it)
                    addTransactionViewModel.deleteTransaction()
                }
                showDeleteDialog = false
                showDetailSheet = false
                viewModel.clearSelectedTransaction()
                transactionToDelete = null
                scope.launch {
                    snackbarHostState.showSnackbar("Transaction deleted")
                }
            },
            title = "Delete Transaction?",
            text = "Are you sure you want to delete this transaction? This action cannot be undone.",
            confirmButtonText = "Delete",
            icon = Icons.Default.Delete
        )
    }
    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                showBottomSheet = false
                addTransactionViewModel.resetState()
            },
            sheetState = bottomSheetState,
            dragHandle = { BottomSheetDefaults.DragHandle() },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = MaterialTheme.shapes.extraLarge
        ) {
            AddTransactionScreen(
                viewModel = addTransactionViewModel,
                onDismiss = {
                    showBottomSheet = false
                }
            )
        }
    }

    if (showDetailSheet && uiState.selectedTransactionDetail != null) {
        ModalBottomSheet(
            onDismissRequest = {
                showDetailSheet = false
                viewModel.clearSelectedTransaction()
            },
            sheetState = detailSheetState,
            dragHandle = { BottomSheetDefaults.DragHandle() },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = MaterialTheme.shapes.extraLarge
        ) {
            val transaction = uiState.selectedTransactionDetail!!
            TransactionDetailScreen(
                transaction = transaction,
                category = uiState.categories[transaction.categoryId],
                account = uiState.accounts[transaction.accountId],
                toAccount = transaction.toAccountId?.let { uiState.accounts[it] },
                currencyCode = uiState.currency,
                onEdit = {
                    showDetailSheet = false
                    addTransactionViewModel.setTransactionForEdit(transaction)
                    scope.launch {
                        delay(200)
                        showBottomSheet = true
                    }
                },
                onDelete = {
                    transactionToDelete = transaction
                    showDeleteDialog = true
                },
                onDismiss = {
                    showDetailSheet = false
                    viewModel.clearSelectedTransaction()
                },
                animationsEnabled = uiState.areAnimationsEnabled
            )
        }
    }
}

sealed class TransactionsListItem {
    data class MonthHeader(val monthYear: String) : TransactionsListItem()
    data class DateHeader(val dateText: String) : TransactionsListItem()
    data class TransactionRow(val transaction: com.prajwalpawar.fiscus.domain.model.Transaction) :
        TransactionsListItem()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeableTransactionItem(
    transaction: com.prajwalpawar.fiscus.domain.model.Transaction,
    category: com.prajwalpawar.fiscus.domain.model.Category?,
    currencyCode: String,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    account: com.prajwalpawar.fiscus.domain.model.Account? = null,
    toAccount: com.prajwalpawar.fiscus.domain.model.Account? = null,
    animationsEnabled: Boolean = true
) {
    val dismissState = rememberSwipeToDismissBoxState()

    LaunchedEffect(dismissState.currentValue) {
        if (dismissState.currentValue == SwipeToDismissBoxValue.EndToStart) {
            onDelete()
            dismissState.snapTo(SwipeToDismissBoxValue.Settled)
        }
    }

    val scale by animateFloatAsState(
        targetValue = if (dismissState.targetValue != SwipeToDismissBoxValue.Settled && animationsEnabled) 0.95f else 1f
    )

    val alpha by animateFloatAsState(
        targetValue = if (dismissState.targetValue != SwipeToDismissBoxValue.Settled && animationsEnabled) 0.7f else 1f
    )

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
                    .padding(vertical = 12.dp, horizontal = 12.dp)
                    .clip(MaterialTheme.shapes.medium)
                    .background(color)
                    .padding(horizontal = 24.dp)
                    .graphicsLayer {
                        if (animationsEnabled) {
                            scaleX = scale
                            scaleY = scale
                            this.alpha = alpha
                        }
                    },
                contentAlignment = Alignment.CenterEnd
            ) {
                if (dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart) {
                    Icon(
                        imageVector = Icons.Default.Delete,
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
            modifier = Modifier,
            transaction = transaction,
            category = category,
            account = account,
            toAccount = toAccount,
            currencyCode = currencyCode,
            animationsEnabled = animationsEnabled,
            onClick = {
                onEdit()
            }
        )
    }
}
