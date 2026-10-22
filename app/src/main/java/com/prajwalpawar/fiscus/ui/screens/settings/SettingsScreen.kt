package com.prajwalpawar.fiscus.ui.screens.settings

import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.compose.material3.LoadingIndicator
import java.time.LocalDate
import com.prajwalpawar.fiscus.util.restartApp
import com.prajwalpawar.fiscus.data.backup.BackupPart
import androidx.compose.ui.Alignment
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarHost
import androidx.compose.material.icons.rounded.Restore
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.material.icons.rounded.Backup
import androidx.compose.foundation.layout.Box
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.CallToAction
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Interests
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.SwapVert
import androidx.compose.material.icons.rounded.Wallpaper
import androidx.compose.material.icons.rounded.WebAsset
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.ListItemShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.DpOffset
import androidx.core.content.pm.PackageInfoCompat
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.prajwalpawar.fiscus.data.model.AppBarPreference
import com.prajwalpawar.fiscus.data.model.AppBarScrollPreference
import com.prajwalpawar.fiscus.data.model.BottomBarPreference
import com.prajwalpawar.fiscus.data.model.IconShapePreference
import com.prajwalpawar.fiscus.data.model.ThemePreference
import com.prajwalpawar.fiscus.ui.components.FiscusAvatar
import com.prajwalpawar.fiscus.ui.components.FiscusDropdownMenu
import com.prajwalpawar.fiscus.ui.components.FiscusListIcon
import com.prajwalpawar.fiscus.ui.components.FiscusSectionHeader
import com.prajwalpawar.fiscus.ui.components.FiscusSegmentedListItem
import com.prajwalpawar.fiscus.ui.theme.FiscusSize
import com.prajwalpawar.fiscus.ui.theme.FiscusSpacing

// A settings row; it receives the corner shapes for its position in the section.
private typealias SettingsRow = @Composable (shapes: ListItemShapes) -> Unit

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SettingsScreen(
    onProfileClick: () -> Unit,
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
    val snackbarHostState = remember { SnackbarHostState() }

    // Wallpaper-based color only exists on Android 12+.
    val dynamicColorSupported = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

    val profileRows = listOf<SettingsRow>(
        { shapes ->
            FiscusSegmentedListItem(
                shapes = shapes,
                onClick = onProfileClick,
                leadingContent = {
                    FiscusAvatar(
                        name = settings.profileName,
                        photoVersion = settings.profilePhotoVersion,
                        size = FiscusSize.avatarRow,
                        initialStyle = MaterialTheme.typography.titleLargeEmphasized
                    )
                },
                content = {
                    Text(
                        text = settings.profileName.ifEmpty { "Add your name" },
                        style = MaterialTheme.typography.titleMediumEmphasized
                    )
                },
                supportingContent = {
                    Text(
                        text = "Name and profile photo",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            )
        }
    )

    val appearanceRows = listOfNotNull<SettingsRow>(
        { shapes ->
            SettingsSelectionItem(
                label = "Theme",
                icon = Icons.Rounded.Palette,
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
                        FiscusListIcon(icon = Icons.Rounded.Wallpaper)
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
                                        imageVector = Icons.Rounded.Check,
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
        // Only matters when the wallpaper colours aren't in use.
        if (!dynamicColorSupported || !settings.dynamicColor) {
            { shapes ->
                PaletteRow(
                    selected = settings.palette,
                    shapes = shapes,
                    onSelected = viewModel::setPalette
                )
            }
        } else {
            null
        },
        { shapes ->
            SettingsSelectionItem(
                label = "Icon shape",
                // The row's own badge previews the chosen shape.
                icon = Icons.Rounded.Interests,
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
                icon = Icons.Rounded.WebAsset,
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
                icon = Icons.Rounded.CallToAction,
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
                icon = Icons.Rounded.SwapVert,
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
                icon = Icons.Rounded.Category,
                shapes = shapes,
                onClick = onCategoriesClick,
            )
        },
        { shapes ->
            SettingsInfoItem(
                label = "Accounts",
                description = "Rename, archive or delete",
                icon = Icons.Rounded.AccountBalanceWallet,
                shapes = shapes,
                onClick = onAccountsClick,
            )
        }
    )

    // Read from the installed package, so it always matches build.gradle's version.
    val version = remember(context) { appVersion(context) }

    val dataState by viewModel.dataState.collectAsStateWithLifecycle()
    var backupDialog by rememberSaveable { mutableStateOf(false) }
    var wipeDialog by rememberSaveable { mutableStateOf(false) }
    // Parts chosen in the backup dialog, waiting for the user to pick where to save.
    var partsToBackUp by remember { mutableStateOf(emptySet<BackupPart>()) }

    // The system file picker: no storage permission, and the user picks where it goes.
    val createBackupFile = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri ->
        if (uri != null) viewModel.backup(uri, partsToBackUp)
    }
    val openBackupFile = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) viewModel.inspectBackup(uri)
    }

    // Restored or wiped data is only loaded fresh by restarting the app.
    LaunchedEffect(dataState.restartNeeded) {
        if (dataState.restartNeeded) restartApp(context)
    }
    LaunchedEffect(dataState.message) {
        dataState.message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.messageShown()
        }
    }

    val dataRows = listOf<SettingsRow>(
        { shapes ->
            SettingsInfoItem(
                label = "Back up",
                description = if (dataState.isBackingUp) "Saving…" else "Save your data to a file",
                icon = Icons.Rounded.Backup,
                shapes = shapes,
                onClick = { if (!dataState.isBackingUp) backupDialog = true },
                // A quick save: progress stays on the row instead of blocking the app.
                trailingContent = if (dataState.isBackingUp) {
                    { LoadingIndicator(modifier = Modifier.size(FiscusSize.listIconContainer)) }
                } else {
                    null
                }
            )
        },
        { shapes ->
            SettingsInfoItem(
                label = "Restore",
                description = "Bring back data from a backup file",
                icon = Icons.Rounded.Restore,
                shapes = shapes,
                onClick = { openBackupFile.launch(arrayOf("*/*")) }
            )
        },
        { shapes ->
            SettingsInfoItem(
                label = "Wipe data",
                description = "Delete everything and start fresh",
                icon = Icons.Rounded.DeleteForever,
                shapes = shapes,
                onClick = { wipeDialog = true }
            )
        }
    )

    if (backupDialog) {
        BackupPartsDialog(
            restoring = false,
            available = BackupPart.entries.toSet(),
            createdAt = null,
            onConfirm = { parts ->
                backupDialog = false
                partsToBackUp = parts
                createBackupFile.launch("fiscus-backup-${LocalDate.now()}.fiscus")
            },
            onDismiss = { backupDialog = false }
        )
    }

    dataState.pendingRestore?.let { pending ->
        BackupPartsDialog(
            restoring = true,
            available = pending.info.parts,
            createdAt = pending.info.createdAt,
            onConfirm = viewModel::restore,
            onDismiss = viewModel::cancelRestore
        )
    }

    if (wipeDialog) {
        WipeDataDialog(
            onConfirm = {
                wipeDialog = false
                viewModel.wipe()
            },
            onDismiss = { wipeDialog = false }
        )
    }

    if (dataState.isBusy || dataState.restartNeeded) {
        DataBusyDialog()
    }

    // Failures need reading and acting on, so they get a dialog, not a snackbar.
    dataState.error?.let { error ->
        DataErrorDialog(message = error, onDismiss = viewModel::errorShown)
    }

    val aboutRows = listOf<SettingsRow>(
        { shapes ->
            SettingsInfoItem(
                label = "Version",
                description = version,
                icon = Icons.Rounded.Info,
                shapes = shapes
            )
        },
        { shapes ->
            SettingsInfoItem(
                label = "Developer",
                description = "Prajwal Pawar",
                icon = Icons.Rounded.Person,
                shapes = shapes
            )
        },
        { shapes ->
            SettingsInfoItem(
                label = "GitHub",
                description = "View source code",
                icon = Icons.Rounded.Code,
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

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = FiscusSpacing.md,
                end = FiscusSpacing.md,
                bottom = FiscusSpacing.lg
            ),
            verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)
        ) {
            settingsSection("Profile", profileRows)
            settingsSection("Appearance", appearanceRows)
            settingsSection("Manage", manageRows)
            settingsSection("Data", dataRows)
            settingsSection("About", aboutRows)
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
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
                        Icons.Rounded.KeyboardArrowUp
                    } else {
                        Icons.Rounded.KeyboardArrowDown
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
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    trailingContent: (@Composable () -> Unit)? = null
) {
    FiscusSegmentedListItem(
        shapes = shapes,
        modifier = modifier,
        onClick = onClick,
        trailingContent = trailingContent,
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

// "0.1.0-beta.1 (build 1)"
private fun appVersion(context: Context): String {
    val info = context.packageManager.getPackageInfo(context.packageName, 0)
    val build = PackageInfoCompat.getLongVersionCode(info)
    return "${info.versionName} (build $build)"
}
