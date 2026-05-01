package com.prajwalpawar.fiscus.ui.screens.settings

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prajwalpawar.fiscus.data.local.pref.PreferenceManager
import com.prajwalpawar.fiscus.domain.repository.FiscusRepository
import com.prajwalpawar.fiscus.data.local.backup.BackupManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Inject

data class SettingsUiState(
    val userName: String = "",
    val userPhotoUri: String? = null,
    val themeMode: String = "system",
    val isBiometricEnabled: Boolean = false,
    val currency: String = "USD",
    val isDynamicColorEnabled: Boolean = true,
    val topBarStyle: String = "standard",
    val areAnimationsEnabled: Boolean = true,
    val isPrivacyModeEnabled: Boolean = false,
    val borderRadius: Int = 12,
    val navLabelMode: String = "always",
    val isCompactNumberFormatEnabled: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferenceManager: PreferenceManager,
    private val repository: FiscusRepository,
    private val backupManager: BackupManager,
    @param:ApplicationContext private val context: Context
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
        preferenceManager.isPrivacyModeEnabled,
        preferenceManager.borderRadius,
        preferenceManager.navLabelMode,
        preferenceManager.isCompactNumberFormatEnabled
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
            isPrivacyModeEnabled = args[8] as Boolean,
            borderRadius = args[9] as Int,
            navLabelMode = args[10] as String,
            isCompactNumberFormatEnabled = args[11] as Boolean
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, SettingsUiState())

    fun updateUserName(name: String) {
        viewModelScope.launch {
            preferenceManager.updateUserName(name)
        }
    }

    fun updateUserPhoto(bitmap: Bitmap) {
        viewModelScope.launch {
            val localPath = withContext(Dispatchers.IO) {
                saveBitmapToInternalStorage(bitmap)
            }
            if (localPath != null) {
                preferenceManager.updateUserPhotoUri(localPath)
            }
        }
    }

    private fun saveBitmapToInternalStorage(bitmap: Bitmap): String? {
        return try {
            // Delete old profile photos to prevent storage bloat
            context.filesDir.listFiles { _, name -> name.startsWith("profile_photo_") }
                ?.forEach { it.delete() }

            val filename = "profile_photo_${System.currentTimeMillis()}.jpg"
            val file = File(context.filesDir, filename)
            val outputStream = FileOutputStream(file)

            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
            outputStream.flush()
            outputStream.close()
            
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


    fun updatePrivacyModeEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferenceManager.updatePrivacyModeEnabled(enabled)
        }
    }

    fun updateBorderRadius(radius: Int) {
        viewModelScope.launch {
            preferenceManager.updateBorderRadius(radius)
        }
    }

    fun updateNavLabelMode(mode: String) {
        viewModelScope.launch {
            preferenceManager.updateNavLabelMode(mode)
        }
    }

    fun updateCompactNumberFormatEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferenceManager.updateCompactNumberFormatEnabled(enabled)
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
