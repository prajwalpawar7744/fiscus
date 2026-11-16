package com.prajwalpawar.fiscus.ui.feature.accounteditor

import android.content.res.Configuration
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import com.prajwalpawar.fiscus.R
import com.prajwalpawar.fiscus.core.money.formatMoney
import com.prajwalpawar.fiscus.model.AccountType
import com.prajwalpawar.fiscus.ui.components.AccountTypeDisplayOrder
import com.prajwalpawar.fiscus.ui.components.ActionButtonGroup
import com.prajwalpawar.fiscus.ui.components.ConnectedChoiceGroup
import com.prajwalpawar.fiscus.ui.components.CurrencyPrefixTransformation
import com.prajwalpawar.fiscus.ui.components.GroupAction
import com.prajwalpawar.fiscus.ui.components.LocalSnackbarHostState
import com.prajwalpawar.fiscus.ui.components.SaveHint
import com.prajwalpawar.fiscus.ui.components.SwitchRow
import com.prajwalpawar.fiscus.ui.components.iconRes
import com.prajwalpawar.fiscus.ui.components.labelRes
import com.prajwalpawar.fiscus.ui.theme.FiscusLayout
import com.prajwalpawar.fiscus.ui.theme.FiscusTheme
import com.prajwalpawar.fiscus.ui.theme.tabularNumbers
import java.util.Currency

/**
 * Stateless add/edit account form (ADR-036). Not a hero moment (ADR-010): a standard M3 form with
 * name, type (connected button group), the balance (no currency: ADR-037), either the
 * opening balance or, when editing, the current one to reconcile to, signed by an "in debt"
 * switch, and the "Default account" switch. Save sits in the shared action group within thumb
 * reach (ADR-028).
 */
@OptIn(
    ExperimentalMaterial3Api::class, // TopAppBarDefaults scroll behavior
    ExperimentalMaterial3ExpressiveApi::class, // LoadingIndicator
)
@Composable
fun AccountEditorScreen(
    uiState: AccountEditorUiState,
    onEvent: (AccountEditorEvent) -> Unit,
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
                            if (uiState.isNew) {
                                R.string.account_editor_title_add
                            } else {
                                R.string.account_editor_title_edit
                            },
                        ),
                    )
                },
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
        bottomBar = {
            if (!uiState.isLoading) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(FiscusLayout.ScreenMargin),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    SaveHint(hint = uiState.saveHint)
                    ActionButtonGroup(
                        actions = listOf(
                            GroupAction(
                                label = stringResource(R.string.account_save),
                                onClick = { onEvent(AccountEditorEvent.Save) },
                                enabled = uiState.canSave,
                                isLoading = uiState.isSaving,
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
            label = "accountEditorLoading",
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                // As in the transaction editor: the Save bar is already reserved, so imePadding()
                // only adds the part of the keyboard not covered yet.
                .consumeWindowInsets(innerPadding),
        ) { isLoading ->
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = if (isLoading) Alignment.Center else Alignment.TopCenter,
            ) {
                if (isLoading) {
                    LoadingIndicator()
                } else {
                    AccountForm(
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
private fun AccountForm(
    uiState: AccountEditorUiState,
    onEvent: (AccountEditorEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val motion = MaterialTheme.motionScheme
    val fieldModifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = FiscusLayout.ScreenMargin)
    Column(
        modifier = modifier
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(vertical = FiscusLayout.ScreenMargin)
            .animateContentSize(motion.defaultSpatialSpec()),
        verticalArrangement = Arrangement.spacedBy(FiscusLayout.SectionSpacing),
    ) {
        OutlinedTextField(
            value = uiState.name,
            onValueChange = { onEvent(AccountEditorEvent.NameChanged(it)) },
            label = { Text(stringResource(R.string.account_name)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Next,
            ),
            modifier = fieldModifier,
        )
        Column(verticalArrangement = Arrangement.spacedBy(FiscusLayout.ItemSpacing)) {
            Text(
                text = stringResource(R.string.account_type),
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(horizontal = FiscusLayout.ScreenMargin),
            )
            ConnectedChoiceGroup(
                options = AccountTypeDisplayOrder,
                selected = uiState.type,
                onSelect = { onEvent(AccountEditorEvent.TypeChanged(it)) },
                label = { stringResource(it.labelRes) },
                icon = { it.iconRes },
                modifier = Modifier.padding(horizontal = FiscusLayout.ScreenMargin),
            )
        }
        BalanceSection(uiState = uiState, onEvent = onEvent)
        if (uiState.canBeDefault) {
            SwitchRow(
                label = stringResource(R.string.account_default),
                supportingText = stringResource(R.string.account_default_hint),
                checked = uiState.isDefault,
                onCheckedChange = { onEvent(AccountEditorEvent.DefaultChanged(it)) },
                modifier = Modifier.padding(horizontal = FiscusLayout.ScreenMargin),
            )
        }
    }
}

/**
 * The balance: when editing, a connected choice of which balance is typed (opening, or current to
 * reconcile); then the unsigned amount and the "in debt" switch. In current mode a line previews
 * the opening balance that saving will store.
 */
@Composable
private fun BalanceSection(uiState: AccountEditorUiState, onEvent: (AccountEditorEvent) -> Unit) {
    val motion = MaterialTheme.motionScheme
    Column(verticalArrangement = Arrangement.spacedBy(FiscusLayout.ItemSpacing)) {
        if (!uiState.isNew) {
            ConnectedChoiceGroup(
                options = BalanceMode.entries,
                selected = uiState.balanceMode,
                onSelect = { onEvent(AccountEditorEvent.BalanceModeChanged(it)) },
                label = { stringResource(it.labelRes) },
                modifier = Modifier.padding(horizontal = FiscusLayout.ScreenMargin),
            )
        }
        // Same as the transaction editor's amount field.
        val symbol = runCatching { Currency.getInstance(uiState.currencyCode).symbol }
            .getOrDefault(uiState.currencyCode)
        OutlinedTextField(
            value = uiState.amountText,
            onValueChange = { onEvent(AccountEditorEvent.AmountChanged(it)) },
            label = { Text(stringResource(uiState.balanceMode.labelRes)) },
            singleLine = true,
            isError = uiState.amountError != null,
            supportingText = uiState.amountError?.let { error -> { Text(stringResource(error)) } },
            textStyle = MaterialTheme.typography.bodyLarge.tabularNumbers,
            visualTransformation = CurrencyPrefixTransformation(symbol),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal,
                imeAction = ImeAction.Done,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = FiscusLayout.ScreenMargin),
        )
        SwitchRow(
            label = stringResource(R.string.account_in_debt),
            checked = uiState.inDebt,
            onCheckedChange = { onEvent(AccountEditorEvent.InDebtChanged(it)) },
            modifier = Modifier.padding(horizontal = FiscusLayout.ScreenMargin),
        )
        AnimatedVisibility(
            visible = uiState.openingPreview != null,
            enter = expandVertically(motion.defaultSpatialSpec()) +
                fadeIn(motion.defaultEffectsSpec()),
            exit = shrinkVertically(motion.fastSpatialSpec()) + fadeOut(motion.fastEffectsSpec()),
        ) {
            uiState.openingPreview?.let { opening ->
                Text(
                    text = stringResource(R.string.account_opening_becomes, formatMoney(opening)),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = FiscusLayout.ScreenMargin),
                )
            }
        }
    }
}

private val BalanceMode.labelRes: Int
    get() = when (this) {
        BalanceMode.OPENING -> R.string.account_balance_mode_opening
        BalanceMode.CURRENT -> R.string.account_balance_mode_current
    }

@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "Large font", showBackground = true, fontScale = 2f)
@Composable
private fun AccountEditorScreenPreview() {
    FiscusTheme(dynamicColor = false) {
        AccountEditorScreen(
            uiState = AccountEditorUiState(
                isLoading = false,
                isNew = false,
                name = "Credit card",
                type = AccountType.CARD,
                currencyCode = "INR",
                isDefault = true,
                balanceMode = BalanceMode.CURRENT,
                amountText = "4500",
                inDebt = true,
            ),
            onEvent = {},
            onBack = {},
        )
    }
}
