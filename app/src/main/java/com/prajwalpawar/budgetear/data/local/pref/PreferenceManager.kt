package com.prajwalpawar.budgetear.data.local.pref

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "settings")

@Singleton
class PreferenceManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.dataStore

    object PreferencesKeys {
        val USER_NAME = stringPreferencesKey("user_name")
        val USER_PHOTO_URI = stringPreferencesKey("user_photo_uri")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val BIOMETRIC_ENABLED = booleanPreferencesKey("biometric_enabled")
        val CURRENCY = stringPreferencesKey("currency")
        val DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")
        val TOP_BAR_STYLE = stringPreferencesKey("top_bar_style")
        val ANIMATIONS_ENABLED = booleanPreferencesKey("animations_enabled")
        val ACCENT_COLOR = stringPreferencesKey("accent_color")
    }

    val userName: Flow<String> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.USER_NAME] ?: ""
    }

    val userPhotoUri: Flow<String?> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.USER_PHOTO_URI]
    }

    val themeMode: Flow<String> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.THEME_MODE] ?: "system"
    }

    val isBiometricEnabled: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.BIOMETRIC_ENABLED] ?: false
    }

    val currency: Flow<String> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.CURRENCY] ?: "INR"
    }

    val isDynamicColorEnabled: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.DYNAMIC_COLOR] ?: true
    }

    val topBarStyle: Flow<String> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.TOP_BAR_STYLE] ?: "standard"
    }

    val areAnimationsEnabled: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.ANIMATIONS_ENABLED] ?: true
    }

    val accentColor: Flow<String> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.ACCENT_COLOR] ?: "Emerald"
    }

    suspend fun updateUserName(name: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.USER_NAME] = name
        }
    }

    suspend fun updateUserPhotoUri(uri: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.USER_PHOTO_URI] = uri
        }
    }

    suspend fun updateThemeMode(mode: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME_MODE] = mode
        }
    }

    suspend fun updateBiometricEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.BIOMETRIC_ENABLED] = enabled
        }
    }

    suspend fun updateCurrency(currency: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.CURRENCY] = currency
        }
    }

    suspend fun updateDynamicColorEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.DYNAMIC_COLOR] = enabled
        }
    }

    suspend fun updateTopBarStyle(style: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.TOP_BAR_STYLE] = style
        }
    }

    suspend fun updateAnimationsEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.ANIMATIONS_ENABLED] = enabled
        }
    }

    suspend fun updateAccentColor(accent: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.ACCENT_COLOR] = accent
        }
    }
}
