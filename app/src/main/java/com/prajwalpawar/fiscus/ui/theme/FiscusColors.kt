package com.prajwalpawar.fiscus.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// Fiscus brand scheme, used when Dynamic color is off or unavailable (Android 11 and older).
// Built around the app icon: brown primary, warm beige neutrals, caramel tertiary.
// Tones follow Material's tonal pairs (container/on-container, surface ladder) for contrast.

val FiscusLightColorScheme = lightColorScheme(
    primary = Color(0xFF7A5233),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFFFDCC4),
    onPrimaryContainer = Color(0xFF5E3B1E),
    inversePrimary = Color(0xFFEFB992),

    secondary = Color(0xFF6F5B4D),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFF9DECD),
    onSecondaryContainer = Color(0xFF564336),

    tertiary = Color(0xFF825500),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFFFDDB0),
    onTertiaryContainer = Color(0xFF633F00),

    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF93000A),

    background = Color(0xFFFFF8F5),
    onBackground = Color(0xFF221A15),
    surface = Color(0xFFFFF8F5),
    onSurface = Color(0xFF221A15),
    surfaceVariant = Color(0xFFF3DFD4),
    onSurfaceVariant = Color(0xFF52443B),
    surfaceTint = Color(0xFF7A5233),
    inverseSurface = Color(0xFF382E29),
    inverseOnSurface = Color(0xFFFEEDE5),
    outline = Color(0xFF84746A),
    outlineVariant = Color(0xFFD6C3B7),
    scrim = Color(0xFF000000),

    surfaceBright = Color(0xFFFFF8F5),
    surfaceDim = Color(0xFFE7D7CF),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFFFF1EA),
    surfaceContainer = Color(0xFFFBEBE3),
    surfaceContainerHigh = Color(0xFFF5E5DD),
    surfaceContainerHighest = Color(0xFFEFE0D8)
)

val FiscusDarkColorScheme = darkColorScheme(
    primary = Color(0xFFEFB992),
    onPrimary = Color(0xFF47280C),
    primaryContainer = Color(0xFF5F3D20),
    onPrimaryContainer = Color(0xFFFFDCC4),
    inversePrimary = Color(0xFF7A5233),

    secondary = Color(0xFFDDC2B1),
    onSecondary = Color(0xFF3E2D21),
    secondaryContainer = Color(0xFF564336),
    onSecondaryContainer = Color(0xFFF9DECD),

    tertiary = Color(0xFFFFB95C),
    onTertiary = Color(0xFF452B00),
    tertiaryContainer = Color(0xFF633F00),
    onTertiaryContainer = Color(0xFFFFDDB0),

    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),

    background = Color(0xFF19120D),
    onBackground = Color(0xFFEFE0D8),
    surface = Color(0xFF19120D),
    onSurface = Color(0xFFEFE0D8),
    surfaceVariant = Color(0xFF52443B),
    onSurfaceVariant = Color(0xFFD6C3B7),
    surfaceTint = Color(0xFFEFB992),
    inverseSurface = Color(0xFFEFE0D8),
    inverseOnSurface = Color(0xFF382E29),
    outline = Color(0xFF9F8D83),
    outlineVariant = Color(0xFF52443B),
    scrim = Color(0xFF000000),

    surfaceBright = Color(0xFF413732),
    surfaceDim = Color(0xFF19120D),
    surfaceContainerLowest = Color(0xFF140D09),
    surfaceContainerLow = Color(0xFF221A15),
    surfaceContainer = Color(0xFF261E19),
    surfaceContainerHigh = Color(0xFF312823),
    surfaceContainerHighest = Color(0xFF3C332D)
)
