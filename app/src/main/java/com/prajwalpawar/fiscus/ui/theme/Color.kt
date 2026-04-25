package com.prajwalpawar.fiscus.ui.theme

import androidx.compose.ui.graphics.Color

// Light Palette Helper
data class AppPalette(
    val primary: Color,
    val onPrimary: Color,
    val primaryContainer: Color,
    val onPrimaryContainer: Color,
    val secondary: Color,
    val onSecondary: Color,
    val secondaryContainer: Color,
    val onSecondaryContainer: Color,
    val tertiary: Color,
    val onTertiary: Color,
    val tertiaryContainer: Color,
    val onTertiaryContainer: Color,
    val background: Color,
    val surface: Color,
    val onBackground: Color,
    val onSurface: Color,
    val surfaceContainer: Color
)

// --- MONOCHROME ---
val MonochromeLight = AppPalette(
    primary = Color(0xFF1C1B1F),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFE6E1E5),
    onPrimaryContainer = Color(0xFF1C1B1F),
    secondary = Color(0xFF49454F),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFE8DEF8),
    onSecondaryContainer = Color(0xFF1D192B),
    tertiary = Color(0xFF313033),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFE6E1E5),
    onTertiaryContainer = Color(0xFF1C1B1F),
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    surfaceContainer = Color(0xFFF4EFF4)
)

val MonochromeDark = AppPalette(
    primary = Color(0xFFE6E1E5),
    onPrimary = Color(0xFF1C1B1F),
    primaryContainer = Color(0xFF313033),
    onPrimaryContainer = Color(0xFFE6E1E5),
    secondary = Color(0xFFCAC4D0),
    onSecondary = Color(0xFF332D41),
    secondaryContainer = Color(0xFF4A4458),
    onSecondaryContainer = Color(0xFFE8DEF8),
    tertiary = Color(0xFFC9C5D0),
    onTertiary = Color(0xFF313033),
    tertiaryContainer = Color(0xFF49454F),
    onTertiaryContainer = Color(0xFFE6E1E5),
    background = Color(0xFF1C1B1F),
    surface = Color(0xFF1C1B1F),
    onBackground = Color(0xFFE6E1E5),
    onSurface = Color(0xFFE6E1E5),
    surfaceContainer = Color(0xFF2B2930)
)