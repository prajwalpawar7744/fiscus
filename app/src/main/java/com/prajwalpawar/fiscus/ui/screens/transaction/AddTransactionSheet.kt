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
import androidx.compose.material3.AssistChip
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.prajwalpawar.fiscus.data.model.Account
import com.prajwalpawar.fiscus.data.model.CategoryType
import com.prajwalpawar.fiscus.data.model.TransactionType
import com.prajwalpawar.fiscus.ui.components.DockedDatePicker
import com.prajwalpawar.fiscus.ui.components.FiscusChipIcon
import com.prajwalpawar.fiscus.ui.components.FiscusConnectedToggleGroup
import com.prajwalpawar.fiscus.ui.components.FiscusSectionHeader
import com.prajwalpawar.fiscus.ui.components.FiscusSubmitButton
import com.prajwalpawar.fiscus.ui.components.accountIcon
import com.prajwalpawar.fiscus.ui.components.categoryIcon
import com.prajwalpawar.fiscus.ui.theme.FiscusSpacing
import com.prajwalpawar.fiscus.util.currencySymbol
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionSheet(
    viewModel: TransactionViewModel,
    onDismissRequest: () -> Unit,
    onAddCategory: (CategoryType) -> Unit,
    onAddAccount: (asTransferTarget: Boolean) -> Unit,
    modifier: Modifier = Modifier,
    // A form has no useful half-open state, so open straight to full height.
    sheetState: SheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        modifier = modifier
    ) {
        AddTransactionContent(
            uiState = uiState,
            onTransactionTypeChange = viewModel::setTransactionType,
            onAmountChange = viewModel::setAmount,
            onCategorySelected = viewModel::selectCategory,
            onAccountSelected = viewModel::selectAccount,
            onToAccountSelected = viewModel::selectToAccount,
            onDateSelected = viewModel::setDate,
            onTitleChange = viewModel::setTitle,
            onNoteChange = viewModel::setNote,
            onAddCategory = {
                val type = if (uiState.form.type == TransactionType.INCOME) {
                    CategoryType.INCOME
                } else {
                    CategoryType.EXPENSE
                }
                onAddCategory(type)
            },
            onAddAccount = onAddAccount,
            onSubmit = {
                viewModel.saveTransaction {
                    // Slide the sheet away before removing it.
                    scope.launch { sheetState.hide() }.invokeOnCompletion {
                        onDismissRequest()
                    }
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun AddTransactionContent(
    uiState: TransactionUiState,
    onTransactionTypeChange: (TransactionType) -> Unit,
    onAmountChange: (String) -> Unit,
    onCategorySelected: (Long) -> Unit,
    onAccountSelected: (Long) -> Unit,
    onToAccountSelected: (Long) -> Unit,
    onDateSelected: (Long) -> Unit,
    onTitleChange: (String) -> Unit,
    onNoteChange: (String) -> Unit,
    onAddCategory: () -> Unit,
    onAddAccount: (asTransferTarget: Boolean) -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val form = uiState.form

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
            selectedOption = form.type,
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
            amount = form.amount,
            onAmountChange = onAmountChange
        )

        OutlinedTextField(
            value = form.title,
            onValueChange = onTitleChange,
            modifier = inset.fillMaxWidth(),
            label = {
                Text("Title")
            },
            placeholder = {
                Text("e.g. Lunch with friends")
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                imeAction = ImeAction.Next
            )
        )

        if (form.type != TransactionType.TRANSFER) {
            ChipSection(
                title = "Category",
                titleModifier = inset,
                addDescription = "Add category",
                onAddClick = onAddCategory
            ) {
                items(
                    items = uiState.categories,
                    key = { it.id }
                ) { category ->
                    val selected = form.categoryId == category.id

                    FilterChip(
                        selected = selected,
                        onClick = {
                            onCategorySelected(category.id)
                        },
                        label = {
                            Text(text = category.name)
                        },
                        leadingIcon = {
                            FiscusChipIcon(
                                selected = selected,
                                icon = categoryIcon(category.iconKey)
                            )
                        }
                    )
                }
            }
        }

        val isTransfer = form.type == TransactionType.TRANSFER

        AccountChipSection(
            title = if (isTransfer) "From" else "Account",
            titleModifier = inset,
            accounts = uiState.accounts,
            selectedAccountId = uiState.selectedAccountId,
            onAccountSelected = onAccountSelected,
            onAddClick = { onAddAccount(false) }
        )

        // Shown even when empty, so a second account can be added right here.
        if (isTransfer) {
            AccountChipSection(
                title = "To",
                titleModifier = inset,
                accounts = uiState.toAccounts,
                selectedAccountId = uiState.selectedToAccountId,
                onAccountSelected = onToAccountSelected,
                onAddClick = { onAddAccount(true) }
            )
        }

        DockedDatePicker(
            modifier = inset,
            selectedDateMillis = form.dateMillis,
            onDateSelected = onDateSelected,
            label = "Date"
        )

        OutlinedTextField(
            value = form.note,
            onValueChange = onNoteChange,
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

        FiscusSubmitButton(
            text = "Add transaction",
            icon = Icons.Filled.Add,
            enabled = uiState.canSubmit,
            onClick = onSubmit,
            modifier = inset
        )

        // Explain a disabled button instead of leaving the user to guess.
        val hint = when {
            uiState.saveFailed -> "Couldn't save the transaction. Try again."
            else -> when (uiState.issue) {
                TransactionFormIssue.AMOUNT -> "Enter an amount to continue"
                TransactionFormIssue.TITLE -> "Add a title to continue"
                TransactionFormIssue.CATEGORY -> "Choose a category to continue"
                TransactionFormIssue.ACCOUNT -> "Choose an account to continue"
                TransactionFormIssue.NEEDS_TWO_ACCOUNTS -> "Transfers need at least two accounts"
                TransactionFormIssue.TO_ACCOUNT -> "Choose an account to transfer to"
                null -> null
            }
        }

        hint?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodyMedium,
                color = if (uiState.saveFailed) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                textAlign = TextAlign.Center,
                modifier = inset.fillMaxWidth()
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
            symbol = currencySymbol(),
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
private fun AccountChipSection(
    title: String,
    titleModifier: Modifier,
    accounts: List<Account>,
    selectedAccountId: Long?,
    onAccountSelected: (Long) -> Unit,
    onAddClick: () -> Unit
) {
    ChipSection(
        title = title,
        titleModifier = titleModifier,
        addDescription = "Add account",
        onAddClick = onAddClick
    ) {
        items(
            items = accounts,
            key = { it.id }
        ) { account ->
            val selected = selectedAccountId == account.id

            FilterChip(
                selected = selected,
                onClick = {
                    onAccountSelected(account.id)
                },
                label = {
                    Text(text = account.name)
                },
                leadingIcon = {
                    FiscusChipIcon(
                        selected = selected,
                        icon = accountIcon(account.type)
                    )
                }
            )
        }
    }
}

@Composable
private fun ChipSection(
    title: String,
    titleModifier: Modifier,
    addDescription: String,
    onAddClick: () -> Unit,
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
            horizontalArrangement = Arrangement.spacedBy(FiscusSpacing.sm)
        ) {
            chips()

            // An action, not a choice, so it's an assist chip rather than a filter chip.
            // No key: a keyed chip would hold the scroll position when it's the only item
            // during loading, leaving the row scrolled to the end.
            item {
                AssistChip(
                    onClick = onAddClick,
                    label = {
                        Text("Add")
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = null
                        )
                    },
                    modifier = Modifier.semantics {
                        contentDescription = addDescription
                    }
                )
            }
        }
    }
}

