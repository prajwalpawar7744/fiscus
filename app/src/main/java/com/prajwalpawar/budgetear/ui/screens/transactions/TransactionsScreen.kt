package com.prajwalpawar.budgetear.ui.screens.transactions

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material3.*
import androidx.compose.material3.rememberSwipeToDismissBoxState
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
import java.text.NumberFormat
import java.time.format.DateTimeFormatter
import java.util.*
import kotlinx.coroutines.launch
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun TransactionsScreen(
    viewModel: TransactionsViewModel,
    addTransactionViewModel: AddTransactionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showBottomSheet by remember { mutableStateOf(false) }
    
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
                title = { Text("Transactions", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { padding ->
        if (listItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                EmptyState(
                    message = "No transactions found",
                    icon = Icons.AutoMirrored.Filled.ReceiptLong
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
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
                                    addTransactionViewModel.setTransactionForEdit(item.transaction)
                                    addTransactionViewModel.deleteTransaction()
                                }
                            )
                        }
                    }
                }
            }
        }
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
