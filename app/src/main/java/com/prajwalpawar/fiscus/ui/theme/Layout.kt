package com.prajwalpawar.fiscus.ui.theme

import androidx.compose.ui.unit.dp

/**
 * App-wide layout dimensions (ADR-020, user decision 2026-11-15). Material 3 has no public spacing
 * tokens in Compose, so these few values live in one place instead of being repeated per screen.
 * Values follow M3 layout guidance (16dp compact margins, 8dp grid). Component sizes, shapes,
 * colors and motion still come from M3 components and `MaterialTheme`. Never add those here.
 */
object FiscusLayout {
    /** Horizontal screen margin and padding inside containers (M3 compact margin). */
    val ScreenMargin = 16.dp

    /** Vertical gap between form or content sections. */
    val SectionSpacing = 24.dp

    /** Gap between related items: options in a group, header icon and label. */
    val ItemSpacing = 8.dp

    /** Gap between tightly coupled lines, e.g. a label above its value. */
    val TightSpacing = 4.dp

    /** Content column width cap, so forms stay readable on medium and expanded widths. */
    val MaxContentWidth = 600.dp

    /** Size of a [androidx.compose.material3.MaterialShapes] icon accent. */
    val AccentSize = 40.dp

    /** An icon set inline with text, e.g. the arrow before a list amount (ADR-026). */
    val InlineIconSize = 18.dp

    /** Size of the large shape accent in empty states, and of its icon (ADR-023). */
    val LargeAccentSize = 96.dp
    val LargeAccentIconSize = 48.dp

    /**
     * Preferred width of a large carousel item, from the official `HorizontalMultiBrowseCarousel`
     * sample (androidx CarouselSamples.kt, checked 2026-11-16; ADR-034). The carousel adjusts it.
     */
    val CarouselItemWidth = 186.dp
}
