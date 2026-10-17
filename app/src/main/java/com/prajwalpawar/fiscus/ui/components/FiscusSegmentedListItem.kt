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
    shapes: ListItemShapes,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    // Null keeps the row static: no ripple, and TalkBack doesn't call it a button.
    onClick: (() -> Unit)? = null,
    colors: ListItemColors = ListItemDefaults.colors(
        containerColor = MaterialTheme.colorScheme.surfaceContainer
    ),
    content: @Composable () -> Unit,
    supportingContent: (@Composable () -> Unit)? = null,
    overlineContent: (@Composable () -> Unit)? = null,
    leadingContent: (@Composable () -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null
) {
    if (onClick == null) {
        SegmentedListItem(
            shapes = shapes,
            modifier = modifier,
            colors = colors,
            content = content,
            supportingContent = supportingContent,
            overlineContent = overlineContent,
            leadingContent = leadingContent,
            trailingContent = trailingContent
        )
    } else {
        SegmentedListItem(
            selected = selected,
            onClick = onClick,
            shapes = shapes,
            modifier = modifier,
            colors = colors,
            content = content,
            supportingContent = supportingContent,
            overlineContent = overlineContent,
            leadingContent = leadingContent,
            trailingContent = trailingContent
        )
    }
}
