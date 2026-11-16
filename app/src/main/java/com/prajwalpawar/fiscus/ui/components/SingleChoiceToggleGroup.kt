package com.prajwalpawar.fiscus.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import com.prajwalpawar.fiscus.ui.theme.FiscusLayout
import com.prajwalpawar.fiscus.ui.theme.FiscusTheme


/**
 * Shared component: a titled single choice from an open-ended list, as Expressive [ToggleButton]s
 * (optional icon + label) in a wrapping grid, so every option is visible (ADR-018). The checked
 * button changes shape and fills with the primary role by itself; options are exposed to
 * accessibility as radio buttons. Shows [emptyMessage] when [options] is empty. With [iconOnly],
 * each button shows just its icon and [label] becomes its accessibility name (the icon picker).
 *
 * Use it for account, category, account type and similar choices (DESIGN_SYSTEM.md "Shared
 * components"). For 2–5 fixed options in a row, use the connected button group instead.
 */
@Composable
fun <T> SingleChoiceToggleGroup(
    title: String,
    options: List<T>,
    selectedKey: Long?,
    key: (T) -> Long,
    label: (T) -> String,
    onSelect: (Long) -> Unit,
    emptyMessage: String,
    modifier: Modifier = Modifier,
    /** Drawable resource for each option's icon, or null for text-only options. */
    icon: ((T) -> Int)? = null,
    iconOnly: Boolean = false,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(FiscusLayout.ItemSpacing),
    ) {
        Text(text = title, style = MaterialTheme.typography.titleSmall)
        if (options.isEmpty()) {
            Text(
                text = emptyMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(FiscusLayout.ItemSpacing),
                verticalArrangement = Arrangement.spacedBy(FiscusLayout.ItemSpacing),
            ) {
                options.forEach { option ->
                    val optionKey = key(option)
                    if (iconOnly && icon != null) {
                        ToggleButton(
                            checked = optionKey == selectedKey,
                            onCheckedChange = { onSelect(optionKey) },
                            modifier = Modifier.semantics { role = Role.RadioButton },
                        ) {
                            Icon(painterResource(icon(option)), contentDescription = label(option))
                        }
                    } else {
                        ToggleButton(
                            checked = optionKey == selectedKey,
                            onCheckedChange = { onSelect(optionKey) },
                            icon = icon?.let { res ->
                                { Icon(painterResource(res(option)), contentDescription = null) }
                            },
                            modifier = Modifier.semantics { role = Role.RadioButton },
                        ) {
                            Text(label(option), maxLines = 1)
                        }
                    }
                }
            }
        }
    }
}

private data class PreviewOption(val id: Long, val name: String, val iconKey: String)

@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "Large font", showBackground = true, fontScale = 2f)
@Composable
private fun SingleChoiceToggleGroupPreview() {
    val options = listOf(
        PreviewOption(1, "Food & dining", "restaurant"),
        PreviewOption(2, "Groceries", "local_grocery_store"),
        PreviewOption(3, "Transport", "directions_car"),
        PreviewOption(4, "Bills & utilities", "receipt_long"),
        PreviewOption(5, "Shopping", "shopping_bag"),
    )
    FiscusTheme(dynamicColor = false) {
        Surface {
            SingleChoiceToggleGroup(
                title = "Category",
                options = options,
                selectedKey = 2,
                key = PreviewOption::id,
                label = PreviewOption::name,
                onSelect = {},
                emptyMessage = "",
                icon = { categoryIconRes(it.iconKey) },
                modifier = Modifier.padding(FiscusLayout.ScreenMargin),
            )
        }
    }
}

@Preview(name = "Icon only, light", showBackground = true)
@Preview(
    name = "Icon only, dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Preview(name = "Icon only, large font", showBackground = true, fontScale = 2f)
@Composable
private fun SingleChoiceToggleGroupIconOnlyPreview() {
    val icons = CategoryIcons.take(8)
    FiscusTheme(dynamicColor = false) {
        Surface {
            SingleChoiceToggleGroup(
                title = "Icon",
                options = icons,
                selectedKey = 0,
                key = { icons.indexOf(it).toLong() },
                label = { it.key },
                onSelect = {},
                emptyMessage = "",
                icon = { it.iconRes },
                iconOnly = true,
                modifier = Modifier.padding(FiscusLayout.ScreenMargin),
            )
        }
    }
}

@Preview(name = "Empty", showBackground = true)
@Composable
private fun SingleChoiceToggleGroupEmptyPreview() {
    FiscusTheme(dynamicColor = false) {
        Surface {
            SingleChoiceToggleGroup(
                title = "To account",
                options = emptyList<PreviewOption>(),
                selectedKey = null,
                key = PreviewOption::id,
                label = PreviewOption::name,
                onSelect = {},
                emptyMessage = "Add another account with the same currency to make a transfer.",
                modifier = Modifier.padding(FiscusLayout.ScreenMargin),
            )
        }
    }
}
