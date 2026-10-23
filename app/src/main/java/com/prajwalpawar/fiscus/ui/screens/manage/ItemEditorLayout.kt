package com.prajwalpawar.fiscus.ui.screens.manage

import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.WindowInsets
import com.prajwalpawar.fiscus.ui.theme.FiscusReducedMotion
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import com.prajwalpawar.fiscus.ui.components.FiscusSubmitButton
import com.prajwalpawar.fiscus.ui.theme.FiscusMotion
import com.prajwalpawar.fiscus.ui.theme.FiscusShapes
import com.prajwalpawar.fiscus.ui.theme.FiscusSize
import com.prajwalpawar.fiscus.ui.theme.FiscusSpacing

// Shared layout for the category and account editors: a live preview of the item on an Expressive
// shape, the form fields, then the save button.
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ItemEditorLayout(
    previewIcon: ImageVector,
    previewName: String,
    namePlaceholder: String,
    saveLabel: String,
    canSave: Boolean,
    onSave: () -> Unit,
    modifier: Modifier = Modifier,
    // Set when editing an existing item: its actions move to a floating toolbar (Save as the
    // FAB, Archive/Delete inside), as on the transaction details page. Null for a new item,
    // which keeps the single full-width save button.
    manage: ItemManageState? = null,
    // "category" or "account", for the toolbar's messages.
    itemNoun: String = "",
    onArchiveToggle: () -> Unit = {},
    onDelete: () -> Unit = {},
    fields: @Composable ColumnScope.() -> Unit
) {
    val motionScheme = MaterialTheme.motionScheme

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(
                    start = FiscusSpacing.md,
                    top = FiscusSpacing.md,
                    end = FiscusSpacing.md,
                    // Room for the floating toolbar, so the last field isn't hidden behind it.
                    bottom = if (manage != null) {
                        FloatingToolbarDefaults.ContainerSize + FloatingToolbarDefaults.ScreenOffset * 2
                    } else {
                        FiscusSpacing.md
                    }
                ),
            verticalArrangement = Arrangement.spacedBy(FiscusSpacing.lg)
        ) {
            // Decorative preview; the fields below carry the real labels.
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(FiscusSpacing.md)
            ) {
                Box(
                    modifier = Modifier
                        .size(FiscusSize.emptyStateShape)
                        .background(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = FiscusShapes.icon
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    // Pop the new icon in when the choice changes (a fade with reduced motion).
                    val reducedMotion = FiscusReducedMotion.enabled
                    AnimatedContent(
                        targetState = previewIcon,
                        transitionSpec = {
                            if (reducedMotion) {
                                fadeIn(motionScheme.defaultEffectsSpec()) togetherWith
                                    fadeOut(motionScheme.fastEffectsSpec())
                            } else (fadeIn(motionScheme.defaultEffectsSpec()) +
                                    scaleIn(
                                        animationSpec = motionScheme.defaultSpatialSpec(),
                                        initialScale = FiscusMotion.ENTER_SCALE
                                    )) togetherWith fadeOut(motionScheme.fastEffectsSpec())
                        },
                        label = "previewIcon"
                    ) { icon ->
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(FiscusSize.emptyStateIcon)
                        )
                    }
                }

                Text(
                    text = previewName.ifBlank { namePlaceholder },
                    style = MaterialTheme.typography.headlineMediumEmphasized,
                    color = if (previewName.isBlank()) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                    textAlign = TextAlign.Center
                )
            }

            fields()

            if (manage == null) {
                FiscusSubmitButton(
                    text = saveLabel,
                    icon = Icons.Rounded.Check,
                    enabled = canSave,
                    onClick = onSave
                )
            } else {
                ItemManageHint(state = manage, itemNoun = itemNoun)
            }
        }

        if (manage != null) {
            ItemManageToolbar(
                state = manage,
                itemNoun = itemNoun,
                onSave = onSave,
                onArchiveToggle = onArchiveToggle,
                onDelete = onDelete,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    // Above the keyboard while typing (the screen already pads the nav bar).
                    .windowInsetsPadding(WindowInsets.ime.exclude(WindowInsets.navigationBars))
                    .padding(bottom = FloatingToolbarDefaults.ScreenOffset)
            )
        }
    }
}
