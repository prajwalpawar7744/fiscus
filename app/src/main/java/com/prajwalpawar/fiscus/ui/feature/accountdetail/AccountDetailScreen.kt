package com.prajwalpawar.fiscus.ui.feature.accountdetail

import android.content.res.Configuration
import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.prajwalpawar.fiscus.R
import com.prajwalpawar.fiscus.core.money.formatMoney
import com.prajwalpawar.fiscus.model.Account
import com.prajwalpawar.fiscus.model.AccountType
import com.prajwalpawar.fiscus.model.AccountWithBalance
import com.prajwalpawar.fiscus.model.Money
import com.prajwalpawar.fiscus.ui.components.ActionButtonGroup
import com.prajwalpawar.fiscus.ui.components.ActionStyle
import com.prajwalpawar.fiscus.ui.components.AmountText
import com.prajwalpawar.fiscus.ui.components.DetailRow
import com.prajwalpawar.fiscus.ui.components.GroupAction
import com.prajwalpawar.fiscus.ui.components.LocalSnackbarHostState
import com.prajwalpawar.fiscus.ui.components.ShapeAccentIcon
import com.prajwalpawar.fiscus.ui.components.accountContainerKey
import com.prajwalpawar.fiscus.ui.components.iconRes
import com.prajwalpawar.fiscus.ui.components.labelRes
import com.prajwalpawar.fiscus.ui.components.netAmountText
import com.prajwalpawar.fiscus.ui.components.sharedContainer
import com.prajwalpawar.fiscus.ui.theme.FiscusLayout
import com.prajwalpawar.fiscus.ui.theme.FiscusTheme

/**
 * Stateless page for one account (ADR-036/037). Not a hero moment (ADR-010): the balance sits on
 * the detail-hero card family, then the account's facts (type, balances, transaction count,
 * status, default) as one contained group, with the page's
 * actions in the shared action group: [Delete | Edit] while unused, [Archive | Edit] once used,
 * [Unarchive | Edit] when archived. The page grows out of the card or row that opened it.
 */
@OptIn(
    ExperimentalMaterial3Api::class, // TopAppBarDefaults scroll behavior
    ExperimentalMaterial3ExpressiveApi::class, // LoadingIndicator
)
@Composable
fun AccountDetailScreen(
    uiState: AccountDetailUiState,
    onEvent: (AccountDetailEvent) -> Unit,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val motion = MaterialTheme.motionScheme
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    Scaffold(
        modifier = modifier
            .sharedContainer(accountContainerKey(uiState.accountId))
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        snackbarHost = { SnackbarHost(LocalSnackbarHostState.current) },
        topBar = {
            LargeFlexibleTopAppBar(
                title = {
                    Text(
                        uiState.account?.account?.name
                            ?: stringResource(R.string.account_detail_title),
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
            uiState.account?.let { item ->
                // Actions within thumb reach, in the shared action group (ADR-028).
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(FiscusLayout.ScreenMargin),
                    contentAlignment = Alignment.Center,
                ) {
                    ActionButtonGroup(
                        actions = listOf(
                            firstAction(uiState, item.account, onEvent),
                            GroupAction(
                                label = stringResource(R.string.action_edit),
                                onClick = onEdit,
                                icon = R.drawable.ic_edit_rounded,
                            ),
                        ),
                        modifier = Modifier.widthIn(max = FiscusLayout.MaxContentWidth),
                    )
                }
            }
        },
    ) { innerPadding ->
        AnimatedContent(
            targetState = uiState.isLoading,
            transitionSpec = {
                fadeIn(motion.defaultEffectsSpec())
                    .togetherWith(fadeOut(motion.fastEffectsSpec()))
                    .using(SizeTransform { _, _ -> motion.defaultSpatialSpec() })
            },
            label = "accountLoading",
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) { isLoading ->
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = if (isLoading) Alignment.Center else Alignment.TopCenter,
            ) {
                val account = uiState.account
                if (isLoading || account == null) {
                    LoadingIndicator()
                } else {
                    AccountContent(
                        uiState = uiState,
                        account = account,
                        modifier = Modifier
                            .widthIn(max = FiscusLayout.MaxContentWidth)
                            .fillMaxSize(),
                    )
                }
            }
        }
    }

    if (uiState.showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { onEvent(AccountDetailEvent.DeleteDismissed) },
            icon = {
                Icon(painterResource(R.drawable.ic_delete_rounded), contentDescription = null)
            },
            iconContentColor = MaterialTheme.colorScheme.error,
            title = { Text(stringResource(R.string.account_delete_title)) },
            text = { Text(stringResource(R.string.account_delete_message)) },
            confirmButton = {
                TextButton(
                    onClick = { onEvent(AccountDetailEvent.DeleteConfirmed) },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error,
                    ),
                ) {
                    Text(stringResource(R.string.action_delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { onEvent(AccountDetailEvent.DeleteDismissed) }) {
                    Text(stringResource(R.string.action_cancel))
                }
            },
        )
    }
}

/** Delete while nothing refers to the account; otherwise Archive, or Unarchive (ADR-036). */
@Composable
private fun firstAction(
    uiState: AccountDetailUiState,
    account: Account,
    onEvent: (AccountDetailEvent) -> Unit,
): GroupAction = when {
    account.archived -> GroupAction(
        label = stringResource(R.string.account_unarchive),
        onClick = { onEvent(AccountDetailEvent.ArchiveToggled) },
        style = ActionStyle.Secondary,
    )
    uiState.canDelete -> GroupAction(
        label = stringResource(R.string.action_delete),
        onClick = { onEvent(AccountDetailEvent.DeleteRequested) },
        icon = R.drawable.ic_delete_rounded,
        style = ActionStyle.Danger,
    )
    else -> GroupAction(
        label = stringResource(R.string.account_archive),
        onClick = { onEvent(AccountDetailEvent.ArchiveToggled) },
        style = ActionStyle.Secondary,
    )
}

/** The hero, then the account's facts as one contained segmented group (ADR-037). */
@Composable
private fun AccountContent(
    uiState: AccountDetailUiState,
    account: AccountWithBalance,
    modifier: Modifier = Modifier,
) {
    val rows = detailRows(uiState, account)
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(FiscusLayout.ScreenMargin),
        verticalArrangement = Arrangement.spacedBy(FiscusLayout.SectionSpacing),
    ) {
        BalanceHero(account = account)
        Column(verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)) {
            rows.forEachIndexed { index, row ->
                DetailRow(
                    icon = row.icon,
                    label = row.label,
                    value = row.value,
                    shapes = ListItemDefaults.segmentedShapes(index, rows.size),
                )
            }
        }
    }
}

private data class AccountFact(@DrawableRes val icon: Int, val label: String, val value: String)

/** Type, opening and current balance, transaction count, status and default (ADR-037). */
@Composable
private fun detailRows(uiState: AccountDetailUiState, item: AccountWithBalance): List<AccountFact> {
    val account = item.account
    val yes = stringResource(R.string.account_yes)
    val no = stringResource(R.string.account_no)
    return listOf(
        AccountFact(
            account.type.iconRes,
            stringResource(R.string.account_type),
            stringResource(account.type.labelRes),
        ),
        AccountFact(
            R.drawable.ic_savings_rounded,
            stringResource(R.string.account_balance_mode_opening),
            balanceText(account.openingBalance),
        ),
        AccountFact(
            R.drawable.ic_account_balance_wallet_rounded,
            stringResource(R.string.account_balance_mode_current),
            balanceText(item.balance),
        ),
        AccountFact(
            R.drawable.ic_receipt_long_rounded,
            stringResource(R.string.account_detail_transactions),
            uiState.transactionCount.toString(),
        ),
        AccountFact(
            R.drawable.ic_category_rounded,
            stringResource(R.string.account_detail_status),
            stringResource(
                if (account.archived) R.string.accounts_archived else R.string.accounts_active,
            ),
        ),
        AccountFact(
            R.drawable.ic_check_rounded,
            stringResource(R.string.account_default),
            if (account.isDefault) yes else no,
        ),
    )
}

/** "₹4,580", or "₹5,000 · In debt" below zero: no sign or arrow on screen (ADR-032, ADR-037). */
@Composable
private fun balanceText(balance: Money): String =
    if (balance.isNegative) {
        stringResource(R.string.home_balance_in_debt, netAmountText(balance))
    } else {
        formatMoney(balance)
    }

/**
 * The account's balance on the detail-hero card family (ADR-025): `extraLarge` card in the
 * secondary container pair, type icon in a large [ShapeAccentIcon], the balance in
 * `displayLargeEmphasized` with no sign or arrow (below zero says "In debt" under it), and the
 * type below. TalkBack reads it as one node with the signed balance.
 */
@Composable
private fun BalanceHero(account: AccountWithBalance, modifier: Modifier = Modifier) {
    val balance = account.balance
    val typeLabel = stringResource(account.account.type.labelRes)
    val line = if (balance.isNegative) {
        stringResource(R.string.home_balance_in_debt, typeLabel)
    } else {
        typeLabel
    }
    val description = stringResource(R.string.account_balance_description, formatMoney(balance))
    Card(
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        ),
        modifier = modifier
            .fillMaxWidth()
            .clearAndSetSemantics { contentDescription = "$description, $line" },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(FiscusLayout.SectionSpacing),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(FiscusLayout.ItemSpacing),
        ) {
            ShapeAccentIcon(
                icon = account.account.type.iconRes,
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary,
                size = FiscusLayout.LargeAccentSize,
                iconSize = FiscusLayout.LargeAccentIconSize,
                modifier = Modifier.padding(bottom = FiscusLayout.ItemSpacing),
            )
            // One line at any size: big amounts step down instead of wrapping (ADR-027).
            AmountText(
                text = netAmountText(balance),
                style = MaterialTheme.typography.displayLargeEmphasized,
                textAlign = TextAlign.Center,
            )
            Text(
                text = line,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "Large font", showBackground = true, fontScale = 2f)
@Composable
private fun AccountDetailScreenPreview() {
    FiscusTheme(dynamicColor = false) {
        AccountDetailScreen(
            uiState = AccountDetailUiState(
                accountId = 2,
                isLoading = false,
                account = AccountWithBalance(
                    Account(2, "Credit card", AccountType.CARD, "INR", Money(-500_000, "INR")),
                    Money(-500_000, "INR"),
                ),
                transactionCount = 3,
            ),
            onEvent = {},
            onBack = {},
            onEdit = {},
        )
    }
}
