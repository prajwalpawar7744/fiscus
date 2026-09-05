package com.prajwalpawar.fiscus.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalCursorBlinkEnabled
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.prajwalpawar.fiscus.data.model.AppBarPreference
import com.prajwalpawar.fiscus.data.model.BottomBarPreference
import com.prajwalpawar.fiscus.data.model.ThemePreference
import com.prajwalpawar.fiscus.ui.theme.FiscusSpacing
import kotlin.math.exp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = viewModel(
        factory = SettingsViewModelFactory(
            context = LocalContext.current
        )
    )
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = FiscusSpacing.md),
        verticalArrangement = Arrangement.spacedBy(FiscusSpacing.xs)
    ) {
        item {
            Text(
                text = "Appearance",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(
                    top = FiscusSpacing.md,
                    bottom = FiscusSpacing.xs
                )
            )
        }

        item {
            SettingsItem(
                label = "Theme",
                selectedValue = settings.theme,
                values = listOf(
                    ThemePreference.SYSTEM to "System",
                    ThemePreference.LIGHT to "Light",
                    ThemePreference.DARK to "Dark"
                ),
                index = 0,
                count = 3,
                onValueSelected = viewModel::setTheme
            )
        }

        item {
            SettingsItem(
                label = "App bar",
                selectedValue = settings.appBar,
                values = listOf(
                    AppBarPreference.SMALL to "Small",
                    AppBarPreference.MEDIUM to "Medium",
                    AppBarPreference.LARGE to "Large"
                ),
                index = 1,
                count = 3,
                onValueSelected = viewModel::setAppBar
            )
        }

        item {
            SettingsItem(
                label = "Bottom bar",
                selectedValue = settings.bottomBar,
                values = listOf(
                    BottomBarPreference.SHOW_LABELS to "Show labels",
                    BottomBarPreference.ICONS_ONLY to "Icons only",
                    BottomBarPreference.SHOW_LABELS_WHEN_SELECTED to "Show labels when selected"
                ),
                index = 2,
                count = 3,
                onValueSelected = viewModel::setBottomBar
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T> SettingsItem(
    label: String,
    selectedValue: T,
    values: List<Pair<T, String>>,
    index: Int,
    count: Int,
    onValueSelected: (T) -> Unit
) {
    var expanded by remember {
        mutableStateOf(false)
    }

    val selectedText = values
        .firstOrNull {
            it.first == selectedValue
        }
        ?.second
        .orEmpty()

    Column {
        SegmentedListItem(
            shapes = ListItemDefaults.segmentedShapes(
                index = index,
                count = count
            ),
            colors = ListItemDefaults.colors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            ),
            selected = expanded,
            onClick = {
                expanded = !expanded
            },
           content = {
               Text(
                   text = label,
                   style = MaterialTheme.typography.bodyLarge
               )
           },
            supportingContent = {
                Text(
                    text = selectedText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            trailingContent = {
                Icon(
                    imageVector = if (expanded) {
                        Icons.Default.KeyboardArrowUp
                    } else {
                        Icons.Default.KeyboardArrowDown
                    },
                    contentDescription = if (expanded) {
                        "Hide $label options"
                    } else {
                        "Show $label options"
                    },
                )
            }
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                expanded = false
            }
        ) {
            values.forEach { (value, text) ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = text,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    },
                    leadingIcon = {
                        if (value == selectedValue) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null
                            )
                        }
                    },
                    onClick = {
                        onValueSelected(value)
                        expanded = false
                    }
                )
            }
        }
    }
}
