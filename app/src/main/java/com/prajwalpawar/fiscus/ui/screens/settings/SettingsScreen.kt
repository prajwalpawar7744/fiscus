package com.prajwalpawar.fiscus.ui.screens.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.input.nestedscroll.nestedScroll
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import com.prajwalpawar.fiscus.ui.components.ConfirmationDialog
import com.prajwalpawar.fiscus.ui.utils.rememberFiscusHaptic
import com.prajwalpawar.fiscus.ui.utils.staggeredVerticalFadeIn
import com.prajwalpawar.fiscus.ui.utils.fiscusClickable
import java.io.BufferedReader
import java.io.InputStreamReader
import android.os.Build
import android.content.Intent
import androidx.compose.ui.graphics.Color
import androidx.core.net.toUri
import com.prajwalpawar.fiscus.BuildConfig

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onManageCategories: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val haptic = rememberFiscusHaptic()
    
    val photoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { viewModel.updateUserPhotoUri(it.toString()) }
    }

    var showNameDialog by remember { mutableStateOf(false) }
    var tempName by remember { mutableStateOf("") }
    
    var showCurrencyDialog by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }
    var showImportWarning by remember { mutableStateOf(false) }

    var showRadiusDialog by remember { mutableStateOf(false) }
    var showNavLabelDialog by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/vnd.sqlite3"),
        onResult = { uri ->
            uri?.let {
                scope.launch {
                    try {
                        val success = context.contentResolver.openOutputStream(it)?.use { output ->
                            viewModel.exportDatabase(output)
                        } ?: false
                        
                        if (success) {
                            snackbarHostState.showSnackbar("Database exported successfully")
                        } else {
                            snackbarHostState.showSnackbar("Export failed")
                        }
                    } catch (e: Exception) {
                        snackbarHostState.showSnackbar("Export failed: ${e.message}")
                    }
                }
            }
        }
    )

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            uri?.let {
                scope.launch {
                    try {
                        val success = context.contentResolver.openInputStream(it)?.use { input ->
                            viewModel.importDatabase(input)
                        } ?: false
                        
                        if (success) {
                            val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
                            val componentName = intent?.component
                            val mainIntent = android.content.Intent.makeRestartActivityTask(componentName)
                            context.startActivity(mainIntent)
                            Runtime.getRuntime().exit(0)
                        } else {
                            snackbarHostState.showSnackbar("Import failed")
                        }
                    } catch (e: Exception) {
                        snackbarHostState.showSnackbar("Import failed: ${e.message}")
                    }
                }
            }
        }
    )

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            if (uiState.topBarStyle == "longtopbar") {
                LargeTopAppBar(
                    title = { Text("Settings", fontWeight = FontWeight.Bold) },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface,
                        scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer
                    ),
                    scrollBehavior = scrollBehavior
                )
            } else {
                TopAppBar(
                    title = { Text("Settings", fontWeight = FontWeight.Bold) },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface,
                        scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer
                    ),
                    scrollBehavior = scrollBehavior
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // Profile Section
            item {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth().staggeredVerticalFadeIn(0, enabled = uiState.areAnimationsEnabled),
                    shape = MaterialTheme.shapes.extraLarge,
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(112.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceContainerLowest)
                                .border(2.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                                .fiscusClickable(haptic = haptic, enabledAnimations = uiState.areAnimationsEnabled) {
                                    photoLauncher.launch("image/*")
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (uiState.userPhotoUri != null) {
                                AsyncImage(
                                    model = uiState.userPhotoUri,
                                    contentDescription = "Profile Photo",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.PhotoCamera,
                                    contentDescription = null,
                                    modifier = Modifier.size(40.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        
                        // Scale name and click text
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = uiState.userName.ifBlank { "Add Name" },
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.fiscusClickable(haptic = haptic, enabledAnimations = uiState.areAnimationsEnabled) {
                                tempName = uiState.userName
                                showNameDialog = true 
                            }
                        )
                        
                        Text(
                            text = "Tap to edit profile",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(8.dp)) }

            // Preferences
            item {
                SettingsGroup(
                    modifier = Modifier.staggeredVerticalFadeIn(1, enabled = uiState.areAnimationsEnabled),
                    title = "Appearance"
                ) {
                    SettingsItem(
                        icon = Icons.Default.Palette,
                        title = "Theme",
                        subtitle = uiState.themeMode.replaceFirstChar { it.uppercase() },
                        animationsEnabled = uiState.areAnimationsEnabled,
                        onClick = {
                            val nextMode = when (uiState.themeMode) {
                                "system" -> "light"
                                "light" -> "dark"
                                else -> "system"
                            }
                            viewModel.updateThemeMode(nextMode)
                        }
                    )
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        SettingsItem(
                            icon = Icons.Default.ColorLens,
                            title = "Dynamic Color",
                            subtitle = "Use colors based on wallpaper",
                            trailingContent = {
                                Switch(
                                    checked = uiState.isDynamicColorEnabled,
                                    onCheckedChange = { 
                                        haptic.click()
                                        viewModel.updateDynamicColorEnabled(it) 
                                    }
                                )
                            },
                            animationsEnabled = uiState.areAnimationsEnabled,
                            onClick = { viewModel.updateDynamicColorEnabled(!uiState.isDynamicColorEnabled) }
                        )
                    }



                    SettingsItem(
                        icon = Icons.Default.ViewHeadline,
                        title = "Top Bar Style",
                        subtitle = if (uiState.topBarStyle == "standard") "Small (Default)" else "Large",
                        animationsEnabled = uiState.areAnimationsEnabled,
                        onClick = {
                            val nextStyle = if (uiState.topBarStyle == "standard") "longtopbar" else "standard"
                            viewModel.updateTopBarStyle(nextStyle)
                        }
                    )

                    SettingsItem(
                        icon = Icons.Default.Animation,
                        title = "Animations",
                        subtitle = if (uiState.areAnimationsEnabled) "Enabled" else "Disabled",
                        animationsEnabled = uiState.areAnimationsEnabled,
                        trailingContent = {
                            Switch(
                                checked = uiState.areAnimationsEnabled,
                                onCheckedChange = { 
                                    haptic.click()
                                    viewModel.updateAnimationsEnabled(it) 
                                }
                            )
                        },
                        onClick = { viewModel.updateAnimationsEnabled(!uiState.areAnimationsEnabled) }
                    )

                    SettingsItem(
                        icon = Icons.Default.RoundedCorner,
                        title = "Border Radius",
                        subtitle = "${uiState.borderRadius}dp",
                        animationsEnabled = uiState.areAnimationsEnabled,
                        onClick = { showRadiusDialog = true }
                    )

                    SettingsItem(
                        icon = Icons.AutoMirrored.Filled.Label,
                        title = "Navigation Labels",
                        subtitle = when(uiState.navLabelMode) {
                            "always" -> "Always show"
                            "selected" -> "Only when selected"
                            else -> "Never"
                        },
                        animationsEnabled = uiState.areAnimationsEnabled,
                        onClick = { showNavLabelDialog = true }
                    )
                }
            }

            item {
                SettingsGroup(
                    modifier = Modifier.staggeredVerticalFadeIn(2, enabled = uiState.areAnimationsEnabled),
                    title = "Security"
                ) {
                    SettingsItem(
                        icon = Icons.Default.Fingerprint,
                        title = "Biometric Lock",
                        subtitle = "Require fingerprint to open app",
                        animationsEnabled = uiState.areAnimationsEnabled,
                        trailingContent = {
                            Switch(
                                checked = uiState.isBiometricEnabled,
                                onCheckedChange = { 
                                    haptic.click()
                                    viewModel.updateBiometricEnabled(it) 
                                }
                            )
                        },
                        onClick = { viewModel.updateBiometricEnabled(!uiState.isBiometricEnabled) }
                    )
                    SettingsItem(
                        icon = Icons.Default.VisibilityOff,
                        title = "Privacy Mode",
                        subtitle = "Mask balances in public places",
                        animationsEnabled = uiState.areAnimationsEnabled,
                        trailingContent = {
                            Switch(
                                checked = uiState.isPrivacyModeEnabled,
                                onCheckedChange = { 
                                    haptic.click()
                                    viewModel.updatePrivacyModeEnabled(it) 
                                }
                            )
                        },
                        onClick = { viewModel.updatePrivacyModeEnabled(!uiState.isPrivacyModeEnabled) }
                    )
                }
            }

            item {
                SettingsGroup(
                    modifier = Modifier.staggeredVerticalFadeIn(3, enabled = uiState.areAnimationsEnabled),
                    title = "Localization"
                ) {
                    SettingsItem(
                        icon = Icons.Default.AttachMoney,
                        title = "Currency",
                        subtitle = uiState.currency,
                        animationsEnabled = uiState.areAnimationsEnabled,
                        onClick = { showCurrencyDialog = true }
                    )
                    SettingsItem(
                        icon = Icons.Default.Category,
                        title = "Manage Categories",
                        subtitle = "Add or edit custom categories",
                        animationsEnabled = uiState.areAnimationsEnabled,
                        onClick = onManageCategories
                    )
                }
            }

            // Backup & Restore Section
            item {
                SettingsGroup(
                    modifier = Modifier.staggeredVerticalFadeIn(4, enabled = uiState.areAnimationsEnabled),
                    title = "Backup & Restore"
                ) {
                    SettingsItem(
                        title = "Export Data",
                        subtitle = "Save your data to a database file",
                        icon = Icons.Default.CloudDownload,
                        animationsEnabled = uiState.areAnimationsEnabled,
                        onClick = { exportLauncher.launch("fiscus_backup_${System.currentTimeMillis()}.db") }
                    )
                    SettingsItem(
                        title = "Import Data",
                        subtitle = "Restore data from a database file",
                        icon = Icons.Default.CloudUpload,
                        animationsEnabled = uiState.areAnimationsEnabled,
                        onClick = { showImportWarning = true }
                    )
                }
            }

            item {
                SettingsGroup(
                    modifier = Modifier.staggeredVerticalFadeIn(5, enabled = uiState.areAnimationsEnabled),
                    title = "Data Management"
                ) {
                    SettingsItem(
                        icon = Icons.Default.DeleteForever,
                        title = "Reset Data",
                        subtitle = "Clear all transactions and accounts",
                        textColor = MaterialTheme.colorScheme.error,
                        animationsEnabled = uiState.areAnimationsEnabled,
                        onClick = { showResetDialog = true }
                    )
                }
            }

            // About Section
            item {
                SettingsGroup(
                    modifier = Modifier.staggeredVerticalFadeIn(6, enabled = uiState.areAnimationsEnabled),
                    title = "About"
                ) {
                    SettingsItem(
                        icon = Icons.Default.Info,
                        title = "Version",
                        subtitle = BuildConfig.VERSION_NAME,
                        animationsEnabled = uiState.areAnimationsEnabled,
                        onClick = {
                            haptic.click()
                        }
                    )
                    SettingsItem(
                        icon = Icons.Default.Link,
                        title = "Credits",
                        subtitle = "Prajwal Pawar",
                        animationsEnabled = uiState.areAnimationsEnabled,
                        onClick = {
                             haptic.click()
                             val intent = Intent(Intent.ACTION_VIEW,
                                 "https://github.com/prajwalpawar7744/fiscus".toUri())
                             context.startActivity(intent)
                        }
                    )
                }
            }
            
            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }

    if (showResetDialog) {
        ConfirmationDialog(
            onDismissRequest = { showResetDialog = false },
            onConfirm = {
                viewModel.resetData()
                showResetDialog = false
                scope.launch {
                    snackbarHostState.showSnackbar("All data has been reset")
                }
            },
            title = "Reset All Data?",
            text = "This will permanently delete ALL transactions, accounts, and categories. This action cannot be undone.",
            confirmButtonText = "Reset Everything",
            icon = Icons.Default.Warning
        )
    }

    if (showImportWarning) {
        ConfirmationDialog(
            onDismissRequest = { showImportWarning = false },
            onConfirm = {
                showImportWarning = false
                importLauncher.launch("*/*")
            },
            title = "Import Data?",
            text = "Importing data will replace your current transactions, accounts, and categories with the data from the backup file. Proceed?",
            confirmButtonText = "Import",
            icon = Icons.Default.CloudUpload,
            confirmButtonColor = ButtonDefaults.textButtonColors(
                contentColor = MaterialTheme.colorScheme.primary
            )
        )
    }

    if (showNameDialog) {
        AlertDialog(
            onDismissRequest = { showNameDialog = false },
            title = { Text("Update Name") },
            text = {
                OutlinedTextField(
                    value = tempName,
                    onValueChange = { tempName = it },
                    label = { Text("Name") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.updateUserName(tempName)
                    showNameDialog = false
                }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showNameDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showCurrencyDialog) {
        val currencies = listOf(
            "INR" to "Indian Rupee",
            "USD" to "US Dollar",
            "EUR" to "Euro",
            "GBP" to "British Pound",
            "JPY" to "Japanese Yen",
            "AUD" to "Australian Dollar",
            "CAD" to "Canadian Dollar"
        )
        AlertDialog(
            onDismissRequest = { showCurrencyDialog = false },
            icon = { Icon(Icons.Default.AttachMoney, null) },
            title = { Text("Select Currency") },
            text = {
                Column {
                    currencies.forEach { (code, name) ->
                        ListItem(
                            headlineContent = { Text(code, fontWeight = FontWeight.SemiBold) },
                            supportingContent = { Text(name, style = MaterialTheme.typography.bodySmall) },
                            leadingContent = {
                                RadioButton(
                                    selected = uiState.currency == code,
                                    onClick = null
                                )
                            },
                            modifier = Modifier
                                .clip(MaterialTheme.shapes.large)
                                .fiscusClickable(haptic = haptic, enabledAnimations = uiState.areAnimationsEnabled) {
                                    viewModel.updateCurrency(code)
                                    showCurrencyDialog = false
                                },
                            colors = ListItemDefaults.colors(
                                containerColor = if (uiState.currency == code)
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                else Color.Transparent
                            )
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showCurrencyDialog = false }) { Text("Close") }
            }
        )
    }

    if (showRadiusDialog) {
        AlertDialog(
            onDismissRequest = { showRadiusDialog = false },
            title = { Text("Border Radius") },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Preview box
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(uiState.borderRadius.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(uiState.borderRadius.dp))
                    )

                    Text(
                        text = "${uiState.borderRadius} dp",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Slider(
                        value = uiState.borderRadius.toFloat(),
                        onValueChange = { 
                            haptic.click()
                            viewModel.updateBorderRadius(it.toInt()) 
                        },
                        valueRange = 0f..28f,
                        steps = 27
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showRadiusDialog = false }) {
                    Text("Done")
                }
            }
        )
    }

    if (showNavLabelDialog) {
        val modes = listOf(
            Triple("always", "Always show", "Labels are visible for all tabs"),
            Triple("selected", "Only when selected", "Labels pop into view on selection"),
            Triple("never", "Never", "Clean, icon-only navigation")
        )
        AlertDialog(
            onDismissRequest = { showNavLabelDialog = false },
            title = { Text("Navigation Labels") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    modes.forEach { (mode, title, desc) ->
                        Row(
                            Modifier.fillMaxWidth()
                                .fiscusClickable(haptic = haptic) { 
                                    viewModel.updateNavLabelMode(mode)
                                    showNavLabelDialog = false
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = uiState.navLabelMode == mode,
                                onClick = null 
                            )
                            Spacer(Modifier.width(16.dp))
                            Column {
                                Text(title, style = MaterialTheme.typography.bodyLarge)
                                Text(desc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showNavLabelDialog = false }) { Text("Cancel") }
            }
        )
    }


}

@Composable
fun SettingsGroup(
    title: String, 
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp)
        )
        Surface(
            color = MaterialTheme.colorScheme.surfaceContainer,
            shape = MaterialTheme.shapes.extraLarge,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                content()
            }
        }
    }
}

@Composable
fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
    trailingContent: @Composable (() -> Unit)? = null,
    animationsEnabled: Boolean = true,
    onClick: () -> Unit
) {
    val haptic = rememberFiscusHaptic()
    ListItem(
        modifier = Modifier.fiscusClickable(
            haptic = haptic,
            enabledAnimations = animationsEnabled
        ) {
            onClick()
        },
        headlineContent = {
            Text(
                title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = textColor
            )
        },
        supportingContent = subtitle?.let {
            {
                Text(
                    it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        leadingContent = {
            Icon(
                icon,
                contentDescription = null,
                tint = if (textColor != MaterialTheme.colorScheme.onSurface) textColor else MaterialTheme.colorScheme.primary
            )
        },
        trailingContent = trailingContent,
        colors = ListItemDefaults.colors(
            containerColor = Color.Transparent
        )
    )
}
