package com.prajwalpawar.fiscus.ui.screens.settings

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prajwalpawar.fiscus.data.backup.BackupException
import com.prajwalpawar.fiscus.data.backup.BackupInfo
import com.prajwalpawar.fiscus.data.backup.BackupManager
import com.prajwalpawar.fiscus.data.backup.BackupPart
import com.prajwalpawar.fiscus.data.local.ProfilePhotoStore
import com.prajwalpawar.fiscus.data.local.SettingsDataStore
import com.prajwalpawar.fiscus.data.model.AppBarPreference
import com.prajwalpawar.fiscus.data.model.AppBarScrollPreference
import com.prajwalpawar.fiscus.data.model.AppSettings
import com.prajwalpawar.fiscus.data.model.BottomBarPreference
import com.prajwalpawar.fiscus.data.model.IconShapePreference
import com.prajwalpawar.fiscus.data.model.ThemePreference
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// Backup / restore / wipe progress for the Data section.
data class DataUiState(
    // Restore or wipe running: the app is blocked until it restarts.
    val isBusy: Boolean = false,
    // A backup is being written; shown inline on the Back up row, nothing is blocked.
    val isBackingUp: Boolean = false,
    // One-off confirmation, shown in a snackbar.
    val message: String? = null,
    // Something failed and the user needs to know why; shown in a dialog.
    val error: String? = null,
    // A chosen backup file waiting for the user to pick what to restore.
    val pendingRestore: PendingRestore? = null,
    // Data was replaced; the app restarts to load it.
    val restartNeeded: Boolean = false
)

data class PendingRestore(val uri: Uri, val info: BackupInfo)

class SettingsViewModel(
    private val settingsDataStore: SettingsDataStore,
    private val profilePhotoStore: ProfilePhotoStore,
    private val backupManager: BackupManager
) : ViewModel() {
    private val data = MutableStateFlow(DataUiState())
    val dataState: StateFlow<DataUiState> = data.asStateFlow()

    fun backup(uri: Uri, parts: Set<BackupPart>) = runDataTask(
        blocking = false,
        success = "Backup saved"
    ) {
        backupManager.backup(uri, parts)
    }

    // Reads what the chosen file holds, then asks which parts to restore.
    fun inspectBackup(uri: Uri) = runDataTask {
        val info = backupManager.readInfo(uri)
        data.update { it.copy(pendingRestore = PendingRestore(uri, info)) }
    }

    fun cancelRestore() {
        data.update { it.copy(pendingRestore = null) }
    }

    fun restore(parts: Set<BackupPart>) {
        val pending = data.value.pendingRestore ?: return
        data.update { it.copy(pendingRestore = null) }
        runDataTask {
            backupManager.restore(pending.uri, parts)
            data.update { it.copy(restartNeeded = true) }
        }
    }

    fun wipe() = runDataTask {
        backupManager.wipe()
        data.update { it.copy(restartNeeded = true) }
    }

    fun messageShown() {
        data.update { it.copy(message = null) }
    }

    fun errorShown() {
        data.update { it.copy(error = null) }
    }

    // [blocking]: restore and wipe block the app; a backup only shows progress on its row.
    private fun runDataTask(
        blocking: Boolean = true,
        success: String? = null,
        task: suspend () -> Unit
    ) {
        if (data.value.isBusy || data.value.isBackingUp) return
        viewModelScope.launch {
            data.update { if (blocking) it.copy(isBusy = true) else it.copy(isBackingUp = true) }
            val error = try {
                task()
                null
            } catch (e: BackupException) {
                e.message
            } catch (e: Exception) {
                "Something went wrong. Nothing was changed."
            }
            data.update {
                it.copy(
                    isBusy = false,
                    isBackingUp = false,
                    message = if (error == null) success ?: it.message else it.message,
                    error = error
                )
            }
        }
    }


    val settings = settingsDataStore.settings.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AppSettings()
    )

    fun setTheme(theme: ThemePreference) {
        viewModelScope.launch {
            settingsDataStore.setTheme(theme)
        }
    }

    fun setDynamicColor(enabled: Boolean) {
        viewModelScope.launch {
            settingsDataStore.setDynamicColor(enabled)
        }
    }

    fun setIconShape(iconShape: IconShapePreference) {
        viewModelScope.launch {
            settingsDataStore.setIconShape(iconShape)
        }
    }

    fun setAppBar(appBar: AppBarPreference) {
        viewModelScope.launch {
            settingsDataStore.setAppBar(appBar)
        }
    }

    fun setBottomBar(bottomBar: BottomBarPreference) {
        viewModelScope.launch {
            settingsDataStore.setBottomBar(bottomBar)
        }
    }

    fun setAppBarScroll(appBarScroll: AppBarScrollPreference) {
        viewModelScope.launch {
            settingsDataStore.setAppBarScroll(appBarScroll)
        }
    }

    fun setProfileName(name: String) {
        viewModelScope.launch {
            settingsDataStore.setProfileName(name.trim())
        }
    }

    fun setProfilePhoto(uri: Uri) {
        viewModelScope.launch {
            if (profilePhotoStore.save(uri)) {
                settingsDataStore.setProfilePhotoVersion(System.currentTimeMillis())
            }
        }
    }

    fun removeProfilePhoto() {
        viewModelScope.launch {
            profilePhotoStore.delete()
            settingsDataStore.setProfilePhotoVersion(0L)
        }
    }
}
