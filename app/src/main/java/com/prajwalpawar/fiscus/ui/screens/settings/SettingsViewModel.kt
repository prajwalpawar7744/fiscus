package com.prajwalpawar.fiscus.ui.screens.settings

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prajwalpawar.fiscus.data.local.ProfilePhotoStore
import com.prajwalpawar.fiscus.data.local.SettingsDataStore
import com.prajwalpawar.fiscus.data.model.AppBarPreference
import com.prajwalpawar.fiscus.data.model.AppBarScrollPreference
import com.prajwalpawar.fiscus.data.model.AppSettings
import com.prajwalpawar.fiscus.data.model.BottomBarPreference
import com.prajwalpawar.fiscus.data.model.IconShapePreference
import com.prajwalpawar.fiscus.data.model.ThemePreference
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settingsDataStore: SettingsDataStore,
    private val profilePhotoStore: ProfilePhotoStore
) : ViewModel() {

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
