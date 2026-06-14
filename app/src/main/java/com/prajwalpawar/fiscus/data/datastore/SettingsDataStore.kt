package com.prajwalpawar.fiscus.data.datastore

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

val Context.settingsDataStore by preferencesDataStore(name = "settings")

object SettingsPrefs {
    val DARK_THEME = booleanPreferencesKey("dark_theme")
    val APP_BAR_STYLE = stringPreferencesKey("app_bar_style")
}