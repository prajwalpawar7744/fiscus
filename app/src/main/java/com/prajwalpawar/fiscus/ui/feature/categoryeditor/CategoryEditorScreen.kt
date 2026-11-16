package com.prajwalpawar.fiscus.ui.feature.categoryeditor

import android.content.res.Configuration
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.ui.tooling.preview.Preview
import com.prajwalpawar.fiscus.R
import com.prajwalpawar.fiscus.model.CategoryKind
import com.prajwalpawar.fiscus.ui.components.ActionButtonGroup
import com.prajwalpawar.fiscus.ui.components.CategoryIcons
import com.prajwalpawar.fiscus.ui.components.CategoryKindDisplayOrder
import com.prajwalpawar.fiscus.ui.components.ConnectedChoiceGroup
import com.prajwalpawar.fiscus.ui.components.GroupAction
import com.prajwalpawar.fiscus.ui.components.LocalSnackbarHostState
import com.prajwalpawar.fiscus.ui.components.SaveHint
import com.prajwalpawar.fiscus.ui.components.SingleChoiceToggleGroup
import com.prajwalpawar.fiscus.ui.components.iconRes
import com.prajwalpawar.fiscus.ui.components.labelRes
import com.prajwalpawar.fiscus.ui.theme.FiscusLayout
import com.prajwalpawar.fiscus.ui.theme.FiscusTheme

/**
 * Stateless add/edit category form (ADR-040). Not a hero moment (ADR-010): a standard M3 form with
 * the name, the kind (connected button group, locked once transactions use the category) and the
 * icon (icon-only toggle buttons). Save sits in the shared action group within thumb reach
 * (ADR-028); archive and delete are on the category's page (ADR-042), as for accounts.
 */
@OptIn(
    ExperimentalMaterial3Api::class, // TopAppBarDefaults scroll behavior
    ExperimentalMaterial3ExpressiveApi::class, // LoadingIndicator
)
@Composable
fun CategoryEditorScreen(
    uiState: CategoryEditorUiState,
    onEvent: (CategoryEditorEvent) -> Unit,
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
                                R.string.category_editor_title_add
                            } else {
                                R.string.category_editor_title_edit
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
                                label = stringResource(R.string.category_save),
                                onClick = { onEvent(CategoryEditorEvent.Save) },
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
            label = "categoryEditorLoading",
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                // As in the other editors: the action bar is already reserved, so imePadding()
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
                    CategoryForm(
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
private fun CategoryForm(
    uiState: CategoryEditorUiState,
    onEvent: (CategoryEditorEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val motion = MaterialTheme.motionScheme
    val margin = Modifier.padding(horizontal = FiscusLayout.ScreenMargin)
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
            onValueChange = { onEvent(CategoryEditorEvent.NameChanged(it)) },
            label = { Text(stringResource(R.string.category_name)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Done,
            ),
            modifier = Modifier.fillMaxWidth().then(margin),
        )
        Column(verticalArrangement = Arrangement.spacedBy(FiscusLayout.ItemSpacing)) {
            Text(
                text = stringResource(R.string.category_kind),
                style = MaterialTheme.typography.titleSmall,
                modifier = margin,
            )
            ConnectedChoiceGroup(
                options = CategoryKindDisplayOrder,
                selected = uiState.kind,
                onSelect = { onEvent(CategoryEditorEvent.KindChanged(it)) },
                label = { stringResource(it.labelRes) },
                icon = { it.iconRes },
                enabled = !uiState.isKindLocked,
                modifier = margin,
            )
            if (uiState.isKindLocked) {
                Text(
                    text = stringResource(R.string.category_kind_locked),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = margin,
                )
            }
        }
        // The picker's options are indexes into CategoryIcons; labels are their a11y names.
        val iconLabels = CategoryIcons.map { stringResource(it.labelRes) }
        SingleChoiceToggleGroup(
            title = stringResource(R.string.category_icon),
            options = CategoryIcons.indices.toList(),
            selectedKey = CategoryIcons.indexOfFirst { it.key == uiState.iconKey }.toLong(),
            key = { it.toLong() },
            label = { iconLabels[it] },
            onSelect = { index ->
                onEvent(CategoryEditorEvent.IconChanged(CategoryIcons[index.toInt()].key))
            },
            emptyMessage = "",
            icon = { CategoryIcons[it].iconRes },
            iconOnly = true,
            modifier = margin,
        )
    }
}

@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "Large font", showBackground = true, fontScale = 2f)
@Composable
private fun CategoryEditorScreenPreview() {
    FiscusTheme(dynamicColor = false) {
        CategoryEditorScreen(
            uiState = CategoryEditorUiState(
                isLoading = false,
                isNew = false,
                name = "Food & dining",
                kind = CategoryKind.EXPENSE,
                iconKey = "restaurant",
                transactionCount = 3,
            ),
            onEvent = {},
            onBack = {},
        )
    }
}
