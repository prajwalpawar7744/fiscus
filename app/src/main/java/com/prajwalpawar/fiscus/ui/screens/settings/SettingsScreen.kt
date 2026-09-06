package com.prajwalpawar.fiscus.ui.screens.settings

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material3.CheckableDropdownMenuItem
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuGroup
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.DropdownMenuPopup
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalCursorBlinkEnabled
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.prajwalpawar.fiscus.data.model.AppBarPreference
import com.prajwalpawar.fiscus.data.model.AppBarScrollPreference
import com.prajwalpawar.fiscus.data.model.BottomBarPreference
import com.prajwalpawar.fiscus.data.model.ThemePreference
import com.prajwalpawar.fiscus.ui.components.FiscusDropdownMenu
import com.prajwalpawar.fiscus.ui.components.FiscusSegmentedListItem
import com.prajwalpawar.fiscus.ui.theme.FiscusSpacing
import kotlin.math.exp
import androidx.core.net.toUri

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
    val context = LocalContext.current

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = FiscusSpacing.md),
        verticalArrangement = Arrangement.spacedBy(FiscusSpacing.xs)
    ) {
        // Appearance
        item {
            SettingsSectionTitle("Appearance")
        }

        item {
            SettingsSelectionItem(
                label = "Theme",
                icon = Icons.Default.Palette,
                selectedValue = settings.theme,
                values = listOf(
                    ThemePreference.SYSTEM to "System",
                    ThemePreference.LIGHT to "Light",
                    ThemePreference.DARK to "Dark"
                ),
                index = 0,
                count = 4,
                onValueSelected = viewModel::setTheme
            )
        }

        item {
            SettingsSelectionItem(
                label = "App bar",
                icon = Icons.Default.ViewAgenda,
                selectedValue = settings.appBar,
                values = listOf(
                    AppBarPreference.SMALL to "Small",
                    AppBarPreference.MEDIUM to "Medium",
                    AppBarPreference.LARGE to "Large"
                ),
                index = 1,
                count = 4,
                onValueSelected = viewModel::setAppBar
            )
        }

        item {
            SettingsSelectionItem(
                label = "Bottom bar",
                icon = Icons.Default.ViewAgenda,
                selectedValue = settings.bottomBar,
                values = listOf(
                    BottomBarPreference.SHOW_LABELS to "Show labels",
                    BottomBarPreference.ICONS_ONLY to "Icons only",
                    BottomBarPreference.SHOW_LABELS_WHEN_SELECTED to "Show labels when selected"
                ),
                index = 2,
                count = 4,
                onValueSelected = viewModel::setBottomBar
            )
        }

        item {
            SettingsSelectionItem(
                label = "App bar scroll",
                icon = Icons.Default.SwapVert,
                selectedValue = settings.appBarScroll,
                values = listOf(
                    AppBarScrollPreference.PINNED to "Pinned",
                    AppBarScrollPreference.ENTER_ALWAYS to "Always returns",
                    AppBarScrollPreference.EXIT_UNTIL_COLLAPSED to "Collapse"
                ),
                index = 3,
                count = 4,
                onValueSelected = viewModel::setAppBarScroll
            )
        }

        // About
        item {
            SettingsSectionTitle("About")
        }

        item {
            FiscusSegmentedListItem(
                index = 0,
                count = 2,
                selected = false,
                onClick = {},
                leadingContent = {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null
                    )
                },
                content = {
                    Text(
                        text = "Developer",
                        style = MaterialTheme.typography.bodyLarge
                    )
                },
                supportingContent = {
                    Text(
                        text = "Prajwal Pawar",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            )
        }

        item {
            FiscusSegmentedListItem(
                index = 1,
                count = 2,
                selected = false,
                leadingContent = {
                    Icon(
                        imageVector = Icons.Default.Code,
                        contentDescription = null
                    )
                },
                onClick = {
                    context.startActivity(
                        Intent(
                            Intent.ACTION_VIEW,
                            "https://github.com/prajwalpawar7744/fiscus".toUri()
                        )
                    )
                },
                content = {
                    Text(
                        text = "GitHub",
                        style = MaterialTheme.typography.bodyLarge
                    )
                },
                supportingContent = {
                    Text(
                        text = "View source code",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T> SettingsSelectionItem(
    label: String,
    icon: ImageVector,
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
        FiscusSegmentedListItem(
            index = index,
            count = count,
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
            leadingContent = {
                Icon(
                    imageVector = icon,
                    contentDescription = null
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

        FiscusDropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                expanded = false
            },
            items = values,
            selectedItem = values.first { it.first == selectedValue },
            onItemSelected = { selectedPair ->
                onValueSelected(selectedPair.first)
                expanded = false
            },
            itemLabel = { pair ->
                Text(
                    text = pair.second,
                    style = MaterialTheme.typography.bodyLarge
                )
            },
            offset = DpOffset(
                x = 0.dp,
                y = FiscusSpacing.xs
            )
        )
    }
}

@Composable
private fun SettingsSectionTitle(
    title: String
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(
            top = FiscusSpacing.md,
            bottom = FiscusSpacing.xs
        )
    )
}