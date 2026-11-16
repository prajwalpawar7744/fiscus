package com.prajwalpawar.fiscus.ui.feature.categories

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
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.prajwalpawar.fiscus.R
import com.prajwalpawar.fiscus.model.Category
import com.prajwalpawar.fiscus.model.CategoryKind
import com.prajwalpawar.fiscus.model.CategoryWithUsage
import com.prajwalpawar.fiscus.ui.components.CategoryKindDisplayOrder
import com.prajwalpawar.fiscus.ui.components.ConnectedChoiceGroup
import com.prajwalpawar.fiscus.ui.components.EmptyState
import com.prajwalpawar.fiscus.ui.components.LocalSnackbarHostState
import com.prajwalpawar.fiscus.ui.components.ShapeAccentIcon
import com.prajwalpawar.fiscus.ui.components.accentRoles
import com.prajwalpawar.fiscus.ui.components.categoryContainerKey
import com.prajwalpawar.fiscus.ui.components.categoryIconRes
import com.prajwalpawar.fiscus.ui.components.containedSegmentedColors
import com.prajwalpawar.fiscus.ui.components.iconRes
import com.prajwalpawar.fiscus.ui.components.labelRes
import com.prajwalpawar.fiscus.ui.components.sharedContainer
import com.prajwalpawar.fiscus.ui.theme.FiscusLayout
import com.prajwalpawar.fiscus.ui.theme.FiscusTheme

/**
 * Stateless Categories page (ADR-040), built like the Accounts page (ADR-036): a connected choice
 * of [Expense | Income], then that kind's active categories and archived ones, each as one
 * contained segmented group. A row grows into the category's page (ADR-028, ADR-042). Not a
 * hero moment: standard M3, calm data. "Add category" is an extended FAB that collapses to its
 * icon while scrolling down.
 */
@OptIn(
    ExperimentalMaterial3Api::class, // TopAppBarDefaults scroll behavior
    ExperimentalMaterial3ExpressiveApi::class, // LoadingIndicator
)
@Composable
fun CategoriesScreen(
    uiState: CategoriesUiState,
    onEvent: (CategoriesEvent) -> Unit,
    onBack: () -> Unit,
    onOpenCategory: (Long) -> Unit,
    onAddCategory: () -> Unit,
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
                title = { Text(stringResource(R.string.categories_title)) },
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
                text = { Text(stringResource(R.string.categories_add)) },
                icon = {
                    Icon(painterResource(R.drawable.ic_add_rounded), contentDescription = null)
                },
                onClick = onAddCategory,
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
            label = "categoriesLoading",
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
                        item(key = "kind") {
                            ConnectedChoiceGroup(
                                options = CategoryKindDisplayOrder,
                                selected = uiState.kind,
                                onSelect = { onEvent(CategoriesEvent.KindSelected(it)) },
                                label = { stringResource(it.labelRes) },
                                icon = { it.iconRes },
                                modifier = Modifier.padding(bottom = FiscusLayout.TightSpacing),
                            )
                        }
                        if (uiState.active.isEmpty() && uiState.archived.isEmpty()) {
                            item(key = "empty") {
                                EmptyState(
                                    icon = R.drawable.ic_category_rounded,
                                    message = stringResource(R.string.categories_empty),
                                )
                            }
                        }
                        categoryGroup(R.string.categories_active, uiState.active, onOpenCategory)
                        categoryGroup(
                            R.string.categories_archived,
                            uiState.archived,
                            onOpenCategory,
                        )
                    }
                }
            }
        }
    }
}

/** A titled group of category rows; nothing when [categories] is empty. */
private fun LazyListScope.categoryGroup(
    title: Int,
    categories: List<CategoryWithUsage>,
    onOpenCategory: (Long) -> Unit,
) {
    if (categories.isEmpty()) return
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
    itemsIndexed(categories, key = { _, item -> "category-${item.category.id}" }) { index, item ->
        CategoryRow(
            item = item,
            index = index,
            count = categories.size,
            onClick = { onOpenCategory(item.category.id) },
        )
    }
}

/**
 * One category: its icon in a [ShapeAccentIcon] with the kind's accent pair, its name, and
 * "Expense · Default · 3 transactions" (ADR-041).
 */
@Composable
private fun CategoryRow(item: CategoryWithUsage, index: Int, count: Int, onClick: () -> Unit) {
    val category = item.category
    val accent = category.kind.accentRoles()
    SegmentedListItem(
        onClick = onClick,
        shapes = ListItemDefaults.segmentedShapes(index, count),
        colors = containedSegmentedColors(),
        modifier = Modifier.sharedContainer(categoryContainerKey(category.id)),
        leadingContent = {
            ShapeAccentIcon(
                icon = categoryIconRes(category.iconKey),
                containerColor = accent.container,
                contentColor = accent.content,
            )
        },
        supportingContent = { Text(categorySupportingText(item)) },
    ) {
        Text(
            text = category.name,
            style = MaterialTheme.typography.titleMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/** Kind, then "Default" (starter) or "Custom" (user-added), then the count (ADR-041). */
@Composable
private fun categorySupportingText(item: CategoryWithUsage): String = listOf(
    stringResource(item.category.kind.labelRes),
    stringResource(
        if (item.category.isSeeded) {
            R.string.category_seeded_short
        } else {
            R.string.category_custom_short
        },
    ),
    pluralStringResource(R.plurals.transaction_count, item.transactionCount, item.transactionCount),
).joinToString(stringResource(R.string.list_separator))

@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "Large font", showBackground = true, fontScale = 2f)
@Composable
private fun CategoriesScreenPreview() {
    val food = Category(1, "Food & dining", CategoryKind.EXPENSE, "restaurant", isSeeded = true)
    FiscusTheme(dynamicColor = false) {
        CategoriesScreen(
            uiState = CategoriesUiState(
                isLoading = false,
                active = listOf(
                    CategoryWithUsage(food, 12),
                    CategoryWithUsage(
                        food.copy(id = 2, name = "Groceries", iconKey = "shopping_cart"),
                        1,
                    ),
                    CategoryWithUsage(
                        food.copy(
                            id = 3,
                            name = "Coffee",
                            iconKey = "local_cafe",
                            isSeeded = false,
                        ),
                        0,
                    ),
                ),
                archived = listOf(
                    CategoryWithUsage(
                        food.copy(
                            id = 4,
                            name = "Gym",
                            iconKey = "fitness_center",
                            archived = true,
                            isSeeded = false,
                        ),
                        4,
                    ),
                ),
            ),
            onEvent = {},
            onBack = {},
            onOpenCategory = {},
            onAddCategory = {},
        )
    }
}
