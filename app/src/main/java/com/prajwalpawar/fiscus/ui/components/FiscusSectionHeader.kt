package com.prajwalpawar.fiscus.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics

// One header style for every grouped section in the app.
@Composable
fun FiscusSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    // Primary pairs with surface roles. On a coloured container pass its "on" colour
    // instead (M3 colour pairing), e.g. onSecondaryContainer.
    color: Color = MaterialTheme.colorScheme.primary
) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = color,
        modifier = modifier.semantics {
            heading()
        }
    )
}
