package com.prajwalpawar.fiscus.ui.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable

// M3 plain tooltip naming an icon-only button's action, shown on long-press (as the
// official samples do for icon buttons). Pass the same text as the icon's
// contentDescription. The tooltip is on top of the button's own TalkBack label.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FiscusTooltip(
    label: String,
    content: @Composable () -> Unit
) {
    TooltipBox(
        positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Above),
        tooltip = { PlainTooltip { Text(label) } },
        state = rememberTooltipState(),
        content = content
    )
}
