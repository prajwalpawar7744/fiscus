package com.prajwalpawar.budgetear.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prajwalpawar.budgetear.data.local.pref.PreferenceManager
import com.prajwalpawar.budgetear.domain.repository.BudgetRepository
import com.prajwalpawar.budgetear.data.local.backup.BackupManager
import android.content.Context
import android.net.Uri
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

data class SettingsUiState(
    val userName: String = "",
    val userPhotoUri: String? = null,
    val themeMode: String = "system",
    val isBiometricEnabled: Boolean = false,
    val currency: String = "USD"
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferenceManager: PreferenceManager,
    private val repository: BudgetRepository,
    private val backupManager: BackupManager,
    @ApplicationContext private val context: Context
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = combine(
        preferenceManager.userName,
        preferenceManager.userPhotoUri,
        preferenceManager.themeMode,
        preferenceManager.isBiometricEnabled,
        preferenceManager.currency
    ) { name, photo, theme, biometric, currency ->
        SettingsUiState(name, photo, theme, biometric, currency)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsUiState())

    fun updateUserName(name: String) {
        viewModelScope.launch {
            preferenceManager.updateUserName(name)
        }
    }

    fun updateUserPhotoUri(uriString: String) {
        viewModelScope.launch {
            val uri = Uri.parse(uriString)
            val localPath = saveImageToInternalStorage(uri)
            if (localPath != null) {
                preferenceManager.updateUserPhotoUri(localPath)
            }
        }
    }

    private fun saveImageToInternalStorage(uri: Uri): String? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val file = File(context.filesDir, "profile_photo.jpg")
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

    fun resetData() {
        viewModelScope.launch {
            repository.clearAllData()
        }
    }

    suspend fun exportData(): String? {
        return backupManager.exportData()
    }

    suspend fun importData(jsonData: String): Boolean {
        return backupManager.importData(jsonData)
    }
}
