package com.prajwalpawar.fiscus.ui.theme

import android.app.UiModeManager
import android.content.Context
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

/**
 * Fiscus app theme: Material 3 Expressive (ADR-006, ADR-012).
 *
 * @param darkTheme whether to use the dark color scheme.
 * @param dynamicColor use wallpaper-based color on Android 12+. When false (or unsupported),
 * the brand schemes from [Color.kt] are used, picked by the user's contrast setting.
 */
@Composable
fun FiscusTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val colorScheme = when {
        // On Android 14+ dynamic schemes already follow the user's contrast setting.
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)

        else -> brandColorScheme(darkTheme, contrastLevel(context))
    }

    MaterialExpressiveTheme(
        colorScheme = colorScheme,
        motionScheme = MotionScheme.expressive(),
        shapes = FiscusShapes,
        typography = FiscusTypography,
        content = content,
    )
}

/** User-selected contrast (Android 14+): 0 = standard, 0.5 = medium, 1 = high. */
private fun contrastLevel(context: Context): Float =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
        context.getSystemService(UiModeManager::class.java)?.contrast ?: 0f
    } else {
        0f
    }

internal fun brandColorScheme(darkTheme: Boolean, contrastLevel: Float): ColorScheme =
    when {
        contrastLevel >= 1f -> if (darkTheme) darkHighContrastScheme else lightHighContrastScheme
        contrastLevel >= 0.5f -> if (darkTheme) darkMediumContrastScheme else lightMediumContrastScheme
        else -> if (darkTheme) darkScheme else lightScheme
    }
