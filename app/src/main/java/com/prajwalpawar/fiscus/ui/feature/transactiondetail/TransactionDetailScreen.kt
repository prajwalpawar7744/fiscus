package com.prajwalpawar.fiscus.ui.feature.transactiondetail

import android.content.res.Configuration
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
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
import androidx.compose.foundation.layout.size
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
import androidx.compose.material3.ListItemShapes
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
import com.prajwalpawar.fiscus.model.Category
import com.prajwalpawar.fiscus.model.CategoryKind
import com.prajwalpawar.fiscus.model.Money
import com.prajwalpawar.fiscus.model.Transaction
import com.prajwalpawar.fiscus.model.TransactionType
import com.prajwalpawar.fiscus.ui.components.ActionButtonGroup
import com.prajwalpawar.fiscus.ui.components.ActionStyle
import com.prajwalpawar.fiscus.ui.components.AmountText
import com.prajwalpawar.fiscus.ui.components.GroupAction
import com.prajwalpawar.fiscus.ui.components.LocalSnackbarHostState
import com.prajwalpawar.fiscus.ui.components.ShapeAccentIcon
import com.prajwalpawar.fiscus.ui.components.accentRoles
import com.prajwalpawar.fiscus.ui.components.categoryIconRes
import com.prajwalpawar.fiscus.ui.components.containedSegmentedColors
import com.prajwalpawar.fiscus.ui.components.dayLabel
import com.prajwalpawar.fiscus.ui.components.heroRoles
import com.prajwalpawar.fiscus.ui.components.iconRes
import com.prajwalpawar.fiscus.ui.components.labelRes
import com.prajwalpawar.fiscus.ui.components.sharedContainer
import com.prajwalpawar.fiscus.ui.components.transactionContainerKey
import com.prajwalpawar.fiscus.ui.theme.FiscusLayout
import com.prajwalpawar.fiscus.ui.theme.FiscusTheme
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import kotlin.time.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toJavaLocalDateTime


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
        // The page the list item grows into, and shrinks back to on Back (ADR-028).
        modifier = modifier
            .sharedContainer(transactionContainerKey(uiState.transactionId))
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        snackbarHost = { SnackbarHost(LocalSnackbarHostState.current) },
        bottomBar = {
            if (uiState.transaction != null) {
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
                            GroupAction(
                                label = stringResource(R.string.action_delete),
                                onClick = { onEvent(TransactionDetailEvent.DeleteRequested) },
                                icon = R.drawable.ic_delete_rounded,
                                style = ActionStyle.Danger,
                            ),
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
        topBar = {
            LargeFlexibleTopAppBar(
                // The transaction's own title (ADR-021); a generic one while loading.
                title = { Text(uiState.transaction?.title ?: stringResource(R.string.detail_title)) },
                // "Today · 7:30 pm": when it happened, at a glance (ADR-024).
                subtitle = detailSubtitle(uiState)?.let { subtitle -> { Text(subtitle) } },
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
                fadeIn(motion.defaultEffectsSpec())
                    .togetherWith(fadeOut(motion.fastEffectsSpec()))
                    .using(SizeTransform { _, _ -> motion.defaultSpatialSpec() })
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
            iconContentColor = MaterialTheme.colorScheme.error,
            title = { Text(stringResource(R.string.detail_delete_title)) },
            text = { Text(stringResource(R.string.detail_delete_message)) },
            confirmButton = {
                TextButton(
                    onClick = { onEvent(TransactionDetailEvent.DeleteConfirmed) },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error,
                    ),
                ) {
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
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(FiscusLayout.ScreenMargin),
        verticalArrangement = Arrangement.spacedBy(FiscusLayout.SectionSpacing),
    ) {
        AmountHero(uiState = uiState, transaction = transaction)
        Column(verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)) {
            val rows = detailRows(uiState, transaction)
            rows.forEachIndexed { index, row ->
                DetailRow(row = row, shapes = ListItemDefaults.segmentedShapes(index, rows.size))
            }
        }
    }
}

/**
 * The amount hero, the viewing side of hero moment 2 (ADR-010, ADR-025). It's led by the category:
 * a large [ShapeAccentIcon] (the category's icon, or the type's for a transfer) in the type's
 * accent roles, the unsigned amount in emphasized display type (ADR-032), and "Expense · Food & dining" below,
 * on the type-coloured card from `heroRoles()`. The label carries the meaning, never color.
 */
@Composable
private fun AmountHero(
    uiState: TransactionDetailUiState,
    transaction: Transaction,
    modifier: Modifier = Modifier,
) {
    val typeLabel = stringResource(transaction.type.labelRes)
    val line = uiState.category
        ?.let { stringResource(R.string.detail_hero_line, typeLabel, it.name) }
        ?: typeLabel
    val description = stringResource(
        R.string.detail_amount_description,
        line,
        formatMoney(transaction.amount),
    )
    val hero = transaction.type.heroRoles()
    val accent = transaction.type.accentRoles()
    Card(
        // Hero moment: a larger step of the shape scale, as in the editor (M3_EXPRESSIVE.md §3).
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(
            containerColor = hero.container,
            contentColor = hero.content,
        ),
        modifier = modifier
            .fillMaxWidth()
            .clearAndSetSemantics { contentDescription = description },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(FiscusLayout.SectionSpacing),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(FiscusLayout.ItemSpacing),
        ) {
            ShapeAccentIcon(
                icon = uiState.category?.let { categoryIconRes(it.iconKey) }
                    ?: transaction.type.iconRes,
                containerColor = accent.container,
                contentColor = accent.content,
                size = FiscusLayout.LargeAccentSize,
                iconSize = FiscusLayout.LargeAccentIconSize,
                modifier = Modifier.padding(bottom = FiscusLayout.ItemSpacing),
            )
            // One line at any size: big amounts step down instead of wrapping (ADR-027).
            AmountText(
                text = formatMoney(transaction.amount),
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

private data class DetailRowData(
    @DrawableRes val icon: Int,
    @StringRes val label: Int,
    val value: String?,
)

/** Type, category, account(s), date and note, as one segmented group (ADR-026). */
@Composable
private fun detailRows(uiState: TransactionDetailUiState, transaction: Transaction) = buildList {
    val account = R.drawable.ic_payments_rounded
    add(
        DetailRowData(
            transaction.type.iconRes,
            R.string.detail_type,
            stringResource(transaction.type.labelRes),
        ),
    )
    if (transaction.type != TransactionType.TRANSFER) {
        add(
            DetailRowData(
                icon = uiState.category?.let { categoryIconRes(it.iconKey) }
                    ?: R.drawable.ic_category_rounded,
                label = R.string.editor_category,
                value = uiState.category?.name,
            ),
        )
    }
    if (transaction.type == TransactionType.TRANSFER) {
        add(DetailRowData(account, R.string.editor_from_account, uiState.account?.name))
        add(DetailRowData(account, R.string.editor_to_account, uiState.toAccount?.name))
    } else {
        add(DetailRowData(account, R.string.editor_account, uiState.account?.name))
    }
    add(
        DetailRowData(
            R.drawable.ic_calendar_today_rounded,
            R.string.editor_date,
            uiState.occurredAt?.let(::formatDateTime),
        ),
    )
    if (transaction.note.isNotBlank()) {
        add(DetailRowData(R.drawable.ic_edit_rounded, R.string.editor_note, transaction.note))
    }
}

/** One labeled detail in the joined group: on `surfaceContainer`, the value in `titleMedium`. */
@Composable
private fun DetailRow(row: DetailRowData, shapes: ListItemShapes) {
    SegmentedListItem(
        shapes = shapes,
        colors = containedSegmentedColors(),
        leadingContent = { Icon(painterResource(row.icon), contentDescription = null) },
        overlineContent = { Text(stringResource(row.label)) },
    ) {
        Text(
            text = row.value ?: stringResource(R.string.detail_unknown),
            style = MaterialTheme.typography.titleMedium,
        )
    }
}


@Composable
private fun detailSubtitle(uiState: TransactionDetailUiState): String? {
    val occurredAt = uiState.occurredAt ?: return null
    val today = uiState.today ?: return null
    val time = remember(occurredAt) {
        DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)
            .format(occurredAt.toJavaLocalDateTime())
    }
    return stringResource(R.string.detail_subtitle, dayLabel(occurredAt.date, today), time)
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
                today = LocalDate(2026, 11, 15),
                account = Account(1, "Cash", AccountType.CASH, "INR", Money(0, "INR")),
                category = Category(1, "Food & dining", CategoryKind.EXPENSE, "restaurant"),
            ),
            onEvent = {},
            onBack = {},
            onEdit = {},
        )
    }
}
