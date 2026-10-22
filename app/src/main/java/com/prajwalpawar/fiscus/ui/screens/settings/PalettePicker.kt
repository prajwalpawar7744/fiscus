package com.prajwalpawar.fiscus.ui.screens.settings

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ColorLens
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItemShapes
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.prajwalpawar.fiscus.data.model.PalettePreference
import com.prajwalpawar.fiscus.ui.components.FiscusListIcon
import com.prajwalpawar.fiscus.ui.components.FiscusSegmentedListItem
import com.prajwalpawar.fiscus.ui.theme.FiscusSize
import com.prajwalpawar.fiscus.ui.theme.FiscusSpacing
import com.prajwalpawar.fiscus.ui.theme.colorScheme

private fun PalettePreference.label(): String = when (this) {
    PalettePreference.MONOCHROME -> "Monochrome"
    PalettePreference.BLUE -> "Blue"
    PalettePreference.GREEN -> "Green"
    PalettePreference.PURPLE -> "Purple"
    PalettePreference.ROSE -> "Rose"
    PalettePreference.AMBER -> "Amber"
}

// Colour palettes for when Dynamic color is off. M3 has no swatch picker component, so
// this follows Android's own wallpaper & style picker: one swatch per palette showing its
// primary, secondary and tertiary colours; the chosen one morphs to a cookie with a check.
@Composable
internal fun PaletteRow(
    selected: PalettePreference,
    shapes: ListItemShapes,
    onSelected: (PalettePreference) -> Unit
) {
    FiscusSegmentedListItem(
        shapes = shapes,
        leadingContent = { FiscusListIcon(icon = Icons.Rounded.ColorLens) },
        content = {
            Text(text = "Palette", style = MaterialTheme.typography.bodyLarge)
        },
        supportingContent = {
            Column(verticalArrangement = Arrangement.spacedBy(FiscusSpacing.sm)) {
                Text(
                    text = selected.label(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                FlowRow(
                    modifier = Modifier.selectableGroup(),
                    horizontalArrangement = Arrangement.spacedBy(FiscusSpacing.sm),
                    verticalArrangement = Arrangement.spacedBy(FiscusSpacing.sm)
                ) {
                    PalettePreference.entries.forEach { palette ->
                        PaletteSwatch(
                            palette = palette,
                            selected = palette == selected,
                            onClick = { onSelected(palette) }
                        )
                    }
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun PaletteSwatch(
    palette: PalettePreference,
    selected: Boolean,
    onClick: () -> Unit
) {
    // Shows each palette as it looks in the current light/dark mode.
    val scheme = palette.colorScheme(darkTheme = MaterialTheme.colorScheme.surface.luminance() < 0.5f)
    val shape = if (selected) MaterialShapes.Cookie9Sided.toShape() else MaterialShapes.Circle.toShape()
    val scale by animateFloatAsState(
        targetValue = if (selected) 1f else 0.9f,
        animationSpec = MaterialTheme.motionScheme.fastSpatialSpec(),
        label = "swatchScale"
    )

    Box(
        modifier = Modifier
            .size(FiscusSize.paletteSwatch)
            .scale(scale)
            .clip(shape)
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
            .semantics { contentDescription = palette.label() },
        contentAlignment = Alignment.Center
    ) {
        // Top half primary; bottom halves secondary and tertiary.
        Column(Modifier.fillMaxSize()) {
            Box(Modifier.fillMaxWidth().weight(1f).background(scheme.primary))
            Row(Modifier.fillMaxWidth().weight(1f)) {
                Box(Modifier.weight(1f).fillMaxSize().background(scheme.secondaryContainer))
                Box(Modifier.weight(1f).fillMaxSize().background(scheme.tertiaryContainer))
            }
        }

        if (selected) {
            Box(
                modifier = Modifier
                    .size(FiscusSize.paletteSwatchCheck)
                    .background(scheme.primaryContainer, MaterialShapes.Circle.toShape()),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = null,
                    tint = scheme.onPrimaryContainer,
                    modifier = Modifier.size(FiscusSize.paletteSwatchCheckIcon)
                )
            }
        }
    }
}
