package com.prajwalpawar.budgetear.ui.screens.transactions

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector
import com.prajwalpawar.budgetear.domain.model.TransactionType

import com.prajwalpawar.budgetear.ui.utils.getCategoryIcon

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    viewModel: AddTransactionViewModel,
    onDismiss: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

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
            text = "Add Transaction",
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

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = viewModel::saveTransaction,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = MaterialTheme.shapes.large,
            enabled = uiState.title.isNotBlank() && uiState.amount.isNotBlank()
        ) {
            Text("Save Transaction", style = MaterialTheme.typography.titleMedium)
        }
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}
