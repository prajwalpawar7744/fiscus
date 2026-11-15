package com.prajwalpawar.fiscus.ui.feature.transactioneditor

import android.content.res.Configuration
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.prajwalpawar.fiscus.R
import com.prajwalpawar.fiscus.core.money.formatMoney
import com.prajwalpawar.fiscus.core.money.parseMoneyInput
import com.prajwalpawar.fiscus.model.Account
import com.prajwalpawar.fiscus.model.AccountType
import com.prajwalpawar.fiscus.model.Category
import com.prajwalpawar.fiscus.model.CategoryKind
import com.prajwalpawar.fiscus.model.Money
import com.prajwalpawar.fiscus.model.TransactionType
import com.prajwalpawar.fiscus.ui.components.CurrencyPrefixTransformation
import com.prajwalpawar.fiscus.ui.components.DockedDatePickerField
import com.prajwalpawar.fiscus.ui.components.ShapeAccentIcon
import com.prajwalpawar.fiscus.ui.components.SingleChoiceToggleGroup
import com.prajwalpawar.fiscus.ui.components.TransactionTypeDisplayOrder
import com.prajwalpawar.fiscus.ui.components.categoryIconRes
import com.prajwalpawar.fiscus.ui.components.iconRes
import com.prajwalpawar.fiscus.ui.components.labelRes
import com.prajwalpawar.fiscus.ui.theme.FiscusLayout
import com.prajwalpawar.fiscus.ui.theme.FiscusTheme
import com.prajwalpawar.fiscus.ui.theme.tabularNumbers
import java.util.Currency
import kotlinx.datetime.LocalDate


/**
 * Stateless add/edit transaction screen: hero moment 2 (ADR-010). Expressive on the amount hero
 * and Save (containment, color role, emphasized type, motion, haptics); calm and labeled for the
 * rest of the form (M3_EXPRESSIVE.md §1). All motion uses `MaterialTheme.motionScheme`.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class) // scroll behavior, LoadingIndicator
@Composable
fun TransactionEditorScreen(
    uiState: TransactionEditorUiState,
    onEvent: (TransactionEditorEvent) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val motion = MaterialTheme.motionScheme
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeFlexibleTopAppBar(
                title = {
                    Text(
                        stringResource(
                            if (uiState.isNew) R.string.editor_title_add else R.string.editor_title_edit,
                        ),
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            painter = painterResource(R.drawable.ic_arrow_back_rounded),
                            contentDescription = stringResource(R.string.action_navigate_back),
                        )
                    }
                },
                scrollBehavior = scrollBehavior,
            )
        },
        bottomBar = {
            if (!uiState.isLoading) {
                // Primary action within thumb reach (M3_EXPRESSIVE.md §6). The keyboard covers it
                // while typing; the form pads for the keyboard so every field stays reachable.
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(FiscusLayout.ScreenMargin),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    SaveHint(hint = uiState.saveHint)
                    SaveButton(
                        enabled = uiState.canSave,
                        isSaving = uiState.isSaving,
                        onClick = { onEvent(TransactionEditorEvent.Save) },
                        modifier = Modifier.widthIn(max = FiscusLayout.MaxContentWidth),
                    )
                }
            }
        },
    ) { innerPadding ->
        AnimatedContent(
            targetState = uiState.isLoading,
            transitionSpec = {
                fadeIn(motion.defaultEffectsSpec()) togetherWith fadeOut(motion.fastEffectsSpec())
            },
            label = "editorLoading",
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) { isLoading ->
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = if (isLoading) Alignment.Center else Alignment.TopCenter,
            ) {
                if (isLoading) {
                    LoadingIndicator()
                } else {
                    EditorForm(
                        uiState = uiState,
                        onEvent = onEvent,
                        modifier = Modifier
                            .widthIn(max = FiscusLayout.MaxContentWidth)
                            .fillMaxWidth(),
                    )
                }
            }
        }
    }
}

@Composable
private fun EditorForm(
    uiState: TransactionEditorUiState,
    onEvent: (TransactionEditorEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val motion = MaterialTheme.motionScheme
    Column(
        modifier = modifier
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(vertical = FiscusLayout.ScreenMargin)
            .animateContentSize(motion.defaultSpatialSpec()),
        verticalArrangement = Arrangement.spacedBy(FiscusLayout.SectionSpacing),
    ) {
        TypeSelector(
            selected = uiState.type,
            onSelect = { onEvent(TransactionEditorEvent.TypeChanged(it)) },
            modifier = Modifier.padding(horizontal = FiscusLayout.ScreenMargin),
        )
        AmountHero(
            uiState = uiState,
            onAmountChange = { onEvent(TransactionEditorEvent.AmountChanged(it)) },
            modifier = Modifier.padding(horizontal = FiscusLayout.ScreenMargin),
        )
        SingleChoiceToggleGroup(
            title = stringResource(
                if (uiState.type == TransactionType.TRANSFER) R.string.editor_from_account else R.string.editor_account,
            ),
            options = uiState.accounts,
            selectedKey = uiState.accountId,
            key = Account::id,
            label = Account::name,
            onSelect = { onEvent(TransactionEditorEvent.AccountSelected(it)) },
            emptyMessage = stringResource(R.string.editor_no_accounts),
            modifier = Modifier.padding(horizontal = FiscusLayout.ScreenMargin),
        )
        // Category (income/expense) or destination account (transfer): swaps with a fade while the
        // container resizes on a spatial spring.
        AnimatedContent(
            targetState = uiState.type,
            transitionSpec = {
                fadeIn(motion.defaultEffectsSpec()) togetherWith
                    fadeOut(motion.fastEffectsSpec()) using
                    SizeTransform(clip = false) { _, _ -> motion.defaultSpatialSpec() }
            },
            label = "editorTypeSection",
        ) { type ->
            if (type == TransactionType.TRANSFER) {
                SingleChoiceToggleGroup(
                    title = stringResource(R.string.editor_to_account),
                    options = uiState.toAccounts,
                    selectedKey = uiState.toAccountId,
                    key = Account::id,
                    label = Account::name,
                    onSelect = { onEvent(TransactionEditorEvent.ToAccountSelected(it)) },
                    emptyMessage = stringResource(R.string.editor_no_transfer_accounts),
                    modifier = Modifier.padding(horizontal = FiscusLayout.ScreenMargin),
                )
            } else {
                SingleChoiceToggleGroup(
                    title = stringResource(R.string.editor_category),
                    options = uiState.categories,
                    selectedKey = uiState.categoryId,
                    key = Category::id,
                    label = Category::name,
                    icon = { categoryIconRes(it.iconKey) },
                    onSelect = { onEvent(TransactionEditorEvent.CategorySelected(it)) },
                    emptyMessage = stringResource(R.string.editor_no_categories),
                    modifier = Modifier.padding(horizontal = FiscusLayout.ScreenMargin),
                )
            }
        }
        uiState.date?.let { date ->
            DockedDatePickerField(
                date = date,
                onDateSelected = { onEvent(TransactionEditorEvent.DateSelected(it)) },
                label = stringResource(R.string.editor_date),
                modifier = Modifier.padding(horizontal = FiscusLayout.ScreenMargin),
            )
        }
        OutlinedTextField(
            value = uiState.note,
            onValueChange = { onEvent(TransactionEditorEvent.NoteChanged(it)) },
            label = { Text(stringResource(R.string.editor_note)) },
            maxLines = 3,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = FiscusLayout.ScreenMargin),
        )
    }
}

/** Single-select connected button group (M3 Expressive "connected" variant); shapes morph on selection. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class) // connected button shapes
@Composable
private fun TypeSelector(
    selected: TransactionType,
    onSelect: (TransactionType) -> Unit,
    modifier: Modifier = Modifier,
) {
    val types = TransactionTypeDisplayOrder
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
    ) {
        types.forEachIndexed { index, type ->
            ToggleButton(
                checked = type == selected,
                onCheckedChange = { onSelect(type) },
                shapes = when (index) {
                    0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                    types.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                    else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                },
                modifier = Modifier
                    .weight(1f)
                    .semantics { role = Role.RadioButton },
            ) {
                Icon(painterResource(type.iconRes), contentDescription = null)
                Spacer(Modifier.size(ToggleButtonDefaults.IconSpacing))
                Text(stringResource(type.labelRes), maxLines = 1)
            }
        }
    }
}

/**
 * The hero (ADR-010, hero moment 2): a type header with a [ShapeAccentIcon], the amount
 * centered in emphasized display type, and one line below that shows either the formatted amount
 * with its account or the error. The container role follows the type (expense → tertiary,
 * income → secondary, transfer → surface/primary). Colors blend on an effects spring; the header
 * and summary swap with spatial/effects springs. No text-field chrome: the hero *is* the field.
 */
@Composable
private fun AmountHero(
    uiState: TransactionEditorUiState,
    onAmountChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val motion = MaterialTheme.motionScheme
    val colors = MaterialTheme.colorScheme
    val roles = when (uiState.type) {
        TransactionType.EXPENSE -> HeroRoles(
            colors.tertiaryContainer, colors.onTertiaryContainer, colors.tertiary, colors.onTertiary,
        )
        TransactionType.INCOME -> HeroRoles(
            colors.secondaryContainer, colors.onSecondaryContainer, colors.secondary, colors.onSecondary,
        )
        TransactionType.TRANSFER -> HeroRoles(
            colors.surfaceContainerHighest, colors.onSurface, colors.primary, colors.onPrimary,
        )
    }
    val effects = motion.defaultEffectsSpec<Color>()
    val container by animateColorAsState(roles.container, effects, label = "heroContainer")
    val content by animateColorAsState(roles.onContainer, effects, label = "heroContent")
    val accent by animateColorAsState(roles.accent, effects, label = "heroAccent")
    val onAccent by animateColorAsState(roles.onAccent, effects, label = "heroOnAccent")

    val focusRequester = remember { FocusRequester() }
    val account = uiState.selectedAccount
    val symbol = account?.let { Currency.getInstance(it.currencyCode).symbol }.orEmpty()
    val amountStyle = MaterialTheme.typography.displayLargeEmphasized.tabularNumbers
        .copy(textAlign = TextAlign.Center)
    val amountDescription = stringResource(R.string.editor_amount)

    Card(
        // Hero moment: remapped to a larger step of the shape scale (M3_EXPRESSIVE.md §3).
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = container, contentColor = content),
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(FiscusLayout.ScreenMargin),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(FiscusLayout.ItemSpacing),
        ) {
            AnimatedContent(
                targetState = uiState.type,
                transitionSpec = {
                    (slideInVertically(motion.fastSpatialSpec()) { it / 2 } + fadeIn(motion.fastEffectsSpec()))
                        .togetherWith(slideOutVertically(motion.fastSpatialSpec()) { -it / 2 } + fadeOut(motion.fastEffectsSpec()))
                },
                label = "heroType",
            ) { type ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(FiscusLayout.ItemSpacing),
                ) {
                    ShapeAccentIcon(icon = type.iconRes, containerColor = accent, contentColor = onAccent)
                    Text(text = stringResource(type.labelRes), style = MaterialTheme.typography.titleMedium)
                }
            }
            TextField(
                value = uiState.amountText,
                onValueChange = onAmountChange,
                visualTransformation = CurrencyPrefixTransformation(symbol),
                textStyle = amountStyle,
                singleLine = true,
                isError = uiState.amountError != null,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Next,
                ),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    errorContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    errorIndicatorColor = Color.Transparent,
                    focusedTextColor = content,
                    unfocusedTextColor = content,
                    errorTextColor = content,
                    cursorColor = content,
                    errorCursorColor = colors.error,
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester)
                    .semantics { contentDescription = amountDescription },
            )
            HeroSummary(
                uiState = uiState,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
    if (uiState.isNew) {
        LaunchedEffect(Unit) { focusRequester.requestFocus() }
    }
}

/** The four color roles of the hero: container pair and accent pair, used only as M3 pairs. */
private data class HeroRoles(val container: Color, val onContainer: Color, val accent: Color, val onAccent: Color)

/**
 * One line under the amount: the error when the amount is invalid, otherwise the formatted amount
 * with its account ("₹1,250.50 · from Cash"). Swaps with an effects spring.
 */
@Composable
private fun HeroSummary(uiState: TransactionEditorUiState, modifier: Modifier = Modifier) {
    val motion = MaterialTheme.motionScheme
    val account = uiState.selectedAccount
    val error = uiState.amountError?.let { stringResource(it) }
    val summary = account?.let { parseMoneyInput(uiState.amountText, it.currencyCode) }?.let { money ->
        stringResource(
            if (uiState.type == TransactionType.INCOME) R.string.editor_hero_summary_to else R.string.editor_hero_summary_from,
            formatMoney(money),
            account.name,
        )
    }
    // Empty field: prompt in the card's own text colour (no placeholder: M3's placeholder role
    // fails contrast on these containers, see DESIGN_SYSTEM.md "Contrast check").
    val prompt = if (uiState.amountText.isBlank()) stringResource(R.string.editor_hint_amount) else null
    AnimatedContent(
        targetState = error ?: summary ?: prompt.orEmpty(),
        transitionSpec = { fadeIn(motion.fastEffectsSpec()) togetherWith fadeOut(motion.fastEffectsSpec()) },
        label = "heroSummary",
        modifier = modifier.semantics { liveRegion = LiveRegionMode.Polite },
    ) { line ->
        Text(
            text = line,
            style = MaterialTheme.typography.bodyMedium.tabularNumbers,
            color = if (error != null && line == error) MaterialTheme.colorScheme.error else LocalContentColor.current,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

/**
 * Says why Save is disabled ("Choose a category"). Enters and exits by expanding and collapsing
 * vertically (M3 enter/exit pattern: no scale) on the theme's springs; text changes cross-fade.
 */
@Composable
private fun SaveHint(@StringRes hint: Int?) {
    val motion = MaterialTheme.motionScheme
    AnimatedVisibility(
        visible = hint != null,
        enter = expandVertically(motion.defaultSpatialSpec()) + fadeIn(motion.defaultEffectsSpec()),
        exit = shrinkVertically(motion.fastSpatialSpec()) + fadeOut(motion.fastEffectsSpec()),
    ) {
        // Keep the last hint while collapsing, so the text doesn't vanish before the space does.
        var lastHint by remember { mutableIntStateOf(0) }
        if (hint != null) lastHint = hint
        AnimatedContent(
            targetState = lastHint,
            transitionSpec = { fadeIn(motion.fastEffectsSpec()) togetherWith fadeOut(motion.fastEffectsSpec()) },
            label = "saveHint",
        ) { res ->
            if (res != 0) {
                Text(
                    text = stringResource(res),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = FiscusLayout.ItemSpacing)
                        .semantics { liveRegion = LiveRegionMode.Polite },
                )
            }
        }
    }
}

/**
 * Large primary action. Its shape morphs on press (built into the Expressive button) and its
 * content swaps to a [LoadingIndicator] while saving.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class) // button size helpers, LoadingIndicator
@Composable
private fun SaveButton(
    enabled: Boolean,
    isSaving: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val motion = MaterialTheme.motionScheme
    val height = ButtonDefaults.MediumContainerHeight
    Button(
        onClick = onClick,
        shapes = ButtonDefaults.shapesFor(height),
        enabled = enabled && !isSaving,
        contentPadding = ButtonDefaults.contentPaddingFor(height),
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = height),
    ) {
        AnimatedContent(
            targetState = isSaving,
            transitionSpec = {
                // Cross-fade only: M3 enter/exit patterns avoid scale (it implies elevation change).
                fadeIn(motion.fastEffectsSpec()) togetherWith fadeOut(motion.fastEffectsSpec())
            },
            label = "saveContent",
        ) { saving ->
            if (saving) {
                LoadingIndicator(modifier = Modifier.size(ButtonDefaults.iconSizeFor(height)))
            } else {
                Text(stringResource(R.string.editor_save), style = ButtonDefaults.textStyleFor(height))
            }
        }
    }
}

private val PreviewAccounts = listOf(
    Account(1, "Cash", AccountType.CASH, "INR", Money(0, "INR")),
    Account(2, "Bank", AccountType.BANK, "INR", Money(0, "INR")),
)
private val PreviewCategories = listOf(
    Category(1, "Food & dining", CategoryKind.EXPENSE, "restaurant"),
    Category(2, "Groceries", CategoryKind.EXPENSE, "local_grocery_store"),
    Category(3, "Transport", CategoryKind.EXPENSE, "directions_car"),
    Category(4, "Bills & utilities", CategoryKind.EXPENSE, "receipt_long"),
)

@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "Large font", showBackground = true, fontScale = 2f)
@Composable
private fun TransactionEditorScreenAddPreview() {
    FiscusTheme(dynamicColor = false) {
        TransactionEditorScreen(
            uiState = TransactionEditorUiState(
                isLoading = false,
                amountText = "1250",
                accounts = PreviewAccounts,
                accountId = 1,
                categories = PreviewCategories,
                categoryId = 1,
                date = LocalDate(2026, 11, 15),
            ),
            onEvent = {},
            onBack = {},
        )
    }
}

@Preview(name = "Edit transfer", showBackground = true)
@Composable
private fun TransactionEditorScreenEditPreview() {
    FiscusTheme(dynamicColor = false) {
        TransactionEditorScreen(
            uiState = TransactionEditorUiState(
                isLoading = false,
                isNew = false,
                type = TransactionType.TRANSFER,
                amountText = "2500.00",
                accounts = PreviewAccounts,
                accountId = 1,
                toAccountId = 2,
                date = LocalDate(2026, 11, 15),
                note = "Rent share",
            ),
            onEvent = {},
            onBack = {},
        )
    }
}
