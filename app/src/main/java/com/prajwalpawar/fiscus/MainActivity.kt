package com.prajwalpawar.fiscus

import android.graphics.Color
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.prajwalpawar.fiscus.data.local.SettingsDataStore
import com.prajwalpawar.fiscus.data.model.AppSettings
import com.prajwalpawar.fiscus.ui.theme.FiscusTheme
import androidx.compose.runtime.*
import com.prajwalpawar.fiscus.data.model.ThemePreference

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val settingsDataStore = SettingsDataStore(applicationContext)

        setContent {
            val settings by settingsDataStore.settings
                .collectAsStateWithLifecycle(
                    initialValue = AppSettings()
                )
            val systemDarkTheme = isSystemInDarkTheme()

            val darkTheme = when (settings.theme) {
                ThemePreference.SYSTEM -> systemDarkTheme
                ThemePreference.LIGHT -> false
                ThemePreference.DARK -> true
            }

            // Bar icons must follow the app's theme, not the system's, or they vanish
            // when the two differ (e.g. app Dark on a Light system).
            DisposableEffect(darkTheme) {
                val barStyle = SystemBarStyle.auto(
                    lightScrim = Color.TRANSPARENT,
                    darkScrim = Color.TRANSPARENT,
                    detectDarkMode = { darkTheme }
                )

                enableEdgeToEdge(
                    statusBarStyle = barStyle,
                    navigationBarStyle = barStyle
                )

                onDispose {}
            }

            FiscusTheme (
                darkTheme = darkTheme,
                dynamicColor = settings.dynamicColor
            ) {
                FiscusApp()
            }
        }
    }
}