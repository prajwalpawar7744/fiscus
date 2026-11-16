package com.prajwalpawar.fiscus.ui.feature.accounts

import android.content.res.Configuration
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.prajwalpawar.fiscus.R
import com.prajwalpawar.fiscus.core.money.formatMoney
import com.prajwalpawar.fiscus.model.Account
import com.prajwalpawar.fiscus.model.AccountType
import com.prajwalpawar.fiscus.model.AccountWithBalance
import com.prajwalpawar.fiscus.model.Money
import com.prajwalpawar.fiscus.ui.components.AmountText
import com.prajwalpawar.fiscus.ui.components.EmptyState
import com.prajwalpawar.fiscus.ui.components.LocalSnackbarHostState
import com.prajwalpawar.fiscus.ui.components.ShapeAccentIcon
import com.prajwalpawar.fiscus.ui.components.accountContainerKey
import com.prajwalpawar.fiscus.ui.components.containedSegmentedColors
import com.prajwalpawar.fiscus.ui.components.iconRes
import com.prajwalpawar.fiscus.ui.components.labelRes
import com.prajwalpawar.fiscus.ui.components.netAmountText
import com.prajwalpawar.fiscus.ui.components.sharedContainer
import com.prajwalpawar.fiscus.ui.theme.FiscusLayout
import com.prajwalpawar.fiscus.ui.theme.FiscusTheme

/**
 * Stateless Accounts page (ADR-036): active accounts, then archived ones, each as one contained
 * segmented group whose rows grow into the account page (ADR-028). Not a hero moment: standard
 * M3, calm data. "Add account" is an extended FAB (icon + label) that collapses to its icon while
 * scrolling down.
 */
@OptIn(
    ExperimentalMaterial3Api::class, // TopAppBarDefaults scroll behavior
    ExperimentalMaterial3ExpressiveApi::class, // LoadingIndicator
)
@Composable
fun AccountsScreen(
    uiState: AccountsUiState,
    onBack: () -> Unit,
    onOpenAccount: (Long) -> Unit,
    onAddAccount: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val motion = MaterialTheme.motionScheme
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val listState = rememberLazyListState()
    val fabExpanded by remember { derivedStateOf { listState.firstVisibleItemIndex == 0 } }
    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeFlexibleTopAppBar(
                title = { Text(stringResource(R.string.accounts_title)) },
                navigationIcon = {
                    IconButton(shapes = IconButtonDefaults.shapes(), onClick = onBack) {
                        Icon(
                            painter = painterResource(R.drawable.ic_arrow_back_rounded),
                            contentDescription = stringResource(R.string.action_navigate_back),
                        )
                    }
                },
                scrollBehavior = scrollBehavior,
            )
        },
        snackbarHost = { SnackbarHost(LocalSnackbarHostState.current) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = { Text(stringResource(R.string.accounts_add)) },
                icon = {
                    Icon(painterResource(R.drawable.ic_add_rounded), contentDescription = null)
                },
                onClick = onAddAccount,
                expanded = fabExpanded,
            )
        },
    ) { innerPadding ->
        AnimatedContent(
            targetState = uiState.isLoading,
            transitionSpec = {
                fadeIn(motion.defaultEffectsSpec())
                    .togetherWith(fadeOut(motion.fastEffectsSpec()))
                    .using(SizeTransform { _, _ -> motion.defaultSpatialSpec() })
            },
            label = "accountsLoading",
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) { isLoading ->
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = if (isLoading) Alignment.Center else Alignment.TopCenter,
            ) {
                when {
                    isLoading -> LoadingIndicator()
                    uiState.active.isEmpty() && uiState.archived.isEmpty() -> EmptyState(
                        icon = R.drawable.ic_account_balance_wallet_rounded,
                        message = stringResource(R.string.accounts_empty),
                    )
                    else -> LazyColumn(
                        state = listState,
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
                        accountGroup(R.string.accounts_active, uiState.active, onOpenAccount)
                        accountGroup(R.string.accounts_archived, uiState.archived, onOpenAccount)
                    }
                }
            }
        }
    }
}

/** A titled group of account rows; nothing when [accounts] is empty. */
private fun LazyListScope.accountGroup(
    title: Int,
    accounts: List<AccountWithBalance>,
    onOpenAccount: (Long) -> Unit,
) {
    if (accounts.isEmpty()) return
    item(key = "header-$title") {
        Text(
            text = stringResource(title),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = FiscusLayout.ScreenMargin,
                    top = FiscusLayout.ScreenMargin,
                    bottom = FiscusLayout.TightSpacing,
                )
                .semantics { heading() },
        )
    }
    itemsIndexed(accounts, key = { _, item -> "account-${item.account.id}" }) { index, item ->
        AccountRow(
            item = item,
            index = index,
            count = accounts.size,
            onClick = { onOpenAccount(item.account.id) },
        )
    }
}

/**
 * One account: type icon in a [ShapeAccentIcon], name, "Card · Default · In debt", and the
 * balance with no sign or arrow (ADR-037). TalkBack reads "Card, Card · 3 transactions, Balance,
 * −₹5,000".
 */
@Composable
private fun AccountRow(item: AccountWithBalance, index: Int, count: Int, onClick: () -> Unit) {
    val account = item.account
    val description =
        stringResource(R.string.account_balance_description, formatMoney(item.balance))
    val supporting = accountSupportingText(item)
    SegmentedListItem(
        onClick = onClick,
        shapes = ListItemDefaults.segmentedShapes(index, count),
        colors = containedSegmentedColors(),
        modifier = Modifier
            .sharedContainer(accountContainerKey(account.id))
            .clearAndSetSemantics {
                contentDescription = "${account.name}, $supporting, $description"
            },
        leadingContent = { ShapeAccentIcon(icon = account.type.iconRes) },
        supportingContent = { Text(supporting) },
        trailingContent = {
            AmountText(
                text = netAmountText(item.balance),
                style = MaterialTheme.typography.titleSmall,
            )
        },
    ) {
        Text(
            text = account.name,
            style = MaterialTheme.typography.titleMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/**
 * "Bank", plus "Default" and "In debt" when they apply (ADR-037), then "3 transactions"
 * (ADR-041).
 */
@Composable
private fun accountSupportingText(item: AccountWithBalance): String = buildList {
    add(stringResource(item.account.type.labelRes))
    if (item.account.isDefault) add(stringResource(R.string.account_default_short))
    if (item.balance.isNegative) add(stringResource(R.string.account_in_debt_short))
    val count = item.transactionCount
    add(pluralStringResource(R.plurals.transaction_count, count, count))
}.joinToString(stringResource(R.string.list_separator))

@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "Large font", showBackground = true, fontScale = 2f)
@Composable
private fun AccountsScreenPreview() {
    val cash = Account(1, "Cash", AccountType.CASH, "INR", Money(0, "INR"), isDefault = true)
    FiscusTheme(dynamicColor = false) {
        AccountsScreen(
            uiState = AccountsUiState(
                isLoading = false,
                active = listOf(
                    AccountWithBalance(cash, Money(458_000, "INR"), transactionCount = 12),
                    AccountWithBalance(
                        cash.copy(
                            id = 2,
                            name = "Credit card",
                            type = AccountType.CARD,
                            isDefault = false,
                        ),
                        Money(-500_000, "INR"),
                    ),
                ),
                archived = listOf(
                    AccountWithBalance(
                        cash.copy(
                            id = 3,
                            name = "Old wallet",
                            type = AccountType.WALLET,
                            archived = true,
                        ),
                        Money(0, "INR"),
                    ),
                ),
            ),
            onBack = {},
            onOpenAccount = {},
            onAddAccount = {},
        )
    }
}
