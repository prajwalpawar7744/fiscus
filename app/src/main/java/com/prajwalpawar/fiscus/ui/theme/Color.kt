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

// --- EMERALD (DEFAULT) ---
val EmeraldLight = AppPalette(
    primary = Color(0xFF006D39),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFF96F7B4),
    onPrimaryContainer = Color(0xFF00210E),
    secondary = Color(0xFF4F6354),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFD1E8D5),
    onSecondaryContainer = Color(0xFF0C1F13),
    tertiary = Color(0xFF3B6470),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFBFEAF7),
    onTertiaryContainer = Color(0xFF001F27),
    background = Color(0xFFFBFDF8),
    surface = Color(0xFFFBFDF8),
    onBackground = Color(0xFF191C1A),
    onSurface = Color(0xFF191C1A),
    surfaceContainer = Color(0xFFEBEFE9)
)

val EmeraldDark = AppPalette(
    primary = Color(0xFF7BDA9A),
    onPrimary = Color(0xFF00391B),
    primaryContainer = Color(0xFF005229),
    onPrimaryContainer = Color(0xFF96F7B4),
    secondary = Color(0xFFB5CCBA),
    onSecondary = Color(0xFF213527),
    secondaryContainer = Color(0xFF374B3D),
    onSecondaryContainer = Color(0xFFD1E8D5),
    tertiary = Color(0xFFA3CDDB),
    onTertiary = Color(0xFF033541),
    tertiaryContainer = Color(0xFF224B58),
    onTertiaryContainer = Color(0xFFBFEAF7),
    background = Color(0xFF191C1A),
    surface = Color(0xFF191C1A),
    onBackground = Color(0xFFE1E3DF),
    onSurface = Color(0xFFE1E3DF),
    surfaceContainer = Color(0xFF1D201E)
)

// --- INDIGO ---
val IndigoLight = AppPalette(
    primary = Color(0xFF415AA9),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFDBE1FF),
    onPrimaryContainer = Color(0xFF00174B),
    secondary = Color(0xFF595E72),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFDDE1F9),
    onSecondaryContainer = Color(0xFF161B2C),
    tertiary = Color(0xFF745470),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFFFD6F7),
    onTertiaryContainer = Color(0xFF2B122A),
    background = Color(0xFFFEFBFF),
    surface = Color(0xFFFEFBFF),
    onBackground = Color(0xFF1B1B1F),
    onSurface = Color(0xFF1B1B1F),
    surfaceContainer = Color(0xFFEEEDF4)
)

val IndigoDark = AppPalette(
    primary = Color(0xFFB4C5FF),
    onPrimary = Color(0xFF082978),
    primaryContainer = Color(0xFF274290),
    onPrimaryContainer = Color(0xFFDBE1FF),
    secondary = Color(0xFFC1C5DD),
    onSecondary = Color(0xFF2B3042),
    secondaryContainer = Color(0xFF414659),
    onSecondaryContainer = Color(0xFFDDE1F9),
    tertiary = Color(0xFFE2BBDC),
    onTertiary = Color(0xFF422740),
    tertiaryContainer = Color(0xFF5A3D57),
    onTertiaryContainer = Color(0xFFFFD6F7),
    background = Color(0xFF1B1B1F),
    surface = Color(0xFF1B1B1F),
    onBackground = Color(0xFFE4E2E6),
    onSurface = Color(0xFFE4E2E6),
    surfaceContainer = Color(0xFF25262A)
)

// --- SAPPHIRE ---
val SapphireLight = AppPalette(
    primary = Color(0xFF006493),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFCAE6FF),
    onPrimaryContainer = Color(0xFF001E30),
    secondary = Color(0xFF50606E),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFD3E5F5),
    onSecondaryContainer = Color(0xFF0C1D29),
    tertiary = Color(0xFF65587E),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFEBDDFF),
    onTertiaryContainer = Color(0xFF201637),
    background = Color(0xFFFCFCFF),
    surface = Color(0xFFFCFCFF),
    onBackground = Color(0xFF1A1C1E),
    onSurface = Color(0xFF1A1C1E),
    surfaceContainer = Color(0xFFEDEEF3)
)

val SapphireDark = AppPalette(
    primary = Color(0xFF8DCDFF),
    onPrimary = Color(0xFF00344F),
    primaryContainer = Color(0xFF004B70),
    onPrimaryContainer = Color(0xFFCAE6FF),
    secondary = Color(0xFFB7C9D9),
    onSecondary = Color(0xFF22323F),
    secondaryContainer = Color(0xFF384956),
    onSecondaryContainer = Color(0xFFD3E5F5),
    tertiary = Color(0xFFCFC0E8),
    onTertiary = Color(0xFF352B4D),
    tertiaryContainer = Color(0xFF4C4165),
    onTertiaryContainer = Color(0xFFEBDDFF),
    background = Color(0xFF1A1C1E),
    surface = Color(0xFF1A1C1E),
    onBackground = Color(0xFFE2E2E6),
    onSurface = Color(0xFFE2E2E6),
    surfaceContainer = Color(0xFF202326)
)

// --- CRIMSON ---
val CrimsonLight = AppPalette(
    primary = Color(0xFFB91C1C),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFFFDAD6),
    onPrimaryContainer = Color(0xFF410002),
    secondary = Color(0xFF775652),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFFFDAD6),
    onSecondaryContainer = Color(0xFF2C1512),
    tertiary = Color(0xFF715B29),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFFDE2A7),
    onTertiaryContainer = Color(0xFF251A00),
    background = Color(0xFFFFFBFF),
    surface = Color(0xFFFFFBFF),
    onBackground = Color(0xFF201A19),
    onSurface = Color(0xFF201A19),
    surfaceContainer = Color(0xFFF9EEED)
)

val CrimsonDark = AppPalette(
    primary = Color(0xFFFFB4AB),
    onPrimary = Color(0xFF690005),
    primaryContainer = Color(0xFF93000A),
    onPrimaryContainer = Color(0xFFFFDAD6),
    secondary = Color(0xFFE7BDB8),
    onSecondary = Color(0xFF442926),
    secondaryContainer = Color(0xFF5D3F3C),
    onSecondaryContainer = Color(0xFFFFDAD6),
    tertiary = Color(0xFFDFC68D),
    onTertiary = Color(0xFF3F2E04),
    tertiaryContainer = Color(0xFF574416),
    onTertiaryContainer = Color(0xFFFDE2A7),
    background = Color(0xFF201A19),
    surface = Color(0xFF201A19),
    onBackground = Color(0xFFEDE0DE),
    onSurface = Color(0xFFEDE0DE),
    surfaceContainer = Color(0xFF241F1E)
)