package com.prajwalpawar.fiscus.ui.components

import androidx.compose.animation.Crossfade
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector

// Leading icon for a filter chip: crossfades to a check when selected.
@Composable
fun FiscusChipIcon(
    selected: Boolean,
    icon: ImageVector
) {
    Crossfade(
        targetState = selected,
        animationSpec = MaterialTheme.motionScheme.fastEffectsSpec(),
        label = "chipIcon"
    ) { isSelected ->
        Icon(
            imageVector = if (isSelected) Icons.Filled.Check else icon,
            contentDescription = null
        )
    }
}
