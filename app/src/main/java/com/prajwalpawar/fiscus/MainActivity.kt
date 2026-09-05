package com.prajwalpawar.fiscus

import android.os.Bundle
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

            FiscusTheme (
                darkTheme = darkTheme
            ) {
                FiscusApp()
            }
        }
    }
}