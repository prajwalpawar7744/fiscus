package com.prajwalpawar.fiscus.data.local

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.prajwalpawar.fiscus.data.model.AppBarPreference
import com.prajwalpawar.fiscus.data.model.AppSettings
import com.prajwalpawar.fiscus.data.model.BottomBarPreference
import com.prajwalpawar.fiscus.data.model.ThemePreference
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore by preferencesDataStore(
    name = "settings"
)

class SettingsDataStore (
    private val context: Context
) {
    private object Keys {
        val theme = stringPreferencesKey("theme")
        val appBar = stringPreferencesKey("app_bar")
        val bottomBar = stringPreferencesKey("bottom_bar")
    }

    val settings: Flow<AppSettings> = context.settingsDataStore.data.map { preferences ->
        AppSettings(
            theme = preferences[Keys.theme]
                ?.let { value ->
                    runCatching {
                        ThemePreference.valueOf(value)
                    }.getOrNull()
                }
                ?: ThemePreference.SYSTEM,

            appBar = preferences[Keys.appBar]
                ?.let { value ->
                    runCatching {
                        AppBarPreference.valueOf(value)
                    }.getOrNull()
                }
                ?: AppBarPreference.SMALL,

            bottomBar = preferences[Keys.bottomBar]
                ?.let { value ->
                    runCatching {
                        BottomBarPreference.valueOf(value)
                    }.getOrNull()
                }
                ?: BottomBarPreference.SHOW_LABELS
        )
    }

    suspend fun setTheme(theme: ThemePreference) {
        context.settingsDataStore.edit { preferences ->
            preferences[Keys.theme] = theme.name
        }
    }

    suspend fun setAppBar(appBar: AppBarPreference) {
        context.settingsDataStore.edit { preferences ->
            preferences[Keys.appBar] = appBar.name
        }
    }

    suspend fun setBottomBar(bottomBar: BottomBarPreference) {
        context.settingsDataStore.edit { preferences ->
            preferences[Keys.bottomBar] = bottomBar.name
        }
    }
}