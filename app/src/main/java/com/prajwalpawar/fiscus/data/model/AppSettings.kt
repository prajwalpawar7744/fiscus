package com.prajwalpawar.fiscus.data.model

enum class ThemePreference {
    SYSTEM,
    LIGHT,
    DARK
}

enum class AppBarPreference {
    SMALL,
    MEDIUM,
    LARGE
}

enum class BottomBarPreference {
    SHOW_LABELS,
    ICONS_ONLY,
    SHOW_LABELS_WHEN_SELECTED
}

data class AppSettings (
    val theme: ThemePreference = ThemePreference.SYSTEM,
    val appBar: AppBarPreference = AppBarPreference.SMALL,
    val bottomBar: BottomBarPreference = BottomBarPreference.SHOW_LABELS
)