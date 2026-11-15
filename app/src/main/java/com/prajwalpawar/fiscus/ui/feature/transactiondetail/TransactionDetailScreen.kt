package com.prajwalpawar.fiscus.ui.feature.transactiondetail

import android.content.res.Configuration
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.tooling.preview.Preview
import com.prajwalpawar.fiscus.R
import com.prajwalpawar.fiscus.core.money.formatMoney
import com.prajwalpawar.fiscus.model.Account
import com.prajwalpawar.fiscus.model.AccountType
import com.prajwalpawar.fiscus.model.Category
import com.prajwalpawar.fiscus.model.CategoryKind
import com.prajwalpawar.fiscus.model.Money
import com.prajwalpawar.fiscus.model.Transaction
import com.prajwalpawar.fiscus.model.TransactionType
import com.prajwalpawar.fiscus.ui.components.LocalSnackbarHostState
import com.prajwalpawar.fiscus.ui.components.ShapeAccentIcon
import com.prajwalpawar.fiscus.ui.components.categoryIconRes
import com.prajwalpawar.fiscus.ui.components.labelRes
import com.prajwalpawar.fiscus.ui.theme.FiscusLayout
import com.prajwalpawar.fiscus.ui.theme.FiscusTheme
import com.prajwalpawar.fiscus.ui.theme.tabularNumbers
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toJavaLocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import kotlin.time.Instant


/**
 * Stateless transaction detail screen. Not a hero moment (ADR-010): standard M3, calm data, with
 * the category icon as the only shape accent. All state comes from [uiState]; all actions go out
 * through lambdas.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class) // scroll behavior, LoadingIndicator
@Composable
fun TransactionDetailScreen(
    uiState: TransactionDetailUiState,
    onEvent: (TransactionDetailEvent) -> Unit,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val motion = MaterialTheme.motionScheme
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        snackbarHost = { SnackbarHost(LocalSnackbarHostState.current) },
        topBar = {
            LargeFlexibleTopAppBar(
                // The transaction's own title (ADR-021); a generic one while loading.
                title = { Text(uiState.transaction?.title ?: stringResource(R.string.detail_title)) },
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
    ) { innerPadding ->
        AnimatedContent(
            targetState = uiState.transaction,
            contentKey = { it == null },
            transitionSpec = {
                fadeIn(motion.defaultEffectsSpec()) togetherWith fadeOut(motion.fastEffectsSpec())
            },
            label = "detailLoading",
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) { transaction ->
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = if (transaction == null) Alignment.Center else Alignment.TopCenter,
            ) {
                if (transaction == null) {
                    LoadingIndicator()
                } else {
                    DetailContent(
                        uiState = uiState,
                        transaction = transaction,
                        onEdit = onEdit,
                        onDelete = { onEvent(TransactionDetailEvent.DeleteRequested) },
                        modifier = Modifier
                            .widthIn(max = FiscusLayout.MaxContentWidth)
                            .fillMaxWidth(),
                    )
                }
            }
        }
    }

    if (uiState.showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { onEvent(TransactionDetailEvent.DeleteDismissed) },
            icon = { Icon(painterResource(R.drawable.ic_delete_rounded), contentDescription = null) },
            title = { Text(stringResource(R.string.detail_delete_title)) },
            text = { Text(stringResource(R.string.detail_delete_message)) },
            confirmButton = {
                TextButton(onClick = { onEvent(TransactionDetailEvent.DeleteConfirmed) }) {
                    Text(stringResource(R.string.action_delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { onEvent(TransactionDetailEvent.DeleteDismissed) }) {
                    Text(stringResource(R.string.action_cancel))
                }
            },
        )
    }
}

@Composable
private fun DetailContent(
    uiState: TransactionDetailUiState,
    transaction: Transaction,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(vertical = FiscusLayout.ScreenMargin),
        verticalArrangement = Arrangement.spacedBy(FiscusLayout.ScreenMargin),
    ) {
        AmountHeader(
            transaction = transaction,
            modifier = Modifier.padding(horizontal = FiscusLayout.ScreenMargin),
        )
        Column {
            if (transaction.type == TransactionType.TRANSFER) {
                DetailRow(R.drawable.ic_payments_rounded, R.string.editor_from_account, uiState.account?.name)
                DetailRow(R.drawable.ic_payments_rounded, R.string.editor_to_account, uiState.toAccount?.name)
            } else {
                DetailRow(R.drawable.ic_payments_rounded, R.string.editor_account, uiState.account?.name)
                DetailRow(
                    icon = uiState.category?.let { categoryIconRes(it.iconKey) } ?: R.drawable.ic_category_rounded,
                    label = R.string.editor_category,
                    value = uiState.category?.name,
                    accent = true,
                )
            }
            DetailRow(R.drawable.ic_calendar_today_rounded, R.string.editor_date, uiState.occurredAt?.let(::formatDateTime))
            if (transaction.note.isNotBlank()) {
                DetailRow(R.drawable.ic_edit_rounded, R.string.editor_note, transaction.note)
            }
        }
        DetailActions(
            onEdit = onEdit,
            onDelete = onDelete,
            modifier = Modifier.padding(horizontal = FiscusLayout.ScreenMargin),
        )
    }
}

/** Hero amount with its type. The sign and label carry the meaning, never color alone. */
@Composable
private fun AmountHeader(transaction: Transaction, modifier: Modifier = Modifier) {
    val typeLabel = stringResource(transaction.type.labelRes)
    val amount = formatMoney(transaction.amount)
    val signed = when (transaction.type) {
        TransactionType.EXPENSE -> stringResource(R.string.amount_signed_expense, amount)
        TransactionType.INCOME -> stringResource(R.string.amount_signed_income, amount)
        TransactionType.TRANSFER -> amount
    }
    val description = stringResource(R.string.detail_amount_description, typeLabel, amount)
    Column(
        modifier = modifier.clearAndSetSemantics { contentDescription = description },
        verticalArrangement = Arrangement.spacedBy(FiscusLayout.TightSpacing),
    ) {
        Text(
            text = typeLabel,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = signed,
            style = MaterialTheme.typography.displayMediumEmphasized.tabularNumbers,
        )
    }
}

/** One labeled detail. With [accent], the icon is shown as a [ShapeAccentIcon] (category only). */
@Composable
private fun DetailRow(
    @DrawableRes icon: Int,
    @StringRes label: Int,
    value: String?,
    accent: Boolean = false,
) {
    ListItem(
        leadingContent = {
            if (accent) {
                ShapeAccentIcon(icon = icon)
            } else {
                Icon(painterResource(icon), contentDescription = null)
            }
        },
        overlineContent = { Text(stringResource(label)) },
    ) {
        Text(value ?: stringResource(R.string.detail_unknown))
    }
}

/** Edit and Delete as an M3 Expressive [ButtonGroup]; Delete uses the error role. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class) // ButtonGroup scope modifiers
@Composable
private fun DetailActions(onEdit: () -> Unit, onDelete: () -> Unit, modifier: Modifier = Modifier) {
    val editLabel = stringResource(R.string.action_edit)
    val deleteLabel = stringResource(R.string.action_delete)
    ButtonGroup(
        overflowIndicator = { menuState -> ButtonGroupDefaults.OverflowIndicator(menuState) },
        modifier = modifier.fillMaxWidth(),
    ) {
        customItem(
            buttonGroupContent = {
                val interactionSource = remember { MutableInteractionSource() }
                Button(
                    onClick = onEdit,
                    shapes = ButtonDefaults.shapes(),
                    interactionSource = interactionSource,
                    modifier = Modifier
                        .weight(1f)
                        .animateWidth(interactionSource),
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_edit_rounded),
                        contentDescription = null,
                        modifier = Modifier.size(ButtonDefaults.IconSize),
                    )
                    Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                    Text(editLabel)
                }
            },
            menuContent = { menuState ->
                DropdownMenuItem(
                    text = { Text(editLabel) },
                    onClick = {
                        menuState.dismiss()
                        onEdit()
                    },
                )
            },
        )
        customItem(
            buttonGroupContent = {
                val interactionSource = remember { MutableInteractionSource() }
                Button(
                    onClick = onDelete,
                    shapes = ButtonDefaults.shapes(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer,
                    ),
                    interactionSource = interactionSource,
                    modifier = Modifier
                        .weight(1f)
                        .animateWidth(interactionSource),
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_delete_rounded),
                        contentDescription = null,
                        modifier = Modifier.size(ButtonDefaults.IconSize),
                    )
                    Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                    Text(deleteLabel)
                }
            },
            menuContent = { menuState ->
                DropdownMenuItem(
                    text = { Text(deleteLabel) },
                    onClick = {
                        menuState.dismiss()
                        onDelete()
                    },
                )
            },
        )
    }
}

private fun formatDateTime(dateTime: LocalDateTime): String =
    DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT)
        .format(dateTime.toJavaLocalDateTime())

@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "Large font", showBackground = true, fontScale = 2f)
@Composable
private fun TransactionDetailScreenPreview() {
    val now = Instant.fromEpochMilliseconds(1_763_200_000_000)
    FiscusTheme(dynamicColor = false) {
        TransactionDetailScreen(
            uiState = TransactionDetailUiState(
                isLoading = false,
                transaction = Transaction(
                    id = 1,
                    type = TransactionType.EXPENSE,
                    title = "Dinner with friends",
                    amount = Money(125_000, "INR"),
                    accountId = 1,
                    categoryId = 1,
                    note = "Dinner with friends",
                    occurredAt = now,
                    createdAt = now,
                    updatedAt = now,
                ),
                occurredAt = LocalDateTime(2026, 11, 15, 19, 30),
                account = Account(1, "Cash", AccountType.CASH, "INR", Money(0, "INR")),
                category = Category(1, "Food & dining", CategoryKind.EXPENSE, "restaurant"),
            ),
            onEvent = {},
            onBack = {},
            onEdit = {},
        )
    }
}
