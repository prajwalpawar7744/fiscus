package com.prajwalpawar.fiscus.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prajwalpawar.fiscus.data.local.pref.PreferenceManager
import com.prajwalpawar.fiscus.domain.repository.FiscusRepository
import com.prajwalpawar.fiscus.data.local.backup.BackupManager
import java.io.InputStream
import java.io.OutputStream
import android.content.Context
import android.net.Uri
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import androidx.core.net.toUri

data class SettingsUiState(
    val userName: String = "",
    val userPhotoUri: String? = null,
    val themeMode: String = "system",
    val isBiometricEnabled: Boolean = false,
    val currency: String = "USD",
    val isDynamicColorEnabled: Boolean = true,
    val topBarStyle: String = "standard",
    val areAnimationsEnabled: Boolean = true,
    val accentColor: String = "Emerald",
    val isPrivacyModeEnabled: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferenceManager: PreferenceManager,
    private val repository: FiscusRepository,
    private val backupManager: BackupManager,
    @ApplicationContext private val context: Context
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = combine<Any?, SettingsUiState>(
        preferenceManager.userName,
        preferenceManager.userPhotoUri,
        preferenceManager.themeMode,
        preferenceManager.isBiometricEnabled,
        preferenceManager.currency,
        preferenceManager.isDynamicColorEnabled,
        preferenceManager.topBarStyle,
        preferenceManager.areAnimationsEnabled,
        preferenceManager.accentColor,
        preferenceManager.isPrivacyModeEnabled
    ) { args ->
        SettingsUiState(
            userName = args[0] as String,
            userPhotoUri = args[1] as? String,
            themeMode = args[2] as String,
            isBiometricEnabled = args[3] as Boolean,
            currency = args[4] as String,
            isDynamicColorEnabled = args[5] as Boolean,
            topBarStyle = args[6] as String,
            areAnimationsEnabled = args[7] as Boolean,
            accentColor = args[8] as String,
            isPrivacyModeEnabled = args[9] as Boolean
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, SettingsUiState())

    fun updateUserName(name: String) {
        viewModelScope.launch {
            preferenceManager.updateUserName(name)
        }
    }

    fun updateUserPhotoUri(uriString: String) {
        viewModelScope.launch {
            val uri = uriString.toUri()
            val localPath = saveImageToInternalStorage(uri)
            if (localPath != null) {
                preferenceManager.updateUserPhotoUri(localPath)
            }
        }
    }

    private fun saveImageToInternalStorage(uri: Uri): String? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            
            // Delete old profile photos to prevent storage bloat
            context.filesDir.listFiles { _, name -> name.startsWith("profile_photo_") }?.forEach { it.delete() }
            
            val filename = "profile_photo_${System.currentTimeMillis()}.jpg"
            val file = File(context.filesDir, filename)
            val outputStream = FileOutputStream(file)
            
            inputStream?.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun updateThemeMode(mode: String) {
        viewModelScope.launch {
            preferenceManager.updateThemeMode(mode)
        }
    }

    fun updateBiometricEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferenceManager.updateBiometricEnabled(enabled)
        }
    }

    fun updateCurrency(currency: String) {
        viewModelScope.launch {
            preferenceManager.updateCurrency(currency)
        }
    }

    fun updateDynamicColorEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferenceManager.updateDynamicColorEnabled(enabled)
        }
    }

    fun updateTopBarStyle(style: String) {
        viewModelScope.launch {
            preferenceManager.updateTopBarStyle(style)
        }
    }

    fun updateAnimationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferenceManager.updateAnimationsEnabled(enabled)
        }
    }

    fun updateAccentColor(accent: String) {
        viewModelScope.launch {
            preferenceManager.updateAccentColor(accent)
        }
    }

    fun updatePrivacyModeEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferenceManager.updatePrivacyModeEnabled(enabled)
        }
    }

    fun resetData() {
        viewModelScope.launch {
            repository.clearAllData()
        }
    }

    suspend fun exportDatabase(outputStream: OutputStream): Boolean {
        return backupManager.exportDatabase(outputStream)
    }

    suspend fun importDatabase(inputStream: InputStream): Boolean {
        return backupManager.importDatabase(inputStream)
    }
}
