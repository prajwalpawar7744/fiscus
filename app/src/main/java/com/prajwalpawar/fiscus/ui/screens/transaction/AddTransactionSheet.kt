package com.prajwalpawar.fiscus.ui.screens.transaction

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Check
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.prajwalpawar.fiscus.data.model.Account
import com.prajwalpawar.fiscus.data.model.AccountType
import com.prajwalpawar.fiscus.data.model.Category
import com.prajwalpawar.fiscus.data.model.TransactionType
import com.prajwalpawar.fiscus.ui.components.DockedDatePicker
import com.prajwalpawar.fiscus.ui.components.FiscusConnectedToggleGroup
import com.prajwalpawar.fiscus.ui.components.FiscusSectionHeader
import com.prajwalpawar.fiscus.ui.theme.FiscusSpacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionSheet(
    viewModel: TransactionViewModel,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    // A form has no useful half-open state, so open straight to full height.
    sheetState: SheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
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

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
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

    // The sheet already pads for the navigation bar and keyboard.
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(vertical = FiscusSpacing.sm),
        verticalArrangement = Arrangement.spacedBy(FiscusSpacing.md)
    ) {
        // Chip rows scroll edge to edge, so everything else adds the side padding itself.
        val inset = Modifier.padding(horizontal = FiscusSpacing.md)

        Text(
            text = "Add transaction",
            style = MaterialTheme.typography.headlineLargeEmphasized,
            modifier = inset
        )

        // Type comes first because it changes which fields follow.
        FiscusConnectedToggleGroup(
            options = TransactionType.entries,
            selectedOption = transactionType,
            onOptionSelected = onTransactionTypeChange,
            optionLabel = { type ->
                when (type) {
                    TransactionType.EXPENSE -> "Expense"
                    TransactionType.INCOME -> "Income"
                    TransactionType.TRANSFER -> "Transfer"
                }
            },
            modifier = inset
        )

        AmountField(
            modifier = inset,
            amount = amount,
            onAmountChange = { value ->
                val filtered = value.filter { character ->
                    character.isDigit() || character == '.'
                }

                if (filtered.count { it == '.' } <= 1) {
                    amount = filtered
                }
            }
        )

        if (transactionType != TransactionType.TRANSFER) {
            ChipSection(title = "Category", titleModifier = inset) {
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
                                imageVector = if (selected) {
                                    Icons.Filled.Check
                                } else {
                                    categoryIcon(category.iconKey)
                                },
                                contentDescription = null
                            )
                        }
                    )
                }
            }
        }

        ChipSection(title = "Account", titleModifier = inset) {
            items(
                items = accounts,
                key = { it.id }
            ) { account ->
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
                            imageVector = if (selected) {
                                Icons.Filled.Check
                            } else {
                                accountIcon(account.type)
                            },
                            contentDescription = null
                        )
                    }
                )
            }
        }

        DockedDatePicker(
            modifier = inset,
            selectedDateMillis = selectedDateMillis,
            onDateSelected = {
                selectedDateMillis = it
            },
            label = "Date"
        )

        OutlinedTextField(
            value = note,
            onValueChange = {
                note = it
            },
            modifier = inset.fillMaxWidth(),
            label = {
                Text("Note")
            },
            placeholder = {
                Text("Optional")
            },
            minLines = 2,
            maxLines = 4
        )

        val buttonHeight = ButtonDefaults.MediumContainerHeight

        Button(
            onClick = {
                // Transaction creation will be connected later.
                // for now dismiss only
                onDismissRequest()
            },
            modifier = inset
                .fillMaxWidth()
                .heightIn(min = buttonHeight),
            enabled = formIsValid,
            shapes = ButtonDefaults.shapes(),
            contentPadding = ButtonDefaults.contentPaddingFor(
                buttonHeight = buttonHeight,
                hasStartIcon = true
            )
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = null,
                modifier = Modifier.size(ButtonDefaults.iconSizeFor(buttonHeight))
            )

            Spacer(
                modifier = Modifier.width(ButtonDefaults.iconSpacingFor(buttonHeight))
            )

            Text(
                text = "Add transaction",
                style = ButtonDefaults.textStyleFor(buttonHeight)
            )
        }
    }
}

@Composable
private fun AmountField(
    amount: String,
    onAmountChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val mutedColor = MaterialTheme.colorScheme.onSurfaceVariant
    val transformation = remember(mutedColor) {
        CurrencyAmountTransformation(
            symbol = "$",
            mutedColor = mutedColor
        )
    }

    BasicTextField(
        value = amount,
        onValueChange = onAmountChange,
        modifier = modifier
            .fillMaxWidth()
            .semantics {
                contentDescription = "Amount"
            },
        textStyle = MaterialTheme.typography.displayLargeEmphasized.copy(
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        ),
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Decimal
        ),
        visualTransformation = transformation,
        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary)
    )
}

// Draws the currency symbol and the "0" placeholder inside the field's own text,
// so they stay centred together with what the user types.
private class CurrencyAmountTransformation(
    private val symbol: String,
    private val mutedColor: Color
) : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val transformed = buildAnnotatedString {
            withStyle(SpanStyle(color = mutedColor)) {
                append(symbol)
            }

            if (text.isEmpty()) {
                withStyle(SpanStyle(color = mutedColor)) {
                    append("0")
                }
            } else {
                append(text)
            }
        }

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int =
                offset + symbol.length

            override fun transformedToOriginal(offset: Int): Int =
                (offset - symbol.length).coerceIn(0, text.length)
        }

        return TransformedText(transformed, offsetMapping)
    }
}

@Composable
private fun ChipSection(
    title: String,
    titleModifier: Modifier,
    chips: LazyListScope.() -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(FiscusSpacing.xs)
    ) {
        FiscusSectionHeader(
            title = title,
            modifier = titleModifier
        )

        // One scrolling row; the padding sits inside the list so chips scroll to the sheet edge.
        LazyRow(
            contentPadding = PaddingValues(horizontal = FiscusSpacing.md),
            horizontalArrangement = Arrangement.spacedBy(FiscusSpacing.sm),
            content = chips
        )
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
