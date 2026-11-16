package com.prajwalpawar.fiscus.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import com.prajwalpawar.fiscus.ui.theme.FiscusLayout
import com.prajwalpawar.fiscus.ui.theme.FiscusTheme

/**
 * Shared component: an on/off setting in a form, the M3 [Switch] at the end of a full-width row.
 * The whole row toggles (one touch target, at least 48dp, `Role.Switch`), so the switch itself
 * has no click. Optional [supportingText] in `bodyMedium`/`onSurfaceVariant` below the label.
 */
@Composable
fun SwitchRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    supportingText: String? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .minimumInteractiveComponentSize()
            .toggleable(value = checked, role = Role.Switch, onValueChange = onCheckedChange)
            .padding(horizontal = FiscusLayout.ScreenMargin),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = label, style = MaterialTheme.typography.bodyLarge)
            supportingText?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Switch(checked = checked, onCheckedChange = null)
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
