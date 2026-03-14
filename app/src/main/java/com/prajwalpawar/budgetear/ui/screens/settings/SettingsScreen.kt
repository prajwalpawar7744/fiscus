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
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import com.prajwalpawar.budgetear.ui.components.ConfirmationDialog
import java.io.BufferedReader
import java.io.InputStreamReader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    
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
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
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
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { photoLauncher.launch("image/*") },
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
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = uiState.userName.ifBlank { "Add Name" },
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.clickable { 
                            tempName = uiState.userName
                            showNameDialog = true 
                        }
                    )
                }
            }

            item { HorizontalDivider() }

            // Preferences
            item {
                SettingsGroup(title = "Appearance") {
                    SettingsItem(
                        icon = Icons.Default.Palette,
                        title = "Theme",
                        subtitle = uiState.themeMode.replaceFirstChar { it.uppercase() },
                        onClick = {
                            val nextMode = when (uiState.themeMode) {
                                "system" -> "light"
                                "light" -> "dark"
                                else -> "system"
                            }
                            viewModel.updateThemeMode(nextMode)
                        }
                    )
                }
            }

            item {
                SettingsGroup(title = "Security") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Fingerprint, contentDescription = null, tint = LocalContentColor.current)
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text("Biometric Lock", style = MaterialTheme.typography.bodyLarge)
                                Text(
                                    "Require fingerprint to open app",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Switch(
                            checked = uiState.isBiometricEnabled,
                            onCheckedChange = { viewModel.updateBiometricEnabled(it) },
                            modifier = Modifier.padding(end = 16.dp)
                        )
                    }
                }
            }

            item {
                SettingsGroup(title = "Localization") {
                    SettingsItem(
                        icon = Icons.Default.AttachMoney,
                        title = "Currency",
                        subtitle = uiState.currency,
                        onClick = { showCurrencyDialog = true }
                    )
                }
            }

            // Backup & Restore Section
            item {
                SettingsGroup(title = "Backup & Restore") {
                    SettingsItem(
                        title = "Export Data",
                        subtitle = "Save your data to a JSON file",
                        icon = Icons.Default.CloudDownload,
                        onClick = { exportLauncher.launch("budgetear_backup_${System.currentTimeMillis()}.json") }
                    )
                    SettingsItem(
                        title = "Import Data",
                        subtitle = "Restore data from a JSON file",
                        icon = Icons.Default.CloudUpload,
                        onClick = { showImportWarning = true }
                    )
                }
            }

            item {
                SettingsGroup(title = "Data Management") {
                    SettingsItem(
                        icon = Icons.Default.DeleteForever,
                        title = "Reset Data",
                        subtitle = "Clear all transactions and accounts",
                        textColor = MaterialTheme.colorScheme.error,
                        onClick = { showResetDialog = true }
                    )
                }
            }
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
        val currencies = listOf("USD", "EUR", "GBP", "INR", "JPY")
        AlertDialog(
            onDismissRequest = { showCurrencyDialog = false },
            title = { Text("Select Currency") },
            text = {
                Column {
                    currencies.forEach { currency ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clickable {
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
fun SettingsGroup(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 12.dp)
        )
        Surface(
            color = MaterialTheme.colorScheme.surfaceContainer,
            shape = MaterialTheme.shapes.large,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(vertical = 4.dp)) {
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
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = if (textColor != MaterialTheme.colorScheme.onSurface) textColor else LocalContentColor.current)
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(title, style = MaterialTheme.typography.bodyLarge, color = textColor)
            if (subtitle != null) {
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
