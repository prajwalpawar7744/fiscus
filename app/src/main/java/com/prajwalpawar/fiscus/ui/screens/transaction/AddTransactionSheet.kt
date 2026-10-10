package com.prajwalpawar.fiscus.ui.screens.transaction

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selectableGroup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.window.Popup
import com.prajwalpawar.fiscus.data.model.TransactionType
import com.prajwalpawar.fiscus.ui.components.DockedDatePicker
import com.prajwalpawar.fiscus.ui.theme.FiscusSpacing
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberBottomSheetState(
        initialValue = SheetValue.Hidden
    )
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        modifier = modifier
    ) {
        AddTransactionContent(
            onDismissRequest = onDismissRequest
        )
    }
}

@Composable
private fun AddTransactionContent(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    val categories = listOf(
        "Food",
        "Transport",
        "Shopping",
        "Bills",
        "Entertainment",
        "Health",
        "Education"
    )

    val accounts = listOf(
        "Cash",
        "HDFC Bank",
        "Credit Card",
        "Savings"
    )

    var selectedCategory by remember {
        mutableStateOf<String?>(null)
    }

    var selectedAccount by remember {
        mutableStateOf<String?>(null)
    }

    var transactionType by remember {
        mutableStateOf(TransactionType.EXPENSE)
    }

    var amount by remember {
        mutableStateOf("")
    }

    var note by remember {
        mutableStateOf("")
    }

    var selectedDateMillis by remember {
        mutableStateOf(todayAsUtcMillis())
    }

    val parsedAmount = amount.toBigDecimalOrNull()

    val amountIsValid = parsedAmount?.let {
        it > java.math.BigDecimal.ZERO
    } ?: false

    val formIsValid = amountIsValid &&
            selectedCategory != null &&
            selectedAccount != null

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .navigationBarsPadding()
            .imePadding()
            .padding(
                horizontal = FiscusSpacing.md,
                vertical = FiscusSpacing.sm
            ),
        verticalArrangement = Arrangement.spacedBy(FiscusSpacing.md)
    ) {
        Text(
            text = "Add transaction",
            style = MaterialTheme.typography.headlineLargeEmphasized
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(FiscusSpacing.xs)
        ) {
            Text(
                text = "Amount",
                style = MaterialTheme.typography.titleMedium
            )

            OutlinedTextField(
                value = amount,
                onValueChange = { value ->
                    val filtered = value.filter { character ->
                        character.isDigit() || character == '.'
                    }

                    if (filtered.count { it == '.' } <= 1) {
                        amount = filtered
                    }
                },
                isError = amount.isNotBlank() && !amountIsValid,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                prefix = {
                    Text(
                        text = "$"
                    )
                },
                placeholder = {
                    Text(
                        text = "0.00"
                    )
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal
                ),
                textStyle = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.SemiBold
                )
            )
        }

        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .semantics {
                    selectableGroup()
                },
            horizontalArrangement = Arrangement.spacedBy(
                ButtonGroupDefaults.ConnectedSpaceBetween
            ),
            verticalArrangement = Arrangement.spacedBy(FiscusSpacing.md)
        ) {
            TransactionType.entries.forEachIndexed { index, type ->
                ToggleButton(
                    checked = transactionType == type,
                    onCheckedChange = {
                        transactionType = type
                    },
                    shapes = when (index) {
                        0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()

                        TransactionType.entries.lastIndex ->
                            ButtonGroupDefaults.connectedTrailingButtonShapes()

                        else ->
                            ButtonGroupDefaults.connectedMiddleButtonShapes()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .semantics {
                            role = Role.RadioButton
                        }
                ) {
                    Text(
                        text = when (type) {
                            TransactionType.EXPENSE -> "Expense"
                            TransactionType.INCOME -> "Income"
                            TransactionType.TRANSFER -> "Transfer"

                        }
                    )
                }
            }
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(FiscusSpacing.xs)
        ) {
            Text(
                text = "Category",
                style = MaterialTheme.typography.titleMedium
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(FiscusSpacing.sm)
            ) {
                items(categories) { category ->
                    FilterChip(
                        selected = selectedCategory == category,
                        onClick = {
                            selectedCategory = category
                        },
                        label = {
                            Text(text = category)
                        }
                    )
                }
            }
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(FiscusSpacing.xs)
        ) {
            Text(
                text = "Account",
                style = MaterialTheme.typography.titleMedium
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(FiscusSpacing.sm)
            ) {
                items(accounts) { account ->
                    FilterChip(
                        selected = selectedAccount == account,
                        onClick = {
                            selectedAccount = account
                        },
                        label = {
                            Text(text = account)
                        }
                    )
                }
            }
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(FiscusSpacing.xs)
        ) {
            Text(
                text = "Date",
                style = MaterialTheme.typography.titleMedium
            )

            DockedDatePicker(
                selectedDateMillis = selectedDateMillis,
                onDateSelected = {
                    selectedDateMillis = it
                },
                label = "Transaction date",
                placeholder = "Choose transaction date"
            )
        }

        OutlinedTextField(
            value = note,
            onValueChange = {
                note = it
            },
            modifier = Modifier.fillMaxWidth(),
            label = {
                Text("Note")
            },
            placeholder = {
                Text("Optional")
            },
            minLines = 2,
            maxLines = 4
        )

        // todo
        // get real account and categories table
        // redesign add transaction sheet to my3e
        // get date picker

        Button(
            onClick = {
                // Transaction creation will be connected later.
                // for now dismiss only
                onDismissRequest()
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = formIsValid
        ) {
            Text(
                text = "Add transaction"
            )
        }
    }
}

private fun todayAsUtcMillis(): Long {
    val today = java.util.Calendar.getInstance()

    return java.util.Calendar.getInstance(
        java.util.TimeZone.getTimeZone("UTC")
    ).apply {
        clear()
        set(
            today.get(java.util.Calendar.YEAR),
            today.get(java.util.Calendar.MONTH),
            today.get(java.util.Calendar.DAY_OF_MONTH)
        )
    }.timeInMillis
}