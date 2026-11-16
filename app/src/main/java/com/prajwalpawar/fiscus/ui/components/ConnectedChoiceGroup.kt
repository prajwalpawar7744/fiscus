package com.prajwalpawar.fiscus.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.prajwalpawar.fiscus.model.TransactionType
import com.prajwalpawar.fiscus.ui.theme.FiscusLayout
import com.prajwalpawar.fiscus.ui.theme.FiscusTheme

/**
 * Shared component: one choice from **2–5 fixed options** as the M3 Expressive connected button
 * group (ADR-018 3b, ADR-036): a `Row` of [ToggleButton]s with connected shapes that change on
 * press and selection, radio semantics, equal widths. For open-ended lists use
 * [SingleChoiceToggleGroup].
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class) // connected button shapes
@Composable
fun <T> ConnectedChoiceGroup(
    options: List<T>,
    selected: T?,
    onSelect: (T) -> Unit,
    label: @Composable (T) -> String,
    modifier: Modifier = Modifier,
    icon: ((T) -> Int?)? = null, // a drawable res, or null for none
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
    ) {
        options.forEachIndexed { index, option ->
            ToggleButton(
                checked = option == selected,
                onCheckedChange = { onSelect(option) },
                shapes = when (index) {
                    0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                    options.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                    else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                },
                modifier = Modifier
                    .weight(1f)
                    .semantics { role = Role.RadioButton },
            ) {
                icon?.invoke(option)?.let { res ->
                    Icon(painterResource(res), contentDescription = null)
                    Spacer(Modifier.size(ToggleButtonDefaults.IconSpacing))
                }
                Text(label(option), maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "Large font", showBackground = true, fontScale = 2f)
@Composable
private fun ConnectedChoiceGroupPreview() {
    FiscusTheme(dynamicColor = false) {
        Surface {
            ConnectedChoiceGroup(
                options = TransactionTypeDisplayOrder,
                selected = TransactionType.EXPENSE,
                onSelect = {},
                label = { stringResource(it.labelRes) },
                icon = { it.iconRes },
                modifier = Modifier.padding(FiscusLayout.ScreenMargin),
            )
        }
    }
}
