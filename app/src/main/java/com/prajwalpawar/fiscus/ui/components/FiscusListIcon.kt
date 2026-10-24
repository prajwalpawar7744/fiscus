package com.prajwalpawar.fiscus.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.prajwalpawar.fiscus.ui.theme.FiscusShapes
import com.prajwalpawar.fiscus.ui.theme.FiscusSize

// Leading icon for list rows, set in a small tonal shape (Android 16 settings style).
@Composable
fun FiscusListIcon(
    icon: ImageVector,
    modifier: Modifier = Modifier,
    // A colour role and its on colour, as a pair.
    containerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onPrimaryContainer
) {
    Box(
        modifier = modifier
            .size(FiscusSize.listIconContainer)
            .background(
                color = containerColor,
                shape = FiscusShapes.icon
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = contentColor
        )
    }
}
