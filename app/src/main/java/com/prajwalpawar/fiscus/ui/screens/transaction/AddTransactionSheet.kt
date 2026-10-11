package com.prajwalpawar.fiscus.ui.screens.transaction

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selectableGroup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.window.Popup
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.prajwalpawar.fiscus.data.model.Account
import com.prajwalpawar.fiscus.data.model.Category
import com.prajwalpawar.fiscus.data.model.TransactionType
import com.prajwalpawar.fiscus.ui.components.DockedDatePicker
import com.prajwalpawar.fiscus.ui.theme.FiscusSpacing
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.TrendingUp
import com.prajwalpawar.fiscus.data.model.AccountType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionSheet(
    viewModel: TransactionViewModel,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberBottomSheetState(
        initialValue = SheetValue.Hidden
    )
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        modifier = modifier
    ) {
        AddTransactionContent(
            categories = uiState.categories,
            accounts = uiState.accounts,
            transactionType = uiState.transactionType,
            onTransactionTypeChange = viewModel::setTransactionType,
            onDismissRequest = onDismissRequest
        )
    }
}

@Composable
private fun AddTransactionContent(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    categories: List<Category>,
    accounts: List<Account>,
    transactionType: TransactionType,
    onTransactionTypeChange: (TransactionType) -> Unit
) {
    var selectedCategoryId by remember {
        mutableStateOf<Long?>(null)
    }

    var selectedAccountId by remember {
        mutableStateOf<Long?>(null)
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

    val formIsValid = amountIsValid && when (transactionType) {
        TransactionType.EXPENSE,
        TransactionType.INCOME -> {
            selectedCategoryId != null &&
                    selectedAccountId != null
        }

        TransactionType.TRANSFER -> false
    }

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
                        onTransactionTypeChange(type)
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

        if (transactionType != TransactionType.TRANSFER) {
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
                items(
                    items = categories,
                    key = { it.id }
                ) { category ->
                    val selected = selectedCategoryId == category.id

                    FilterChip(
                        selected = selected,
                        onClick = {
                            selectedCategoryId = category.id
                        },
                        label = {
                            Text(text = category.name)
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = categoryIcon(category.iconKey),
                                contentDescription = null
                            )
                        },
                        trailingIcon = if (selected) {
                            {
                                Icon(
                                    imageVector = Icons.Filled.Check,
                                    contentDescription = "Selected"
                                )
                            }
                        } else {
                            null
                        }
                    )
                }
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
                    val selected = selectedAccountId == account.id

                    FilterChip(
                        selected = selected,
                        onClick = {
                            selectedAccountId = account.id
                        },
                        label = {
                            Text(text = account.name)
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = accountIcon(account.type),
                                contentDescription = null
                            )
                        },
                        trailingIcon = if (selected) {
                            {
                                Icon(
                                    imageVector = Icons.Filled.Check,
                                    contentDescription = "Selected"
                                )
                            }
                        } else {
                            null
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
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = null
            )

            Spacer(
                modifier = Modifier.width(FiscusSpacing.sm)
            )

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

private fun categoryIcon(iconKey: String): ImageVector {
    return when (iconKey) {
        "restaurant" -> Icons.Filled.Restaurant
        "directions_car" -> Icons.Filled.DirectionsCar
        "shopping_bag" -> Icons.Filled.ShoppingBag
        "receipt" -> Icons.Filled.Receipt
        "health" -> Icons.Filled.Favorite
        "school" -> Icons.Filled.School
        "movie" -> Icons.Filled.Movie
        "home" -> Icons.Filled.Home
        "payments" -> Icons.Filled.Payments
        "business" -> Icons.Filled.Business
        "trending_up" -> Icons.Filled.TrendingUp
        "card_giftcard" -> Icons.Filled.CardGiftcard
        "category" -> Icons.Filled.Category
        else -> Icons.Filled.Category
    }
}

private fun accountIcon(type: AccountType): ImageVector {
    return when (type) {
        AccountType.CASH -> Icons.Filled.Payments
        AccountType.BANK -> Icons.Filled.AccountBalance
        AccountType.CREDIT_CARD -> Icons.Filled.CreditCard
        AccountType.E_WALLET -> Icons.Filled.AccountBalanceWallet
        AccountType.OTHER -> Icons.Filled.Savings
    }
}