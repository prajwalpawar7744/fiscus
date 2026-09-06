package com.prajwalpawar.fiscus.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prajwalpawar.fiscus.data.local.SettingsDataStore
import com.prajwalpawar.fiscus.data.model.AppBarPreference
import com.prajwalpawar.fiscus.data.model.AppBarScrollPreference
import com.prajwalpawar.fiscus.data.model.BottomBarPreference
import com.prajwalpawar.fiscus.data.model.ThemePreference
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.prajwalpawar.fiscus.data.model.AppSettings

class SettingsViewModel(
    private val settingsDataStore: SettingsDataStore
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
}