package com.prajwalpawar.fiscus.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.prajwalpawar.fiscus.data.model.AppBarPreference
import com.prajwalpawar.fiscus.data.model.AppBarScrollPreference
import com.prajwalpawar.fiscus.data.model.AppSettings
import com.prajwalpawar.fiscus.data.model.BottomBarPreference
import com.prajwalpawar.fiscus.data.model.IconShapePreference
import com.prajwalpawar.fiscus.data.model.PalettePreference
import com.prajwalpawar.fiscus.data.model.ThemePreference
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore by preferencesDataStore(
    name = "settings"
)

class SettingsDataStore(
    private val context: Context
) {
    private object Keys {
        val theme = stringPreferencesKey("theme")
        val dynamicColor = booleanPreferencesKey("dynamic_color")
        val iconShape = stringPreferencesKey("icon_shape")
        val palette = stringPreferencesKey("palette")
        val appBar = stringPreferencesKey("app_bar")
        val bottomBar = stringPreferencesKey("bottom_bar")
        val appBarScroll = stringPreferencesKey("app_bar_scroll")
        val profileName = stringPreferencesKey("profile_name")
        val profilePhotoVersion = longPreferencesKey("profile_photo_version")
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

            palette = preferences[Keys.palette]
                ?.let { value ->
                    runCatching {
                        PalettePreference.valueOf(value)
                    }.getOrNull()
                }
                ?: PalettePreference.MONOCHROME,

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

            profileName = preferences[Keys.profileName].orEmpty(),

            profilePhotoVersion = preferences[Keys.profilePhotoVersion] ?: 0L,
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

    suspend fun setPalette(palette: PalettePreference) {
        context.settingsDataStore.edit { preferences ->
            preferences[Keys.palette] = palette.name
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

    suspend fun setProfileName(name: String) {
        context.settingsDataStore.edit { preferences ->
            preferences[Keys.profileName] = name
        }
    }

    suspend fun setProfilePhotoVersion(version: Long) {
        context.settingsDataStore.edit { preferences ->
            preferences[Keys.profilePhotoVersion] = version
        }
    }

    // Replaces every setting at once (restoring a backup).
    suspend fun replaceAppearance(settings: AppSettings) {
        context.settingsDataStore.edit { preferences ->
            preferences[Keys.theme] = settings.theme.name
            preferences[Keys.dynamicColor] = settings.dynamicColor
            preferences[Keys.palette] = settings.palette.name
            preferences[Keys.iconShape] = settings.iconShape.name
            preferences[Keys.appBar] = settings.appBar.name
            preferences[Keys.bottomBar] = settings.bottomBar.name
            preferences[Keys.appBarScroll] = settings.appBarScroll.name
        }
    }

    // Back to defaults (wiping data).
    suspend fun clear() {
        context.settingsDataStore.edit { it.clear() }
    }
}
