package com.prajwalpawar.fiscus.ui.screens.manage

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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.graphics.shapes.RoundedPolygon
import com.prajwalpawar.fiscus.ui.components.FiscusSubmitButton
import com.prajwalpawar.fiscus.ui.theme.FiscusMotion
import com.prajwalpawar.fiscus.ui.theme.FiscusSize
import com.prajwalpawar.fiscus.ui.theme.FiscusSpacing

// Shared layout for "new item" pages: a live preview of the item on an Expressive
// shape, the form fields, then the save button.
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun NewItemLayout(
    previewIcon: ImageVector,
    previewName: String,
    namePlaceholder: String,
    previewShape: RoundedPolygon,
    saveLabel: String,
    canSave: Boolean,
    onSave: () -> Unit,
    modifier: Modifier = Modifier,
    fields: @Composable ColumnScope.() -> Unit
) {
    val motionScheme = MaterialTheme.motionScheme

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(FiscusSpacing.md),
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
                        shape = previewShape.toShape()
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Pop the new icon in when the choice changes.
                AnimatedContent(
                    targetState = previewIcon,
                    transitionSpec = {
                        (fadeIn(motionScheme.defaultEffectsSpec()) +
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

        FiscusSubmitButton(
            text = saveLabel,
            icon = Icons.Filled.Check,
            enabled = canSave,
            onClick = onSave
        )
    }
}
