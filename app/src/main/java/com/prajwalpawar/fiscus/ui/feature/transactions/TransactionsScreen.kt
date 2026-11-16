package com.prajwalpawar.fiscus.ui.feature.transactions

import android.content.res.Configuration
import androidx.annotation.DrawableRes
import androidx.annotation.PluralsRes
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ExpandedFullScreenSearchBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SearchBarState
import androidx.compose.material3.SearchBarValue
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.SheetValue
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.material3.rememberSearchBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import com.prajwalpawar.fiscus.R
import com.prajwalpawar.fiscus.core.money.formatMoney
import com.prajwalpawar.fiscus.model.Account
import com.prajwalpawar.fiscus.model.AccountType
import com.prajwalpawar.fiscus.model.Category
import com.prajwalpawar.fiscus.model.CategoryKind
import com.prajwalpawar.fiscus.model.Money
import com.prajwalpawar.fiscus.model.PeriodSummary
import com.prajwalpawar.fiscus.model.Transaction
import com.prajwalpawar.fiscus.model.TransactionDaySection
import com.prajwalpawar.fiscus.model.TransactionListEntry
import com.prajwalpawar.fiscus.model.TransactionType
import com.prajwalpawar.fiscus.ui.components.ActionButtonGroup
import com.prajwalpawar.fiscus.ui.components.ActionStyle
import com.prajwalpawar.fiscus.ui.components.AddTransactionFabMenu
import com.prajwalpawar.fiscus.ui.components.AmountText
import com.prajwalpawar.fiscus.ui.components.DayHeader
import com.prajwalpawar.fiscus.ui.components.EmptyState
import com.prajwalpawar.fiscus.ui.components.GroupAction
import com.prajwalpawar.fiscus.ui.components.LocalSnackbarHostState
import com.prajwalpawar.fiscus.ui.components.PeriodTotal
import com.prajwalpawar.fiscus.ui.components.ShapeAccentIcon
import com.prajwalpawar.fiscus.ui.components.TransactionListItem
import com.prajwalpawar.fiscus.ui.components.TransactionTypeDisplayOrder
import com.prajwalpawar.fiscus.ui.components.accentRoles
import com.prajwalpawar.fiscus.ui.components.categoryIconRes
import com.prajwalpawar.fiscus.ui.components.dayLabel
import com.prajwalpawar.fiscus.ui.components.iconRes
import com.prajwalpawar.fiscus.ui.components.labelRes
import com.prajwalpawar.fiscus.ui.components.netAmountText
import com.prajwalpawar.fiscus.ui.components.netIconRes
import com.prajwalpawar.fiscus.ui.components.sharedContainer
import com.prajwalpawar.fiscus.ui.components.signedNetText
import com.prajwalpawar.fiscus.ui.components.transactionContainerKey
import com.prajwalpawar.fiscus.ui.components.transactionSupportingText
import com.prajwalpawar.fiscus.ui.theme.FiscusLayout
import com.prajwalpawar.fiscus.ui.theme.FiscusTheme
import com.prajwalpawar.fiscus.ui.theme.tabularNumbers
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import kotlin.time.Instant
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateRange
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toLocalDateTime

/**
 * Stateless Transactions screen (ADR-022). Standard M3, not a hero moment (ADR-010): a collapsed
 * [SearchBar] that expands into [ExpandedFullScreenSearchBar], [FilterChip] filters, and one
 * group of [SegmentedListItem]s per day. All state comes from [uiState]; actions go to [onEvent].
 */
// Search, sheets and pickers are experimental; LoadingIndicator is Expressive-experimental.
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TransactionsScreen(
    uiState: TransactionsUiState,
    onEvent: (TransactionsEvent) -> Unit,
    onOpenSettings: () -> Unit,
    onAddTransaction: (TransactionType) -> Unit,
    onOpenTransaction: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val motion = MaterialTheme.motionScheme
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val searchBarState = rememberSearchBarState()
    val queryState = rememberTextFieldState()
    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeFlexibleTopAppBar(
                title = { Text(stringResource(R.string.nav_transactions)) },
                actions = {
                    IconButton(shapes = IconButtonDefaults.shapes(), onClick = onOpenSettings) {
                        Icon(
                            painter = painterResource(R.drawable.ic_settings_rounded),
                            contentDescription = stringResource(R.string.nav_settings),
                        )
                    }
                },
                scrollBehavior = scrollBehavior,
            )
        },
        snackbarHost = { SnackbarHost(LocalSnackbarHostState.current) },
        floatingActionButton = { AddTransactionFabMenu(onAddTransaction = onAddTransaction) },
    ) { innerPadding ->
        AnimatedContent(
            targetState = uiState.isLoading,
            transitionSpec = {
                fadeIn(motion.defaultEffectsSpec())
                    .togetherWith(fadeOut(motion.fastEffectsSpec()))
                    .using(SizeTransform { _, _ -> motion.defaultSpatialSpec() })
            },
            label = "transactionsLoading",
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
                    LazyColumn(
                        modifier = Modifier
                            .widthIn(max = FiscusLayout.MaxContentWidth)
                            .fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = FiscusLayout.ScreenMargin,
                            end = FiscusLayout.ScreenMargin,
                            bottom = FiscusLayout.SectionSpacing,
                        ),
                        verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap),
                    ) {
                        item(key = "search") {
                            TransactionSearchBar(
                                searchBarState = searchBarState,
                                queryState = queryState,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                        item(key = "filters") {
                            FilterChipRow(uiState = uiState, onEvent = onEvent)
                        }
                        uiState.summary?.let { summary ->
                            item(key = "summary") {
                                SummaryCard(
                                    summary = summary,
                                    period = uiState.dateRange?.let { dateRangeLabel(it) }
                                        ?: stringResource(R.string.transactions_this_month),
                                    modifier = Modifier.animateItem(
                                        fadeInSpec = motion.defaultEffectsSpec(),
                                        placementSpec = motion.defaultSpatialSpec(),
                                        fadeOutSpec = motion.fastEffectsSpec(),
                                    ),
                                )
                            }
                        }
                        if (uiState.sections.isEmpty()) {
                            item(key = "empty") {
                                EmptyMessage(
                                    isFiltered = uiState.isFilteredEmpty,
                                    onClearFilters = { onEvent(TransactionsEvent.FiltersCleared) },
                                    // Centered in the visible list area (ADR-031).
                                    modifier = Modifier.fillParentMaxSize(),
                                )
                            }
                        } else {
                            transactionSections(uiState, onOpenTransaction = onOpenTransaction)
                        }
                    }
                }
            }
        }
    }

    SearchResults(
        uiState = uiState,
        searchBarState = searchBarState,
        queryState = queryState,
        onEvent = onEvent,
        onOpenTransaction = onOpenTransaction,
    )
    FilterPickers(uiState = uiState, onEvent = onEvent)
}

/** The collapsed search bar; tapping it expands [SearchResults] out of it. */
@OptIn(ExperimentalMaterial3Api::class) // SearchBar
@Composable
private fun TransactionSearchBar(
    searchBarState: SearchBarState,
    queryState: TextFieldState,
    modifier: Modifier = Modifier,
) {
    SearchBar(
        state = searchBarState,
        inputField = { SearchInputField(searchBarState, queryState) },
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class) // SearchBarDefaults.InputField
@Composable
private fun SearchInputField(searchBarState: SearchBarState, queryState: TextFieldState) {
    val scope = rememberCoroutineScope()
    val isExpanded = searchBarState.currentValue == SearchBarValue.Expanded
    SearchBarDefaults.InputField(
        textFieldState = queryState,
        searchBarState = searchBarState,
        onSearch = {},
        placeholder = { Text(stringResource(R.string.transactions_search_placeholder)) },
        leadingIcon = {
            if (isExpanded) {
                IconButton(shapes = IconButtonDefaults.shapes(), onClick = { scope.launch { searchBarState.animateToCollapsed() } }) {
                    Icon(
                        painter = painterResource(R.drawable.ic_arrow_back_rounded),
                        contentDescription = stringResource(R.string.action_navigate_back),
                    )
                }
            } else {
                Icon(painterResource(R.drawable.ic_search_rounded), contentDescription = null)
            }
        },
        trailingIcon = {
            if (isExpanded && queryState.text.isNotEmpty()) {
                IconButton(shapes = IconButtonDefaults.shapes(), onClick = { queryState.clearText() }) {
                    Icon(
                        painter = painterResource(R.drawable.ic_close_rounded),
                        contentDescription = stringResource(R.string.action_clear_search),
                    )
                }
            }
        },
    )
}

/**
 * Full-screen search (M3 search pattern). The query is sent to the ViewModel as it's typed and
 * combined with the chip filters; closing search clears it.
 */
@OptIn(ExperimentalMaterial3Api::class) // ExpandedFullScreenSearchBar
@Composable
private fun SearchResults(
    uiState: TransactionsUiState,
    searchBarState: SearchBarState,
    queryState: TextFieldState,
    onEvent: (TransactionsEvent) -> Unit,
    onOpenTransaction: (Long) -> Unit,
) {
    val scope = rememberCoroutineScope()
    LaunchedEffect(queryState) {
        snapshotFlow { queryState.text.toString() }
            .collect { onEvent(TransactionsEvent.QueryChanged(it)) }
    }
    LaunchedEffect(searchBarState.currentValue) {
        if (searchBarState.currentValue == SearchBarValue.Collapsed) queryState.clearText()
    }
    ExpandedFullScreenSearchBar(
        state = searchBarState,
        inputField = { SearchInputField(searchBarState, queryState) },
    ) {
        LazyColumn(
            contentPadding = PaddingValues(
                start = FiscusLayout.ScreenMargin,
                end = FiscusLayout.ScreenMargin,
                bottom = FiscusLayout.SectionSpacing,
            ),
            verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap),
        ) {
            when {
                // Nothing typed yet: a hint, not the whole list (ADR-024).
                !uiState.isSearching -> item(key = "hint") {
                    EmptyState(
                        icon = R.drawable.ic_search_rounded,
                        message = stringResource(R.string.transactions_search_hint),
                        modifier = Modifier.fillParentMaxSize(),
                    )
                }
                uiState.sections.isEmpty() -> item(key = "noResults") {
                    EmptyState(
                        icon = R.drawable.ic_search_rounded,
                        message = stringResource(R.string.transactions_search_empty),
                        modifier = Modifier.fillParentMaxSize(),
                    )
                }
                else -> item(key = "count") {
                    Text(
                        text = pluralStringResource(
                            R.plurals.transactions_search_results,
                            uiState.resultCount,
                            uiState.resultCount,
                        ),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .padding(horizontal = FiscusLayout.ScreenMargin)
                            .semantics { liveRegion = LiveRegionMode.Polite },
                    )
                }
            }
            if (uiState.isSearching) {
                transactionSections(uiState, shareContainers = false) { id ->
                    // The expanded bar is a dialog above the app: close it before navigating, or
                    // it would cover the detail screen.
                    scope.launch {
                        searchBarState.snapTo(0f)
                        onOpenTransaction(id)
                    }
                }
            }
        }
    }
}

/** Type chips toggle in place; Account, Category and Date open a picker. */
@Composable
private fun FilterChipRow(uiState: TransactionsUiState, onEvent: (TransactionsEvent) -> Unit) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(FiscusLayout.ItemSpacing),
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(vertical = FiscusLayout.ItemSpacing),
    ) {
        PickerChip(
            label = selectionLabel(
                selected = TransactionTypeDisplayOrder
                    .filter { it in uiState.selectedTypes }
                    .map { stringResource(it.labelRes) },
                none = R.string.transactions_filter_type,
                plural = R.plurals.transactions_filter_types,
            ),
            selected = uiState.selectedTypes.isNotEmpty(),
            onClick = { onEvent(TransactionsEvent.PickerOpened(FilterPicker.TYPE)) },
        )
        PickerChip(
            label = selectionLabel(
                selected = uiState.accounts
                    .filter { it.id in uiState.selectedAccountIds }
                    .map { it.name },
                none = R.string.transactions_filter_account,
                plural = R.plurals.transactions_filter_accounts,
            ),
            selected = uiState.selectedAccountIds.isNotEmpty(),
            onClick = { onEvent(TransactionsEvent.PickerOpened(FilterPicker.ACCOUNT)) },
        )
        PickerChip(
            label = selectionLabel(
                selected = uiState.categories
                    .filter { it.id in uiState.selectedCategoryIds }
                    .map { it.name },
                none = R.string.transactions_filter_category,
                plural = R.plurals.transactions_filter_categories,
            ),
            selected = uiState.selectedCategoryIds.isNotEmpty(),
            onClick = { onEvent(TransactionsEvent.PickerOpened(FilterPicker.CATEGORY)) },
        )
        PickerChip(
            label = uiState.dateRange?.let { dateRangeLabel(it) }
                ?: stringResource(R.string.transactions_this_month),
            selected = uiState.dateRange != null,
            onClick = { onEvent(TransactionsEvent.PickerOpened(FilterPicker.DATE)) },
        )
        if (uiState.hasChipFilters) {
            TextButton(shapes = ButtonDefaults.shapes(), onClick = { onEvent(TransactionsEvent.FiltersCleared) }) {
                Text(stringResource(R.string.transactions_filter_clear))
            }
        }
    }
}

/** A filter chip that opens a picker: M3 shows a trailing drop-down arrow for these. */
@Composable
private fun PickerChip(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        shapes = FilterChipDefaults.shapes(),
        leadingIcon = if (selected) {
            { CheckIcon() }
        } else {
            null
        },
        trailingIcon = {
            Icon(
                painter = painterResource(R.drawable.ic_arrow_drop_down_rounded),
                contentDescription = null,
            )
        },
    )
}

@Composable
private fun CheckIcon() {
    Icon(
        painter = painterResource(R.drawable.ic_check_rounded),
        contentDescription = null,
    )
}

/** "Account" with nothing picked, the name for one, "2 accounts" for more. */
@Composable
private fun selectionLabel(
    selected: List<String>,
    @StringRes none: Int,
    @PluralsRes plural: Int,
): String = when (selected.size) {
    0 -> stringResource(none)
    1 -> selected.single()
    else -> pluralStringResource(plural, selected.size, selected.size)
}

@Composable
private fun dateRangeLabel(range: LocalDateRange): String {
    val formatter = remember { DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM) }
    val start = formatter.format(range.start.toJavaLocalDate())
    val end = formatter.format(range.endInclusive.toJavaLocalDate())
    return if (range.start == range.endInclusive) {
        start
    } else {
        stringResource(R.string.date_range, start, end)
    }
}

/** One header plus a group of joined [TransactionListItem]s per day. */
private fun LazyListScope.transactionSections(
    uiState: TransactionsUiState,
    // Search results live in a dialog window, outside the shared-transition layout.
    shareContainers: Boolean = true,
    onOpenTransaction: (Long) -> Unit,
) {
    val today = uiState.today ?: return
    uiState.sections.forEach { section ->
        item(key = "day-${section.date}", contentType = "dayHeader") {
            DayHeader(date = section.date, today = today, net = section.net)
        }
        itemsIndexed(
            items = section.items,
            key = { _, entry -> entry.transaction.id },
            contentType = { _, _ -> "transaction" },
        ) { index, entry ->
            val motion = MaterialTheme.motionScheme
            TransactionListItem(
                transaction = entry.transaction,
                supportingText = transactionSupportingText(entry),
                icon = entry.category?.let { categoryIconRes(it.iconKey) }
                    ?: entry.transaction.type.iconRes,
                onClick = { onOpenTransaction(entry.transaction.id) },
                shapes = ListItemDefaults.segmentedShapes(index, section.items.size),
                highlight = uiState.query,
                // Rows move and fade on the theme's springs as filters change (M3_EXPRESSIVE §5).
                modifier = Modifier
                    .animateItem(
                        fadeInSpec = motion.defaultEffectsSpec(),
                        placementSpec = motion.defaultSpatialSpec(),
                        fadeOutSpec = motion.fastEffectsSpec(),
                    )
                    .then(
                        if (shareContainers) {
                            // Grows into the detail page and shrinks back on Back (ADR-028).
                            Modifier.sharedContainer(transactionContainerKey(entry.transaction.id))
                        } else {
                            Modifier
                        },
                    ),
            )
        }
    }
}

/**
 * Totals for what the list shows (ADR-023): the net leads in emphasized headline type, income and
 * expense follow with their type accents. Contained on a surface-container tier, default
 * elevation; calm data, not a hero moment (ADR-010).
 */
@Composable
private fun SummaryCard(summary: PeriodSummary, period: String, modifier: Modifier = Modifier) {
    val netText = signedNetText(summary.net)
    val netLabel = stringResource(R.string.transactions_summary_net)
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = FiscusLayout.ItemSpacing),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        ),
    ) {
        Column(
            modifier = Modifier.padding(FiscusLayout.ScreenMargin),
            verticalArrangement = Arrangement.spacedBy(FiscusLayout.TightSpacing),
        ) {
            Text(
                text = period,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            AmountText(
                text = netAmountText(summary.net),
                style = MaterialTheme.typography.headlineMediumEmphasized,
                modifier = Modifier.clearAndSetSemantics {
                    contentDescription = "$netLabel, $netText"
                },
            )
            Row(
                modifier = Modifier.padding(top = FiscusLayout.ItemSpacing),
                horizontalArrangement = Arrangement.spacedBy(FiscusLayout.ScreenMargin),
            ) {
                PeriodTotal(TransactionType.INCOME, summary.income, Modifier.weight(1f))
                PeriodTotal(TransactionType.EXPENSE, summary.expense, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun EmptyMessage(
    isFiltered: Boolean,
    onClearFilters: () -> Unit,
    modifier: Modifier = Modifier,
) {
    EmptyState(
        modifier = modifier,
        icon = if (isFiltered) R.drawable.ic_search_rounded else R.drawable.ic_receipt_long_rounded,
        message = stringResource(
            if (isFiltered) {
                R.string.transactions_filtered_empty
            } else {
                R.string.transactions_empty_month
            },
        ),
        action = if (isFiltered) {
            {
                TextButton(shapes = ButtonDefaults.shapes(), onClick = onClearFilters) {
                    Text(stringResource(R.string.transactions_filter_clear))
                }
            }
        } else {
            null
        },
    )
}


/** The open picker: a sheet for accounts or categories, a dialog for the date range. */
@OptIn(ExperimentalMaterial3Api::class) // ModalBottomSheet
@Composable
private fun FilterPickers(uiState: TransactionsUiState, onEvent: (TransactionsEvent) -> Unit) {
    val dismiss = { onEvent(TransactionsEvent.PickerDismissed) }
    val picker = uiState.openPicker ?: return
    if (picker == FilterPicker.DATE) {
        DateRangeDialog(
            range = uiState.dateRange,
            onConfirm = { onEvent(TransactionsEvent.DateRangeChanged(it)) },
            onDismiss = dismiss,
        )
        return
    }
    // Open fully expanded: from the half-height state, a swipe that first drags the sheet open
    // and then scrolls the list left the rows ignoring taps (reproduced in
    // TransactionsFilterFlowTest; ADR-026).
    val sheetState = rememberBottomSheetState(
        initialValue = SheetValue.Hidden,
        enabledValues = setOf(SheetValue.Hidden, SheetValue.Expanded),
    )
    ModalBottomSheet(onDismissRequest = dismiss, sheetState = sheetState) {
        val groups = when (picker) {
            FilterPicker.TYPE -> listOf(
                TransactionTypeDisplayOrder.map { type ->
                    CheckableOption(
                        key = type,
                        name = stringResource(type.labelRes),
                        icon = type.iconRes,
                        checked = type in uiState.selectedTypes,
                        onToggle = { onEvent(TransactionsEvent.TypeToggled(type)) },
                    )
                },
            )
            FilterPicker.ACCOUNT -> listOf(
                uiState.accounts.map { account ->
                    CheckableOption(
                        key = account.id,
                        name = account.name,
                        icon = account.type.iconRes,
                        checked = account.id in uiState.selectedAccountIds,
                        onToggle = { onEvent(TransactionsEvent.AccountToggled(account.id)) },
                    )
                },
            )
            else -> listOf(CategoryKind.EXPENSE, CategoryKind.INCOME).map { kind ->
                uiState.categories.filter { it.kind == kind }.map { category ->
                    CheckableOption(
                        key = category.id,
                        name = category.name,
                        icon = categoryIconRes(category.iconKey),
                        checked = category.id in uiState.selectedCategoryIds,
                        onToggle = { onEvent(TransactionsEvent.CategoryToggled(category.id)) },
                    )
                }
            }.filter { it.isNotEmpty() }
        }
        CheckableSheetContent(
            groups = groups,
            onClear = { onEvent(TransactionsEvent.FilterCleared(picker)) },
            onDone = dismiss,
        )
    }
}

/** One row of a filter sheet. Toggling applies right away; the list updates behind the sheet. */
private data class CheckableOption(
    val key: Any,
    val name: String,
    @DrawableRes val icon: Int?,
    val checked: Boolean,
    val onToggle: () -> Unit,
)

/**
 * A filter sheet: checkable [SegmentedListItem]s (one joined group per entry in [groups]) and a
 * footer [ButtonGroup] with Clear and Done.
 */
@Composable
private fun CheckableSheetContent(
    groups: List<List<CheckableOption>>,
    onClear: () -> Unit,
    onDone: () -> Unit,
) {
    Column(modifier = Modifier.padding(bottom = FiscusLayout.ScreenMargin)) {
        LazyColumn(
            modifier = Modifier.weight(1f, fill = false),
            contentPadding = PaddingValues(horizontal = FiscusLayout.ScreenMargin),
            verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap),
        ) {
            groups.forEachIndexed { groupIndex, options ->
                if (groupIndex > 0) {
                    item(key = "gap-$groupIndex") {
                        Spacer(Modifier.height(FiscusLayout.ItemSpacing))
                    }
                }
                itemsIndexed(options, key = { _, option -> option.key }) { index, option ->
                    SegmentedListItem(
                        checked = option.checked,
                        onCheckedChange = { option.onToggle() },
                        shapes = ListItemDefaults.segmentedShapes(index, options.size),
                        leadingContent = option.icon?.let { icon ->
                            { Icon(painterResource(icon), contentDescription = null) }
                        },
                        trailingContent = if (option.checked) {
                            { CheckIcon() }
                        } else {
                            null
                        },
                    ) {
                        Text(option.name)
                    }
                }
            }
        }
        SheetActions(
            onClear = onClear,
            clearEnabled = groups.any { group -> group.any { it.checked } },
            onDone = onDone,
            modifier = Modifier.padding(
                start = FiscusLayout.ScreenMargin,
                end = FiscusLayout.ScreenMargin,
                top = FiscusLayout.SectionSpacing,
            ),
        )
    }
}

/** Clear and Done in the shared [ActionButtonGroup], like every page's actions (ADR-028). */
@Composable
private fun SheetActions(
    onClear: () -> Unit,
    clearEnabled: Boolean,
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ActionButtonGroup(
        actions = listOf(
            GroupAction(
                label = stringResource(R.string.action_clear),
                onClick = onClear,
                style = ActionStyle.Secondary,
                enabled = clearEnabled,
            ),
            GroupAction(label = stringResource(R.string.action_done), onClick = onDone),
        ),
        modifier = modifier,
    )
}

/**
 * M3 date range picker in a dialog. [DateRangePicker] works in UTC-midnight millis; conversion to
 * local days happens here, like `DockedDatePickerField`.
 */
@OptIn(ExperimentalMaterial3Api::class) // DateRangePicker state
@Composable
private fun DateRangeDialog(
    range: LocalDateRange?,
    onConfirm: (LocalDateRange?) -> Unit,
    onDismiss: () -> Unit,
) {
    val state = rememberDateRangePickerState(
        initialSelectedStartDateMillis = range?.start?.let(::utcMillis),
        initialSelectedEndDateMillis = range?.endInclusive?.let(::utcMillis),
    )
    val start = state.selectedStartDateMillis
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                shapes = ButtonDefaults.shapes(),
                enabled = start != null,
                onClick = {
                    val startDate = start?.let(::utcDate) ?: return@TextButton
                    val endDate = state.selectedEndDateMillis?.let(::utcDate) ?: startDate
                    onConfirm(startDate..endDate)
                },
            ) {
                Text(stringResource(R.string.action_apply))
            }
        },
        dismissButton = {
            if (range != null) {
                TextButton(shapes = ButtonDefaults.shapes(), onClick = { onConfirm(null) }) {
                    Text(stringResource(R.string.transactions_filter_clear))
                }
            } else {
                TextButton(shapes = ButtonDefaults.shapes(), onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) }
            }
        },
    ) {
        DateRangePicker(state = state, modifier = Modifier.weight(1f))
    }
}

private fun utcDate(millis: Long): LocalDate =
    Instant.fromEpochMilliseconds(millis).toLocalDateTime(TimeZone.UTC).date

private fun utcMillis(date: LocalDate): Long =
    date.atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds()

private val PreviewNow = Instant.parse("2026-11-15T10:30:00Z")
private val PreviewCash = Account(
    id = 1,
    name = "Cash",
    type = AccountType.CASH,
    currencyCode = "INR",
    openingBalance = Money(0, "INR"),
)
private val PreviewBank = PreviewCash.copy(id = 2, name = "Bank", type = AccountType.BANK)
private val PreviewFood =
    Category(id = 1, name = "Food & dining", kind = CategoryKind.EXPENSE, iconKey = "restaurant")

private fun previewTransaction(
    id: Long,
    title: String,
    type: TransactionType,
    amountMinor: Long,
) = Transaction(
    id = id,
    type = type,
    title = title,
    amount = Money(amountMinor, "INR"),
    accountId = PreviewCash.id,
    toAccountId = if (type == TransactionType.TRANSFER) PreviewBank.id else null,
    categoryId = if (type == TransactionType.TRANSFER) null else PreviewFood.id,
    occurredAt = PreviewNow,
    createdAt = PreviewNow,
    updatedAt = PreviewNow,
)

private val PreviewState = TransactionsUiState(
    isLoading = false,
    today = LocalDate(2026, 11, 15),
    sections = listOf(
        TransactionDaySection(
            date = LocalDate(2026, 11, 15),
            items = listOf(
                TransactionListEntry(
                    previewTransaction(1, "Lunch with team", TransactionType.EXPENSE, 45_000),
                    PreviewCash,
                    null,
                    PreviewFood,
                ),
                TransactionListEntry(
                    previewTransaction(2, "Dinner", TransactionType.EXPENSE, 100_000),
                    PreviewCash,
                    null,
                    PreviewFood,
                ),
            ),
            net = Money(-145_000, "INR"),
        ),
        TransactionDaySection(
            date = LocalDate(2026, 11, 14),
            items = listOf(
                TransactionListEntry(
                    previewTransaction(3, "Savings", TransactionType.TRANSFER, 500_000),
                    PreviewCash,
                    PreviewBank,
                    null,
                ),
            ),
            net = null,
        ),
    ),
    selectedTypes = setOf(TransactionType.EXPENSE),
    summary = PeriodSummary(income = Money(0, "INR"), expense = Money(145_000, "INR")),
    accounts = listOf(PreviewCash, PreviewBank),
    categories = listOf(PreviewFood),
)

@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "Large font", showBackground = true, fontScale = 2f)
@Composable
private fun TransactionsScreenPreview() {
    FiscusTheme(dynamicColor = false) {
        TransactionsScreen(
            uiState = PreviewState,
            onEvent = {},
            onOpenSettings = {},
            onAddTransaction = {},
            onOpenTransaction = {},
        )
    }
}

@Preview(name = "Filtered empty, light", showBackground = true)
@Preview(
    name = "Filtered empty, dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Preview(name = "Filtered empty, large font", showBackground = true, fontScale = 2f)
@Composable
private fun TransactionsScreenFilteredEmptyPreview() {
    FiscusTheme(dynamicColor = false) {
        TransactionsScreen(
            uiState = PreviewState.copy(sections = emptyList(), summary = null),
            onEvent = {},
            onOpenSettings = {},
            onAddTransaction = {},
            onOpenTransaction = {},
        )
    }
}
