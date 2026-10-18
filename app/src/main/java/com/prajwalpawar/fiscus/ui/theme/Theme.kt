package com.prajwalpawar.fiscus.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalContext
import com.prajwalpawar.fiscus.data.model.IconShapePreference

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun FiscusTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    iconShape: IconShapePreference = IconShapePreference.COOKIE,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        // Brand colours when Dynamic color is off or unavailable.
        darkTheme -> FiscusDarkColorScheme
        else -> FiscusLightColorScheme
    }

    // Shapes, motion and typography (system font) use the Expressive defaults.
    MaterialExpressiveTheme(
        colorScheme = colorScheme
    ) {
        CompositionLocalProvider(
            LocalIconShape provides iconShape.polygon().toShape(),
            content = content
        )
    }
}
