package com.prajwalpawar.fiscus.ui.screens.transaction

import androidx.compose.foundation.layout.Arrangement
import com.prajwalpawar.fiscus.util.formatMoney
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.Alignment
import androidx.compose.runtime.key
import androidx.compose.material3.Surface
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material.icons.rounded.Close
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.AssistChip
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.prajwalpawar.fiscus.data.model.Account
import com.prajwalpawar.fiscus.data.model.CategoryType
import com.prajwalpawar.fiscus.data.model.TransactionType
import com.prajwalpawar.fiscus.ui.components.DockedDatePicker
import com.prajwalpawar.fiscus.ui.components.FiscusChipIcon
import com.prajwalpawar.fiscus.ui.components.FiscusConnectedToggleGroup
import com.prajwalpawar.fiscus.ui.components.FiscusSectionHeader
import com.prajwalpawar.fiscus.ui.components.FiscusSubmitButton
import com.prajwalpawar.fiscus.ui.components.ImeInsetsRepair
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
        // The sheet is its own window, so it needs its own repair.
        ImeInsetsRepair()

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
            onAddItem = viewModel::addItem,
            onItemChange = viewModel::updateItem,
            onRemoveItem = viewModel::removeItem,
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
    onAddItem: () -> Unit,
    onItemChange: (key: Long, transform: (ItemDraft) -> ItemDraft) -> Unit,
    onRemoveItem: (key: Long) -> Unit,
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
            text = if (form.editingId != null) "Edit transaction" else "Add transaction",
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
            amount = form.effectiveAmount,
            onAmountChange = onAmountChange,
            // With items, the amount is their sum and can't be typed over.
            readOnly = form.usesItems
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
            ItemsSection(
                items = form.items,
                onAddItem = onAddItem,
                onItemChange = onItemChange,
                onRemoveItem = onRemoveItem,
                modifier = inset
            )

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
            text = if (form.editingId != null) "Save changes" else "Add transaction",
            icon = if (form.editingId != null) Icons.Rounded.Check else Icons.Rounded.Add,
            enabled = uiState.canSubmit,
            onClick = onSubmit,
            modifier = inset
        )

        // Explain a disabled button instead of leaving the user to guess.
        val hint = when {
            uiState.saveFailed -> "Couldn't save the transaction. Try again."
            else -> when (uiState.issue) {
                TransactionFormIssue.ITEMS -> "Give every item a name, quantity and price"
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
    modifier: Modifier = Modifier,
    readOnly: Boolean = false
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
        readOnly = readOnly,
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
                            imageVector = Icons.Rounded.Add,
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


// Optional breakdown: each item is name, quantity × price; the amount becomes their sum.
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ItemsSection(
    items: List<ItemDraft>,
    onAddItem: () -> Unit,
    onItemChange: (key: Long, transform: (ItemDraft) -> ItemDraft) -> Unit,
    onRemoveItem: (key: Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)
    ) {
        FiscusSectionHeader(title = if (items.isEmpty()) "Items (optional)" else "Items")

        items.forEachIndexed { index, item ->
            key(item.key) {
                ItemRow(
                    item = item,
                    position = index + 1,
                    shape = ListItemDefaults.segmentedShapes(index = index, count = items.size).shape,
                    onChange = { transform -> onItemChange(item.key, transform) },
                    onRemove = { onRemoveItem(item.key) }
                )
            }
        }

        OutlinedButton(
            onClick = onAddItem,
            shapes = ButtonDefaults.shapes(),
            modifier = Modifier.padding(top = FiscusSpacing.xs)
        ) {
            Icon(
                imageVector = Icons.Rounded.Add,
                contentDescription = null,
                modifier = Modifier.size(ButtonDefaults.IconSize)
            )
            Text(
                text = if (items.isEmpty()) "Split into items" else "Add item",
                modifier = Modifier.padding(start = ButtonDefaults.IconSpacing)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ItemRow(
    item: ItemDraft,
    position: Int,
    shape: Shape,
    onChange: ((ItemDraft) -> ItemDraft) -> Unit,
    onRemove: () -> Unit
) {
    Surface(
        shape = shape,
        color = MaterialTheme.colorScheme.surfaceContainer
    ) {
        Column(
            modifier = Modifier.padding(FiscusSpacing.sm),
            verticalArrangement = Arrangement.spacedBy(FiscusSpacing.xs)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = item.name,
                    onValueChange = { name -> onChange { it.copy(name = name) } },
                    modifier = Modifier.weight(1f),
                    label = { Text("Item $position") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Next
                    )
                )

                IconButton(
                    onClick = onRemove,
                    shapes = IconButtonDefaults.shapes()
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Remove item $position"
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(FiscusSpacing.sm)
            ) {
                OutlinedTextField(
                    value = item.quantity,
                    onValueChange = { quantity -> onChange { it.copy(quantity = quantity) } },
                    modifier = Modifier.weight(1f),
                    label = { Text("Qty") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Next
                    )
                )

                OutlinedTextField(
                    value = item.price,
                    onValueChange = { price -> onChange { it.copy(price = price) } },
                    modifier = Modifier.weight(2f),
                    label = { Text("Price each") },
                    prefix = { Text(currencySymbol()) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Next
                    )
                )
            }

            item.total()?.let { total ->
                Text(
                    text = "= ${formatMoney(total)}",
                    style = MaterialTheme.typography.titleSmallEmphasized,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.End,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(end = FiscusSpacing.sm)
                )
            }
        }
    }
}
