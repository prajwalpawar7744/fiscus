package com.prajwalpawar.fiscus.data.local

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.prajwalpawar.fiscus.data.model.AppBarPreference
import com.prajwalpawar.fiscus.data.model.AppBarScrollPreference
import com.prajwalpawar.fiscus.data.model.AppSettings
import com.prajwalpawar.fiscus.data.model.BottomBarPreference
import com.prajwalpawar.fiscus.data.model.IconShapePreference
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
        val dynamicColor = booleanPreferencesKey("dynamic_color")
        val iconShape = stringPreferencesKey("icon_shape")
        val appBar = stringPreferencesKey("app_bar")
        val bottomBar = stringPreferencesKey("bottom_bar")
        val appBarScroll = stringPreferencesKey("app_bar_scroll")
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

            dynamicColor = preferences[Keys.dynamicColor] ?: true,

            iconShape = preferences[Keys.iconShape]
                ?.let { value ->
                    runCatching {
                        IconShapePreference.valueOf(value)
                    }.getOrNull()
                }
                ?: IconShapePreference.COOKIE,

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
                ?: BottomBarPreference.SHOW_LABELS,

            appBarScroll = preferences[Keys.appBarScroll]
                ?.let { value ->
                    runCatching {
                        AppBarScrollPreference.valueOf(value)
                    }.getOrNull()
                }
                ?: AppBarScrollPreference.PINNED,
        ).let { settings ->
            // A small app bar has nothing to collapse, so treat collapse as pinned.
            if (
                settings.appBar == AppBarPreference.SMALL &&
                settings.appBarScroll == AppBarScrollPreference.EXIT_UNTIL_COLLAPSED
            ) {
                settings.copy(appBarScroll = AppBarScrollPreference.PINNED)
            } else {
                settings
            }
        }
    }

    suspend fun setTheme(theme: ThemePreference) {
        context.settingsDataStore.edit { preferences ->
            preferences[Keys.theme] = theme.name
        }
    }

    suspend fun setDynamicColor(enabled: Boolean) {
        context.settingsDataStore.edit { preferences ->
            preferences[Keys.dynamicColor] = enabled
        }
    }

    suspend fun setIconShape(iconShape: IconShapePreference) {
        context.settingsDataStore.edit { preferences ->
            preferences[Keys.iconShape] = iconShape.name
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

    suspend fun setAppBarScroll(appBarScroll: AppBarScrollPreference) {
        context.settingsDataStore.edit { preferences ->
            preferences[Keys.appBarScroll] = appBarScroll.name
        }
    }
}