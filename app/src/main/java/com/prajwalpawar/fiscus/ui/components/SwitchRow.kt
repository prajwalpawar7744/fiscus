package com.prajwalpawar.fiscus.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.ListItemShapes
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import com.prajwalpawar.fiscus.ui.theme.FiscusTheme

/**
 * Shared component: an on/off setting in a form, a contained, toggleable M3 [SegmentedListItem]
 * with the M3 [Switch] as trailing content (like [DetailRow]). The whole item toggles (one touch
 * target, `Role.Switch`), so the switch itself has no click. Optional [supportingText] below the
 * label. Pass [shapes] from `ListItemDefaults.segmentedShapes(index, count)` when rows are grouped.
 */
@Composable
fun SwitchRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    supportingText: String? = null,
    shapes: ListItemShapes = ListItemDefaults.segmentedShapes(index = 0, count = 1),
) {
    SegmentedListItem(
        checked = checked,
        onCheckedChange = onCheckedChange,
        shapes = shapes,
        colors = containedSegmentedColors(),
        modifier = modifier.semantics { role = Role.Switch },
        supportingContent = supportingText?.let { { Text(it) } },
        trailingContent = { Switch(checked = checked, onCheckedChange = null) },
    ) {
        Text(label)
    }
}

@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "Large font", showBackground = true, fontScale = 2f)
@Composable
private fun SwitchRowPreview() {
    FiscusTheme(dynamicColor = false) {
        Surface {
            Column {
                SwitchRow("In debt (balance below zero)", checked = false, onCheckedChange = {})
                SwitchRow(
                    "Default account",
                    checked = true,
                    onCheckedChange = {},
                    supportingText = "New transactions start on this account",
                )
            }
        }
    }
}
