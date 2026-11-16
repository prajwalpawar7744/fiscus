package com.prajwalpawar.fiscus.ui.feature.home

import android.content.res.Configuration
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.WavyProgressIndicatorDefaults
import androidx.compose.material3.carousel.HorizontalUncontainedCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.prajwalpawar.fiscus.R
import com.prajwalpawar.fiscus.core.money.formatMoney
import com.prajwalpawar.fiscus.model.Account
import com.prajwalpawar.fiscus.model.AccountType
import com.prajwalpawar.fiscus.model.AccountWithBalance
import com.prajwalpawar.fiscus.model.Category
import com.prajwalpawar.fiscus.model.CategoryKind
import com.prajwalpawar.fiscus.model.Money
import com.prajwalpawar.fiscus.model.PeriodSummary
import com.prajwalpawar.fiscus.model.Transaction
import com.prajwalpawar.fiscus.model.TransactionListEntry
import com.prajwalpawar.fiscus.model.TransactionType
import com.prajwalpawar.fiscus.ui.components.AddTransactionFabMenu
import com.prajwalpawar.fiscus.ui.components.AmountText
import com.prajwalpawar.fiscus.ui.components.EmptyState
import com.prajwalpawar.fiscus.ui.components.LocalSnackbarHostState
import com.prajwalpawar.fiscus.ui.components.PeriodTotal
import com.prajwalpawar.fiscus.ui.components.ShapeAccentIcon
import com.prajwalpawar.fiscus.ui.components.TransactionListItem
import com.prajwalpawar.fiscus.ui.components.accountContainerKey
import com.prajwalpawar.fiscus.ui.components.categoryIconRes
import com.prajwalpawar.fiscus.ui.components.heroRoles
import com.prajwalpawar.fiscus.ui.components.iconRes
import com.prajwalpawar.fiscus.ui.components.netAmountText
import com.prajwalpawar.fiscus.ui.components.sharedContainer
import com.prajwalpawar.fiscus.ui.components.transactionContainerKey
import com.prajwalpawar.fiscus.ui.components.transactionSupportingText
import com.prajwalpawar.fiscus.ui.theme.FiscusLayout
import com.prajwalpawar.fiscus.ui.theme.FiscusTheme
import com.prajwalpawar.fiscus.ui.theme.tabularNumbers
import kotlin.time.Instant

/**
 * Stateless Home screen (ADR-033): the balance hero (hero moment 1, ADR-010), then the accounts
 * and the newest transactions as contained segmented groups. All state comes from [uiState]; all
 * actions go out through lambdas.
 */
@OptIn(
    ExperimentalMaterial3Api::class, // TopAppBarDefaults scroll behavior
    ExperimentalMaterial3ExpressiveApi::class, // LoadingIndicator
)
@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onOpenSettings: () -> Unit,
    onAddTransaction: (TransactionType) -> Unit,
    onOpenTransaction: (Long) -> Unit,
    onOpenAccount: (Long) -> Unit,
    onManageAccounts: () -> Unit,
    onSeeAllTransactions: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val motion = MaterialTheme.motionScheme
    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeFlexibleTopAppBar(
                title = { Text(stringResource(R.string.nav_home)) },
                actions = {
                    IconButton(onClick = onOpenSettings) {
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
            label = "homeLoading",
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
                        item(key = "hero") {
                            BalanceHero(
                                total = uiState.totalBalance,
                                accountCount = uiState.accounts.size,
                                monthSummary = uiState.monthSummary,
                            )
                        }
                        accountsSection(uiState.accounts, onOpenAccount, onManageAccounts)
                        recentSection(uiState.recent, onOpenTransaction, onSeeAllTransactions)
                    }
                }
            }
        }
    }
}

/**
 * Hero moment 1 (ADR-010, ADR-033/034/038): the total balance, left-aligned in
 * `displayLargeEmphasized` on a `primaryContainer` card one shape step above the other heroes
 * (`extraLargeIncreased`), with a small [SpentRing] in the top-end corner (ADR-035/038). This
 * month's income and expense sit below on tiles in each type's hero pair (`shapes.large`: 32dp
 * card − 16dp padding, optical roundness). The spent line in words is the ring's TalkBack text
 * only. TalkBack reads the total as one node.
 */
@Composable
private fun BalanceHero(
    total: Money?,
    accountCount: Int,
    monthSummary: PeriodSummary?,
    modifier: Modifier = Modifier,
) {
    val label = stringResource(R.string.home_balance_label)
    // Spoken signed; shown unsigned after a ↓ when below zero (ADR-032).
    val totalText = total?.let { formatMoney(it) }
        ?: stringResource(R.string.home_balance_mixed)
    val shownTotal = total?.let { netAmountText(it) } ?: totalText
    val accountsLine =
        pluralStringResource(R.plurals.home_balance_accounts, accountCount, accountCount)
    Card(
        // Hero moment 1: one step above the transaction heroes (M3_EXPRESSIVE.md §3).
        shape = MaterialTheme.shapes.extraLargeIncreased,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        ),
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = FiscusLayout.ItemSpacing),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(FiscusLayout.ScreenMargin),
            verticalArrangement = Arrangement.spacedBy(FiscusLayout.ItemSpacing),
        ) {
            // Amount left-aligned; the spent ring in the top-end corner, at the indicator's
            // own default size (ADR-038).
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(FiscusLayout.ItemSpacing),
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(top = FiscusLayout.ItemSpacing)
                        .clearAndSetSemantics {
                            contentDescription = "$label, $totalText, $accountsLine"
                        },
                    verticalArrangement = Arrangement.spacedBy(FiscusLayout.ItemSpacing),
                ) {
                    // One line at any size: big amounts step down instead of wrapping (ADR-027).
                    AmountText(
                        text = shownTotal,
                        style = MaterialTheme.typography.displayLargeEmphasized,
                    )
                    Text(
                        // No arrow on the hero (user, 2026-11-16): below zero says so in words.
                        text = if (total?.isNegative == true) {
                            stringResource(R.string.home_balance_in_debt, accountsLine)
                        } else {
                            accountsLine
                        },
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
                // Only once something was spent this month (ADR-037): no empty 0% ring.
                if (monthSummary != null && !monthSummary.expense.isZero) {
                    SpentRing(spent = spentShare(monthSummary))
                }
            }
            monthSummary?.let { summary ->
                Row(
                    modifier = Modifier.padding(top = FiscusLayout.ItemSpacing),
                    horizontalArrangement = Arrangement.spacedBy(FiscusLayout.ItemSpacing),
                ) {
                    MonthTile(TransactionType.INCOME, summary.income, Modifier.weight(1f))
                    MonthTile(TransactionType.EXPENSE, summary.expense, Modifier.weight(1f))
                }
            }
        }
    }
}

/**
 * One of the hero's month totals: a [PeriodTotal] on a tile in the type's hero pair (income
 * secondaryContainer, expense tertiaryContainer, ADR-024), label in the tile's own on-color.
 */
@Composable
private fun MonthTile(type: TransactionType, amount: Money, modifier: Modifier = Modifier) {
    val roles = type.heroRoles()
    Surface(
        shape = MaterialTheme.shapes.large,
        color = roles.container,
        contentColor = roles.content,
        modifier = modifier,
    ) {
        PeriodTotal(
            type = type,
            amount = amount,
            labelColor = roles.content,
            modifier = Modifier.padding(FiscusLayout.ScreenMargin),
        )
    }
}

/** How much of this month's income was spent: ring progress, the short label in it, the line. */
private data class SpentShare(val fraction: Float, val label: String, val line: String)

/**
 * Expense ÷ income for the hero (ADR-035). Over 100% the ring is full ("100%+"); with expense but
 * no income it's full ("—"); with neither it's empty ("0%"). [SpentShare.line] says it in words,
 * so the ring never carries the meaning alone.
 */
@Composable
private fun spentShare(summary: PeriodSummary?): SpentShare {
    val income = summary?.income?.amountMinor ?: 0L
    val expense = summary?.expense?.amountMinor ?: 0L
    return when {
        income > 0 && expense > income -> SpentShare(
            fraction = 1f,
            label = stringResource(R.string.home_spent_ring_over),
            line = stringResource(R.string.home_spent_over),
        )
        income > 0 -> {
            val percent = percentOf(expense, income)
            SpentShare(
                fraction = expense.toFloat() / income,
                label = stringResource(R.string.home_spent_ring_percent, percent),
                line = stringResource(R.string.home_spent_share, percent),
            )
        }
        expense > 0 -> SpentShare(
            fraction = 1f,
            label = stringResource(R.string.home_spent_ring_none),
            line = stringResource(
                R.string.home_spent_no_income,
                formatMoney(requireNotNull(summary).expense),
            ),
        )
        else -> SpentShare(
            fraction = 0f,
            label = stringResource(R.string.home_spent_ring_percent, 0),
            line = stringResource(R.string.home_spent_nothing),
        )
    }
}

/**
 * The hero's lead (ADR-035): an Expressive [CircularWavyProgressIndicator] of [SpentShare.fraction]
 * at `LargeAccentSize`, with the short label centered in `titleMediumEmphasized`. Default colours;
 * the wave and its amplitude are the component's own motion. TalkBack reads the line instead.
 */
@Composable
private fun SpentRing(spent: SpentShare, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(WavyProgressIndicatorDefaults.CircularContainerSize)
            .clearAndSetSemantics { contentDescription = spent.line },
        contentAlignment = Alignment.Center,
    ) {
        CircularWavyProgressIndicator(
            progress = { spent.fraction },
            modifier = Modifier.fillMaxSize(),
        )
        Text(
            text = spent.label,
            style = MaterialTheme.typography.labelSmallEmphasized.tabularNumbers,
        )
    }
}

/** [part] as a whole percentage of [whole] (> 0), rounded down, in Long maths (ADR-003). */
internal fun percentOf(part: Long, whole: Long): Int = (part * 100 / whole).toInt()

/**
 * The active accounts as an M3 uncontained carousel (ADR-034, ADR-037): each item is a card in the
 * secondary container pair with the account type in a [ShapeAccentIcon], the name and the balance.
 * Items keep one width (`CarouselItemWidth`) and scroll freely; `maskClip` clips the card's
 * shape at the edges.
 */
private fun LazyListScope.accountsSection(
    accounts: List<AccountWithBalance>,
    onOpenAccount: (Long) -> Unit,
    onManageAccounts: () -> Unit,
) {
    item(key = "accountsHeader") {
        SectionHeader(
            title = stringResource(R.string.home_accounts),
            action = {
                FilledTonalButton(onClick = onManageAccounts, shapes = ButtonDefaults.shapes()) {
                    Text(stringResource(R.string.home_manage_accounts))
                }
            },
        )
    }
    if (accounts.isEmpty()) return
    item(key = "accounts") {
        val state = rememberCarouselState { accounts.size }
        // Uncontained: every card keeps the same width, however many accounts there are (ADR-037).
        HorizontalUncontainedCarousel(
            state = state,
            itemWidth = FiscusLayout.CarouselItemWidth,
            itemSpacing = FiscusLayout.ItemSpacing,
            modifier = Modifier.fillMaxWidth(),
        ) { index ->
            val item = accounts[index]
            val balance = formatMoney(item.balance)
            Surface(
                onClick = { onOpenAccount(item.account.id) },
                color = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier
                    // Fill the carousel's fixed item width, whatever the content's size (ADR-037).
                    .fillMaxWidth()
                    // Grows into the account page and shrinks back on Back (ADR-028, ADR-036).
                    .sharedContainer(accountContainerKey(item.account.id))
                    .maskClip(MaterialTheme.shapes.extraLarge)
                    .clearAndSetSemantics {
                        contentDescription = "${item.account.name}, $balance"
                    },
            ) {
                Column(
                    modifier = Modifier.padding(FiscusLayout.ScreenMargin),
                    verticalArrangement = Arrangement.spacedBy(FiscusLayout.ItemSpacing),
                ) {
                    ShapeAccentIcon(
                        icon = item.account.type.iconRes,
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = MaterialTheme.colorScheme.onSecondary,
                        modifier = Modifier.padding(bottom = FiscusLayout.ItemSpacing),
                    )
                    Text(
                        text = item.account.name,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    AmountText(
                        text = netAmountText(item.balance),
                        style = MaterialTheme.typography.headlineSmall,
                    )
                    // No arrow on the card (user, 2026-11-16): below zero says so in words.
                    if (item.balance.isNegative) {
                        Text(
                            text = stringResource(R.string.account_in_debt_short),
                            style = MaterialTheme.typography.labelMedium,
                        )
                    }
                }
            }
        }
    }
}

/**
 * The newest transactions (shared [TransactionListItem], growing into their page, ADR-028) with
 * a tonal "See all" button (shape changes on press) opening the Transactions tab; the shared
 * [EmptyState] when there are none (ADR-031).
 */
private fun LazyListScope.recentSection(
    recent: List<TransactionListEntry>,
    onOpenTransaction: (Long) -> Unit,
    onSeeAllTransactions: () -> Unit,
) {
    item(key = "recentHeader") {
        SectionHeader(
            title = stringResource(R.string.home_recent),
            action = if (recent.isEmpty()) {
                null
            } else {
                {
                    FilledTonalButton(
                        onClick = onSeeAllTransactions,
                        shapes = ButtonDefaults.shapes(),
                    ) {
                        Text(stringResource(R.string.home_see_all))
                    }
                }
            },
        )
    }
    if (recent.isEmpty()) {
        item(key = "recentEmpty") {
            EmptyState(
                icon = R.drawable.ic_receipt_long_rounded,
                message = stringResource(R.string.home_empty),
            )
        }
        return
    }
    itemsIndexed(recent, key = { _, entry -> "recent-${entry.transaction.id}" }) { index, entry ->
        TransactionListItem(
            transaction = entry.transaction,
            supportingText = transactionSupportingText(entry),
            icon = entry.category?.let { categoryIconRes(it.iconKey) }
                ?: entry.transaction.type.iconRes,
            onClick = { onOpenTransaction(entry.transaction.id) },
            shapes = ListItemDefaults.segmentedShapes(index, recent.size),
            // Grows into the detail page and shrinks back on Back (ADR-028).
            modifier = Modifier.sharedContainer(transactionContainerKey(entry.transaction.id)),
        )
    }
}

/** A section title in `titleMedium` (a heading for TalkBack), with an optional trailing action. */
@Composable
private fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    action: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                start = FiscusLayout.ScreenMargin,
                top = FiscusLayout.ScreenMargin,
                bottom = FiscusLayout.TightSpacing,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier
                .weight(1f)
                .semantics { heading() },
        )
        action?.invoke()
    }
}

@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "Large font", showBackground = true, fontScale = 2f)
@Composable
private fun HomeScreenPreview() {
    FiscusTheme(dynamicColor = false) {
        HomeScreen(
            uiState = PreviewState,
            onOpenSettings = {},
            onAddTransaction = {},
            onOpenTransaction = {},
            onOpenAccount = {},
            onManageAccounts = {},
            onSeeAllTransactions = {},
        )
    }
}

@Preview(name = "Empty", showBackground = true)
@Composable
private fun HomeScreenEmptyPreview() {
    FiscusTheme(dynamicColor = false) {
        HomeScreen(
            uiState = HomeUiState(
                isLoading = false,
                totalBalance = Money(0, "INR"),
                accounts = listOf(AccountWithBalance(PreviewCash, Money(0, "INR"))),
            ),
            onOpenSettings = {},
            onAddTransaction = {},
            onOpenTransaction = {},
            onOpenAccount = {},
            onManageAccounts = {},
            onSeeAllTransactions = {},
        )
    }
}

private val PreviewCash = Account(
    id = 1,
    name = "Cash",
    type = AccountType.CASH,
    currencyCode = "INR",
    openingBalance = Money(0, "INR"),
)
private val PreviewBank = PreviewCash.copy(id = 2, name = "Bank", type = AccountType.BANK)
private val PreviewFood = Category(1, "Food & dining", CategoryKind.EXPENSE, "restaurant")
private val PreviewNow = Instant.parse("2026-11-15T10:30:00Z")

private val PreviewState = HomeUiState(
    isLoading = false,
    totalBalance = Money(12_458_000, "INR"),
    accounts = listOf(
        AccountWithBalance(PreviewCash, Money(458_000, "INR")),
        AccountWithBalance(PreviewBank, Money(12_000_000, "INR")),
    ),
    monthSummary = PeriodSummary(
        income = Money(5_000_000, "INR"),
        expense = Money(1_245_000, "INR"),
    ),
    recent = listOf(
        TransactionListEntry(
            transaction = Transaction(
                id = 1,
                type = TransactionType.EXPENSE,
                title = "Lunch with team",
                amount = Money(45_000, "INR"),
                accountId = 1,
                categoryId = 1,
                occurredAt = PreviewNow,
                createdAt = PreviewNow,
                updatedAt = PreviewNow,
            ),
            account = PreviewCash,
            toAccount = null,
            category = PreviewFood,
        ),
    ),
)
