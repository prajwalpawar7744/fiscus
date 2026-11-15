package com.prajwalpawar.fiscus.ui.components

import androidx.compose.material3.ListItemColors
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

/**
 * Colors for [androidx.compose.material3.SegmentedListItem]s that sit directly on the screen
 * background. The default segmented container is `surface`, the same as the screen, so groups
 * would have no visible containment; `surfaceContainer` is the next surface tier up (M3 builds
 * hierarchy with surface-container tiers, not shadows; ADR-025).
 */
@Composable
fun containedSegmentedColors(): ListItemColors =
    ListItemDefaults.segmentedColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
