package com.prajwalpawar.budgetear.ui.screens.transactions

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.prajwalpawar.budgetear.domain.model.TransactionType
import com.prajwalpawar.budgetear.ui.components.ConfirmationDialog
import com.prajwalpawar.budgetear.ui.utils.getCategoryIcon
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    viewModel: AddTransactionViewModel,
    onDismiss: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = uiState.date.time
    )
    var showDeleteDialog by remember { mutableStateOf(false) }

    val dateFormatter = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            onDismiss()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(scrollState)
            .padding(24.dp)
            .navigationBarsPadding() // Ensures content is above navigation bar
            .imePadding(), // Ensures content is above keyboard
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = if (uiState.transactionId == null) "Add Transaction" else "Edit Transaction",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(8.dp))

        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            SegmentedButton(
                selected = uiState.type == TransactionType.EXPENSE,
                onClick = { viewModel.onTypeChange(TransactionType.EXPENSE) },
                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                label = { Text("Expense") }
            )
            SegmentedButton(
                selected = uiState.type == TransactionType.INCOME,
                onClick = { viewModel.onTypeChange(TransactionType.INCOME) },
                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                label = { Text("Income") }
            )
        }

        OutlinedTextField(
            value = uiState.title,
            onValueChange = viewModel::onTitleChange,
            label = { Text("Transaction Title") },
            placeholder = { Text("e.g. Groceries") },
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            singleLine = true
        )

        OutlinedTextField(
            value = uiState.amount,
            onValueChange = viewModel::onAmountChange,
            label = { Text("Amount") },
            placeholder = { Text("0.00") },
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            prefix = { Text("$", color = MaterialTheme.colorScheme.onSurfaceVariant) },
            singleLine = true
        )

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "Category",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 4.dp)
            ) {
                items(uiState.categories) { category ->
                    FilterChip(
                        selected = uiState.categoryId == category.id,
                        onClick = { category.id?.let { viewModel.onCategoryChange(it) } },
                        label = { Text(category.name) },
                        leadingIcon = {
                            Icon(
                                imageVector = getCategoryIcon(category.icon),
                                contentDescription = null,
                                modifier = Modifier.size(FilterChipDefaults.IconSize),
                                tint = if (uiState.categoryId == category.id)
                                    MaterialTheme.colorScheme.onSecondaryContainer
                                else
                                    Color(category.color)
                            )
                        }
                    )
                }
            }
        }

        OutlinedTextField(
            value = uiState.note,
            onValueChange = viewModel::onNoteChange,
            label = { Text("Note (Optional)") },
            placeholder = { Text("Add more details...") },
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            minLines = 3
        )

        // Date Picker Field
        OutlinedTextField(
            value = dateFormatter.format(uiState.date),
            onValueChange = {},
            label = { Text("Date") },
            readOnly = true,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showDatePicker = true },
            shape = MaterialTheme.shapes.large,
            trailingIcon = {
                IconButton(onClick = { showDatePicker = true }) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = "Select Date"
                    )
                }
            },
            enabled = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.outline,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            )
        )

        if (showDatePicker) {
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        datePickerState.selectedDateMillis?.let {
                            viewModel.onDateChange(Date(it))
                        }
                        showDatePicker = false
                    }) {
                        Text("OK")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) {
                        Text("Cancel")
                    }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = viewModel::saveTransaction,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = MaterialTheme.shapes.large,
            enabled = uiState.title.isNotBlank() && uiState.amount.isNotBlank()
        ) {
            val isEdit = uiState.transactionId != null
            val icon = if (isEdit) Icons.Default.Edit else Icons.Default.Add
            val text = if (isEdit) "Update Transaction" else "Add Transaction"

            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(text, style = MaterialTheme.typography.titleMedium)
        }

        if (uiState.transactionId != null) {
            OutlinedButton(
                onClick = { showDeleteDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = MaterialTheme.shapes.large,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text("Delete Transaction", style = MaterialTheme.typography.titleMedium)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }

    if (showDeleteDialog) {
        ConfirmationDialog(
            onDismissRequest = { showDeleteDialog = false },
            onConfirm = {
                viewModel.deleteTransaction()
                showDeleteDialog = false
            },
            title = "Delete Transaction?",
            text = "Are you sure you want to delete this transaction? This action cannot be undone.",
            confirmButtonText = "Delete",
            icon = Icons.Default.Delete
        )
    }
}
