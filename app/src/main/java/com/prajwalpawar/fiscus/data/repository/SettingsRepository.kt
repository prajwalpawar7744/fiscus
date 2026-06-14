package com.prajwalpawar.fiscus.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import com.prajwalpawar.fiscus.data.datastore.SettingsPrefs
import com.prajwalpawar.fiscus.data.datastore.settingsDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SettingsRepository (
    private val context: Context
) {
    val darkTheme: Flow<Boolean> = context.settingsDataStore.data.map { prefs ->
        prefs[SettingsPrefs.DARK_THEME] ?: false
    }

    val appBarStyle: Flow<String> = context.settingsDataStore.data.map { prefs ->
        prefs[SettingsPrefs.APP_BAR_STYLE] ?: "LARGE"
    }

    suspend fun setDarkTheme(enabled: Boolean) {
        context.settingsDataStore.edit { prefs ->
            prefs[SettingsPrefs.DARK_THEME] = enabled
        }
    }

    suspend fun setAppBarStyle(style: String) {
        context.settingsDataStore.edit { prefs ->
            prefs[SettingsPrefs.APP_BAR_STYLE] = style
        }
    }
}