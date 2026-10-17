package com.prajwalpawar.fiscus

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.prajwalpawar.fiscus.data.local.SettingsDataStore
import com.prajwalpawar.fiscus.data.model.AppSettings
import com.prajwalpawar.fiscus.data.model.ThemePreference
import com.prajwalpawar.fiscus.ui.theme.FiscusTheme
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // The one place settings are read for the UI; null until DataStore answers.
        val settingsState: StateFlow<AppSettings?> = SettingsDataStore(applicationContext)
            .settings
            .stateIn(
                scope = lifecycleScope,
                started = SharingStarted.Eagerly,
                initialValue = null
            )

        // Hold the splash screen so the first frame already uses the saved theme.
        splashScreen.setKeepOnScreenCondition {
            settingsState.value == null
        }

        setContent {
            val settings by settingsState.collectAsStateWithLifecycle()
            val systemDarkTheme = isSystemInDarkTheme()

            settings?.let { loadedSettings ->
                val darkTheme = when (loadedSettings.theme) {
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
                    dynamicColor = loadedSettings.dynamicColor
                ) {
                    FiscusApp(settings = loadedSettings)
                }
            }
        }
    }
}
