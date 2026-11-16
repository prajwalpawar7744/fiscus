package com.prajwalpawar.fiscus.ui.feature.categorydetail

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
import androidx.compose.material3.IconButtonDefaults
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
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.prajwalpawar.fiscus.R
import com.prajwalpawar.fiscus.model.Category
import com.prajwalpawar.fiscus.model.CategoryKind
import com.prajwalpawar.fiscus.model.CategoryWithUsage
import com.prajwalpawar.fiscus.ui.components.ActionButtonGroup
import com.prajwalpawar.fiscus.ui.components.ActionStyle
import com.prajwalpawar.fiscus.ui.components.DetailRow
import com.prajwalpawar.fiscus.ui.components.GroupAction
import com.prajwalpawar.fiscus.ui.components.LocalSnackbarHostState
import com.prajwalpawar.fiscus.ui.components.ShapeAccentIcon
import com.prajwalpawar.fiscus.ui.components.accentRoles
import com.prajwalpawar.fiscus.ui.components.categoryContainerKey
import com.prajwalpawar.fiscus.ui.components.categoryIconRes
import com.prajwalpawar.fiscus.ui.components.heroRoles
import com.prajwalpawar.fiscus.ui.components.iconRes
import com.prajwalpawar.fiscus.ui.components.labelRes
import com.prajwalpawar.fiscus.ui.components.sharedContainer
import com.prajwalpawar.fiscus.ui.theme.FiscusLayout
import com.prajwalpawar.fiscus.ui.theme.FiscusTheme

/**
 * Stateless page for one category (ADR-042), built like the account page (ADR-036/037). Not a
 * hero moment (ADR-010): the category on the detail-hero card family, then its facts (kind,
 * source, transaction count, status) as one contained group, with the page's actions in the
 * shared action group: [Unarchive | Edit] when archived, [Delete | Edit] while unused,
 * [Archive | Edit] once used. The page grows out of the row that opened it.
 */
@OptIn(
    ExperimentalMaterial3Api::class, // TopAppBarDefaults scroll behavior
    ExperimentalMaterial3ExpressiveApi::class, // LoadingIndicator
)
@Composable
fun CategoryDetailScreen(
    uiState: CategoryDetailUiState,
    onEvent: (CategoryDetailEvent) -> Unit,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val motion = MaterialTheme.motionScheme
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    Scaffold(
        modifier = modifier
            .sharedContainer(categoryContainerKey(uiState.categoryId))
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        snackbarHost = { SnackbarHost(LocalSnackbarHostState.current) },
        topBar = {
            LargeFlexibleTopAppBar(
                title = {
                    Text(
                        uiState.category?.category?.name
                            ?: stringResource(R.string.category_detail_title),
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
        bottomBar = {
            uiState.category?.let { item ->
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
                            firstAction(uiState, item.category, onEvent),
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
            label = "categoryLoading",
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) { isLoading ->
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = if (isLoading) Alignment.Center else Alignment.TopCenter,
            ) {
                val category = uiState.category
                if (isLoading || category == null) {
                    LoadingIndicator()
                } else {
                    CategoryContent(
                        item = category,
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
            onDismissRequest = { onEvent(CategoryDetailEvent.DeleteDismissed) },
            icon = {
                Icon(painterResource(R.drawable.ic_delete_rounded), contentDescription = null)
            },
            iconContentColor = MaterialTheme.colorScheme.error,
            title = { Text(stringResource(R.string.category_delete_title)) },
            text = { Text(stringResource(R.string.category_delete_message)) },
            confirmButton = {
                TextButton(
                    shapes = ButtonDefaults.shapes(),
                    onClick = { onEvent(CategoryDetailEvent.DeleteConfirmed) },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error,
                    ),
                ) {
                    Text(stringResource(R.string.action_delete))
                }
            },
            dismissButton = {
                TextButton(
                    shapes = ButtonDefaults.shapes(),
                    onClick = { onEvent(CategoryDetailEvent.DeleteDismissed) },
                ) {
                    Text(stringResource(R.string.action_cancel))
                }
            },
        )
    }
}

/** Unarchive when archived; otherwise Delete while unused, else Archive (ADR-036, ADR-042). */
@Composable
private fun firstAction(
    uiState: CategoryDetailUiState,
    category: Category,
    onEvent: (CategoryDetailEvent) -> Unit,
): GroupAction = when {
    category.archived -> GroupAction(
        label = stringResource(R.string.category_unarchive),
        onClick = { onEvent(CategoryDetailEvent.ArchiveToggled) },
        style = ActionStyle.Secondary,
    )
    uiState.canDelete -> GroupAction(
        label = stringResource(R.string.action_delete),
        onClick = { onEvent(CategoryDetailEvent.DeleteRequested) },
        icon = R.drawable.ic_delete_rounded,
        style = ActionStyle.Danger,
    )
    else -> GroupAction(
        label = stringResource(R.string.category_archive),
        onClick = { onEvent(CategoryDetailEvent.ArchiveToggled) },
        style = ActionStyle.Secondary,
    )
}

/** The hero, then the category's facts as one contained segmented group. */
@Composable
private fun CategoryContent(item: CategoryWithUsage, modifier: Modifier = Modifier) {
    val rows = detailRows(item)
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(FiscusLayout.ScreenMargin),
        verticalArrangement = Arrangement.spacedBy(FiscusLayout.SectionSpacing),
    ) {
        CategoryHero(item = item)
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

private data class CategoryFact(@DrawableRes val icon: Int, val label: String, val value: String)

/** Kind, source (Default or Custom), transaction count and status (ADR-042). */
@Composable
private fun detailRows(item: CategoryWithUsage): List<CategoryFact> {
    val category = item.category
    return listOf(
        CategoryFact(
            category.kind.iconRes,
            stringResource(R.string.category_detail_kind),
            stringResource(category.kind.labelRes),
        ),
        CategoryFact(
            R.drawable.ic_category_rounded,
            stringResource(R.string.category_detail_origin),
            stringResource(
                if (category.isSeeded) {
                    R.string.category_seeded_short
                } else {
                    R.string.category_custom_short
                },
            ),
        ),
        CategoryFact(
            R.drawable.ic_receipt_long_rounded,
            stringResource(R.string.category_detail_transactions),
            item.transactionCount.toString(),
        ),
        CategoryFact(
            R.drawable.ic_archive_rounded,
            stringResource(R.string.category_detail_status),
            stringResource(
                if (category.archived) {
                    R.string.categories_archived
                } else {
                    R.string.categories_active
                },
            ),
        ),
    )
}

/**
 * The category on the detail-hero card family (ADR-025): `extraLarge` card in the kind's hero
 * pair, the category icon in a large [ShapeAccentIcon] with the kind's accent pair, the name in
 * `displayMedium` (calm, as on the account page, ADR-039), and "Expense · 3 transactions" below.
 * TalkBack reads it as one node.
 */
@Composable
private fun CategoryHero(item: CategoryWithUsage, modifier: Modifier = Modifier) {
    val category = item.category
    val hero = category.kind.heroRoles()
    val accent = category.kind.accentRoles()
    val line = listOf(
        stringResource(category.kind.labelRes),
        pluralStringResource(
            R.plurals.transaction_count,
            item.transactionCount,
            item.transactionCount,
        ),
    ).joinToString(stringResource(R.string.list_separator))
    Card(
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(
            containerColor = hero.container,
            contentColor = hero.content,
        ),
        modifier = modifier
            .fillMaxWidth()
            .clearAndSetSemantics { contentDescription = "${category.name}, $line" },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(FiscusLayout.SectionSpacing),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(FiscusLayout.ItemSpacing),
        ) {
            ShapeAccentIcon(
                icon = categoryIconRes(category.iconKey),
                containerColor = accent.container,
                contentColor = accent.content,
                size = FiscusLayout.LargeAccentSize,
                iconSize = FiscusLayout.LargeAccentIconSize,
                modifier = Modifier.padding(bottom = FiscusLayout.ItemSpacing),
            )
            Text(
                text = category.name,
                style = MaterialTheme.typography.displayMedium,
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
private fun CategoryDetailScreenPreview() {
    FiscusTheme(dynamicColor = false) {
        CategoryDetailScreen(
            uiState = CategoryDetailUiState(
                categoryId = 1,
                isLoading = false,
                category = CategoryWithUsage(
                    Category(1, "Food & dining", CategoryKind.EXPENSE, "restaurant", isSeeded = true),
                    transactionCount = 12,
                ),
            ),
            onEvent = {},
            onBack = {},
            onEdit = {},
        )
    }
}
