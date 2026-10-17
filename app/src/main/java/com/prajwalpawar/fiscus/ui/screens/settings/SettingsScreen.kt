package com.prajwalpawar.fiscus.ui.screens.settings

import android.content.Intent
import android.os.Build
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CallToAction
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Interests
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material.icons.filled.WebAsset
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.ListItemShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.DpOffset
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.prajwalpawar.fiscus.data.model.AppBarPreference
import com.prajwalpawar.fiscus.data.model.AppBarScrollPreference
import com.prajwalpawar.fiscus.data.model.BottomBarPreference
import com.prajwalpawar.fiscus.data.model.IconShapePreference
import com.prajwalpawar.fiscus.data.model.ThemePreference
import com.prajwalpawar.fiscus.ui.components.FiscusDropdownMenu
import com.prajwalpawar.fiscus.ui.components.FiscusListIcon
import com.prajwalpawar.fiscus.ui.components.FiscusSectionHeader
import com.prajwalpawar.fiscus.ui.components.FiscusSegmentedListItem
import com.prajwalpawar.fiscus.ui.theme.FiscusSpacing

// A settings row; it receives the corner shapes for its position in the section.
private typealias SettingsRow = @Composable (shapes: ListItemShapes) -> Unit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onCategoriesClick: () -> Unit,
    onAccountsClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = viewModel(
        factory = SettingsViewModelFactory(
            context = LocalContext.current
        )
    )
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Wallpaper-based color only exists on Android 12+.
    val dynamicColorSupported = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

    val appearanceRows = listOfNotNull<SettingsRow>(
        { shapes ->
            SettingsSelectionItem(
                label = "Theme",
                icon = Icons.Default.Palette,
                selectedValue = settings.theme,
                values = listOf(
                    ThemePreference.SYSTEM to "System",
                    ThemePreference.LIGHT to "Light",
                    ThemePreference.DARK to "Dark"
                ),
                shapes = shapes,
                onValueSelected = viewModel::setTheme
            )
        },
        if (dynamicColorSupported) {
            { shapes ->
                SegmentedListItem(
                    checked = settings.dynamicColor,
                    onCheckedChange = viewModel::setDynamicColor,
                    shapes = shapes,
                    colors = ListItemDefaults.segmentedColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                    ),
                    leadingContent = {
                        FiscusListIcon(icon = Icons.Default.Wallpaper)
                    },
                    supportingContent = {
                        Text(
                            text = "Use colors from your wallpaper",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    trailingContent = {
                        // The row handles the toggle, so the switch only displays state.
                        Switch(
                            checked = settings.dynamicColor,
                            onCheckedChange = null,
                            thumbContent = if (settings.dynamicColor) {
                                {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        modifier = Modifier.size(SwitchDefaults.IconSize)
                                    )
                                }
                            } else {
                                null
                            }
                        )
                    }
                ) {
                    Text(
                        text = "Dynamic color",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        } else {
            null
        },
        { shapes ->
            SettingsSelectionItem(
                label = "Icon shape",
                // The row's own badge previews the chosen shape.
                icon = Icons.Default.Interests,
                selectedValue = settings.iconShape,
                values = listOf(
                    IconShapePreference.CIRCLE to "Circle",
                    IconShapePreference.SQUARE to "Square",
                    IconShapePreference.COOKIE to "Cookie",
                    IconShapePreference.CLOVER to "Clover",
                    IconShapePreference.SUNNY to "Sunny",
                    IconShapePreference.FLOWER to "Flower"
                ),
                shapes = shapes,
                onValueSelected = viewModel::setIconShape
            )
        },
        { shapes ->
            SettingsSelectionItem(
                label = "App bar",
                icon = Icons.Default.WebAsset,
                selectedValue = settings.appBar,
                values = listOf(
                    AppBarPreference.SMALL to "Small",
                    AppBarPreference.MEDIUM to "Medium",
                    AppBarPreference.LARGE to "Large"
                ),
                shapes = shapes,
                onValueSelected = viewModel::setAppBar
            )
        },
        { shapes ->
            SettingsSelectionItem(
                label = "Bottom bar",
                icon = Icons.Default.CallToAction,
                selectedValue = settings.bottomBar,
                values = listOf(
                    BottomBarPreference.SHOW_LABELS to "Show labels",
                    BottomBarPreference.ICONS_ONLY to "Icons only",
                    BottomBarPreference.SHOW_LABELS_WHEN_SELECTED to "Show labels when selected"
                ),
                shapes = shapes,
                onValueSelected = viewModel::setBottomBar
            )
        },
        { shapes ->
            SettingsSelectionItem(
                label = "App bar scroll",
                icon = Icons.Default.SwapVert,
                selectedValue = settings.appBarScroll,
                values = listOfNotNull(
                    AppBarScrollPreference.PINNED to "Pinned",
                    AppBarScrollPreference.ENTER_ALWAYS to "Always returns",
                    // A small app bar has nothing to collapse.
                    (AppBarScrollPreference.EXIT_UNTIL_COLLAPSED to "Collapse")
                        .takeIf { settings.appBar != AppBarPreference.SMALL }
                ),
                shapes = shapes,
                onValueSelected = viewModel::setAppBarScroll
            )
        }
    )

    val manageRows = listOf<SettingsRow>(
        { shapes ->
            SettingsInfoItem(
                label = "Categories",
                description = "Rename, archive or delete",
                icon = Icons.Default.Category,
                shapes = shapes,
                onClick = onCategoriesClick
            )
        },
        { shapes ->
            SettingsInfoItem(
                label = "Accounts",
                description = "Rename, archive or delete",
                icon = Icons.Default.AccountBalanceWallet,
                shapes = shapes,
                onClick = onAccountsClick
            )
        }
    )

    val aboutRows = listOf<SettingsRow>(
        { shapes ->
            SettingsInfoItem(
                label = "Developer",
                description = "Prajwal Pawar",
                icon = Icons.Default.Person,
                shapes = shapes
            )
        },
        { shapes ->
            SettingsInfoItem(
                label = "GitHub",
                description = "View source code",
                icon = Icons.Default.Code,
                shapes = shapes,
                onClick = {
                    context.startActivity(
                        Intent(
                            Intent.ACTION_VIEW,
                            "https://github.com/prajwalpawar7744/fiscus".toUri()
                        )
                    )
                }
            )
        }
    )

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = FiscusSpacing.md,
            end = FiscusSpacing.md,
            bottom = FiscusSpacing.lg
        ),
        verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)
    ) {
        settingsSection("Appearance", appearanceRows)
        settingsSection("Manage", manageRows)
        settingsSection("About", aboutRows)
    }
}

// Shapes come from each row's position, so adding or hiding a row keeps the corners right.
private fun LazyListScope.settingsSection(
    title: String,
    rows: List<SettingsRow>
) {
    item(key = title) {
        SettingsSectionTitle(title)
    }

    itemsIndexed(rows) { index, row ->
        row(
            ListItemDefaults.segmentedShapes(
                index = index,
                count = rows.size
            )
        )
    }
}

@Composable
private fun <T> SettingsSelectionItem(
    label: String,
    icon: ImageVector,
    selectedValue: T,
    values: List<Pair<T, String>>,
    shapes: ListItemShapes,
    onValueSelected: (T) -> Unit
) {
    var expanded by remember {
        mutableStateOf(false)
    }

    val selectedPair = values.firstOrNull {
        it.first == selectedValue
    }

    Column {
        FiscusSegmentedListItem(
            shapes = shapes,
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
                FiscusListIcon(icon = icon)
            },
            supportingContent = {
                Text(
                    text = selectedPair?.second.orEmpty(),
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
            selectedItem = selectedPair,
            onItemSelected = { pair ->
                onValueSelected(pair.first)
                expanded = false
            },
            itemLabel = { pair ->
                Text(
                    text = pair.second,
                    style = MaterialTheme.typography.bodyLarge
                )
            },
            offset = DpOffset(
                x = FiscusSpacing.none,
                y = FiscusSpacing.xs
            )
        )
    }
}

@Composable
private fun SettingsInfoItem(
    label: String,
    description: String,
    icon: ImageVector,
    shapes: ListItemShapes,
    onClick: (() -> Unit)? = null
) {
    FiscusSegmentedListItem(
        shapes = shapes,
        onClick = onClick,
        leadingContent = {
            FiscusListIcon(icon = icon)
        },
        content = {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge
            )
        },
        supportingContent = {
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    )
}

@Composable
private fun SettingsSectionTitle(
    title: String
) {
    // Inset to line up with the text inside the rows below.
    FiscusSectionHeader(
        title = title,
        modifier = Modifier.padding(
            start = FiscusSpacing.md,
            top = FiscusSpacing.lg,
            bottom = FiscusSpacing.sm
        )
    )
}
