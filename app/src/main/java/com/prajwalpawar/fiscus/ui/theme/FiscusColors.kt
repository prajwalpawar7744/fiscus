package com.prajwalpawar.fiscus.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// Fiscus static scheme, used when Dynamic color is off or unavailable (Android 11 and older).
// Monochrome, like Material's monochrome scheme: every role comes from one neutral grey
// tonal palette (tones noted as T##), black/white primary; only error keeps its red so
// destructive actions still stand out.

val FiscusLightColorScheme = lightColorScheme(
    primary = Color(0xFF000000),              // T0
    onPrimary = Color(0xFFE2E2E2),            // T90
    primaryContainer = Color(0xFF3B3B3B),     // T25
    onPrimaryContainer = Color(0xFFFFFFFF),   // T100
    inversePrimary = Color(0xFFC6C6C6),       // T80

    secondary = Color(0xFF474747),            // T30
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFD4D4D4),   // T85
    onSecondaryContainer = Color(0xFF1B1B1B), // T10

    tertiary = Color(0xFF5E5E5E),             // T40
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFE2E2E2),    // T90
    onTertiaryContainer = Color(0xFF1B1B1B),

    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF93000A),

    background = Color(0xFFF9F9F9),           // T98
    onBackground = Color(0xFF1B1B1B),
    surface = Color(0xFFF9F9F9),
    onSurface = Color(0xFF1B1B1B),
    surfaceVariant = Color(0xFFE2E2E2),
    onSurfaceVariant = Color(0xFF474747),
    surfaceTint = Color(0xFF000000),
    inverseSurface = Color(0xFF303030),       // T20
    inverseOnSurface = Color(0xFFF1F1F1),     // T95
    outline = Color(0xFF777777),              // T50
    outlineVariant = Color(0xFFC6C6C6),
    scrim = Color(0xFF000000),

    surfaceBright = Color(0xFFF9F9F9),
    surfaceDim = Color(0xFFDADADA),           // T87
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF3F3F3),  // T96
    surfaceContainer = Color(0xFFEEEEEE),     // T94
    surfaceContainerHigh = Color(0xFFE8E8E8), // T92
    surfaceContainerHighest = Color(0xFFE2E2E2)
)

val FiscusDarkColorScheme = darkColorScheme(
    primary = Color(0xFFFFFFFF),              // T100
    onPrimary = Color(0xFF1B1B1B),            // T10
    primaryContainer = Color(0xFFD4D4D4),     // T85
    onPrimaryContainer = Color(0xFF000000),   // T0
    inversePrimary = Color(0xFF5E5E5E),       // T40

    secondary = Color(0xFFC6C6C6),            // T80
    onSecondary = Color(0xFF1B1B1B),
    secondaryContainer = Color(0xFF474747),   // T30
    onSecondaryContainer = Color(0xFFE2E2E2),

    tertiary = Color(0xFFABABAB),             // T70
    onTertiary = Color(0xFF1B1B1B),
    tertiaryContainer = Color(0xFF3B3B3B),    // T25
    onTertiaryContainer = Color(0xFFE2E2E2),

    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),

    background = Color(0xFF131313),           // T6
    onBackground = Color(0xFFE2E2E2),
    surface = Color(0xFF131313),
    onSurface = Color(0xFFE2E2E2),
    surfaceVariant = Color(0xFF474747),
    onSurfaceVariant = Color(0xFFC6C6C6),
    surfaceTint = Color(0xFFFFFFFF),
    inverseSurface = Color(0xFFE2E2E2),
    inverseOnSurface = Color(0xFF303030),
    outline = Color(0xFF919191),              // T60
    outlineVariant = Color(0xFF474747),
    scrim = Color(0xFF000000),

    surfaceBright = Color(0xFF393939),        // T24
    surfaceDim = Color(0xFF131313),
    surfaceContainerLowest = Color(0xFF0E0E0E), // T4
    surfaceContainerLow = Color(0xFF1B1B1B),  // T10
    surfaceContainer = Color(0xFF1F1F1F),     // T12
    surfaceContainerHigh = Color(0xFF2A2A2A), // T17
    surfaceContainerHighest = Color(0xFF353535) // T22
)
