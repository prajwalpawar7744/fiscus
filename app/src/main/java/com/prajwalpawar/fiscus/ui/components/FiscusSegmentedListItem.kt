package com.prajwalpawar.fiscus.ui.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItemColors
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.ListItemShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedListItem
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FiscusSegmentedListItem(
    index: Int,
    count: Int,
    selected: Boolean = false,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    shapes: ListItemShapes = ListItemDefaults.segmentedShapes(
        index = index,
        count = count
    ),
    colors: ListItemColors = ListItemDefaults.colors(
        containerColor = MaterialTheme.colorScheme.surfaceContainer
    ),
    content: @Composable () -> Unit,
    supportingContent: (@Composable () -> Unit)? = null,
    overlineContent: (@Composable () -> Unit)? = null,
    leadingContent: (@Composable () -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null
) {
    SegmentedListItem(
        modifier = modifier,
        shapes = shapes,
        colors = colors,
        selected = selected,
        onClick = onClick ?: {},
        content = content,
        supportingContent = supportingContent,
        overlineContent = overlineContent,
        leadingContent = leadingContent,
        trailingContent = trailingContent
    )
}