package com.prajwalpawar.budgetear.ui.screens.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.prajwalpawar.budgetear.ui.components.ConfirmationDialog
import com.prajwalpawar.budgetear.ui.utils.rememberBudgetearHaptic
import com.prajwalpawar.budgetear.ui.utils.staggeredVerticalFadeIn
import com.prajwalpawar.budgetear.ui.utils.budgetearClickable
import java.io.BufferedReader
import java.io.InputStreamReader
import android.os.Build
import android.content.Intent
import android.net.Uri
import androidx.core.net.toUri
import com.prajwalpawar.budgetear.BuildConfig

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val haptic = rememberBudgetearHaptic()
    
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

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json"),
        onResult = { uri ->
            uri?.let {
                scope.launch {
                    val jsonData = viewModel.exportData()
                    if (jsonData != null) {
                        try {
                            context.contentResolver.openOutputStream(it)?.use { output ->
                                output.write(jsonData.toByteArray())
                            }
                            snackbarHostState.showSnackbar("Data exported successfully")
                        } catch (e: Exception) {
                            snackbarHostState.showSnackbar("Export failed: ${e.message}")
                        }
                    } else {
                        snackbarHostState.showSnackbar("Export failed: No data")
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
                        val inputStream = context.contentResolver.openInputStream(it)
                        val reader = BufferedReader(InputStreamReader(inputStream))
                        val jsonData = reader.readText()
                        inputStream?.close()
                        
                        val success = viewModel.importData(jsonData)
                        if (success) {
                            snackbarHostState.showSnackbar("Data imported successfully")
                        } else {
                            snackbarHostState.showSnackbar("Import failed: Invalid data")
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
                    colors = TopAppBarDefaults.largeTopAppBarColors(
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
                                .budgetearClickable(haptic = haptic, enabledAnimations = uiState.areAnimationsEnabled) {
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
                            modifier = Modifier.budgetearClickable(haptic = haptic, enabledAnimations = uiState.areAnimationsEnabled) { 
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
                        subtitle = "Save your data to a JSON file",
                        icon = Icons.Default.CloudDownload,
                        animationsEnabled = uiState.areAnimationsEnabled,
                        onClick = { exportLauncher.launch("budgetear_backup_${System.currentTimeMillis()}.json") }
                    )
                    SettingsItem(
                        title = "Import Data",
                        subtitle = "Restore data from a JSON file",
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
                                 "https://github.com/prajwalpawar7744/budgetear".toUri())
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
                importLauncher.launch("application/json")
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
        val currencies = listOf("INR", "USD", "EUR", "GBP", "JPY")
        AlertDialog(
            onDismissRequest = { showCurrencyDialog = false },
            title = { Text("Select Currency") },
            text = {
                Column {
                    currencies.forEach { currency ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .budgetearClickable(haptic = haptic, enabledAnimations = uiState.areAnimationsEnabled) {
                                    viewModel.updateCurrency(currency)
                                    showCurrencyDialog = false
                                }
                                .padding(16.dp)
                        ) {
                            Text(currency)
                        }
                    }
                }
            },
            confirmButton = {}
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
    textColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface,
    trailingContent: @Composable (() -> Unit)? = null,
    animationsEnabled: Boolean = true,
    onClick: () -> Unit
) {
    val haptic = rememberBudgetearHaptic()
    ListItem(
        modifier = Modifier.budgetearClickable(
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
            containerColor = androidx.compose.ui.graphics.Color.Transparent
        )
    )
}
