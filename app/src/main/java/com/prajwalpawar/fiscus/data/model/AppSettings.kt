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

enum class AppBarScrollPreference {
    PINNED,
    ENTER_ALWAYS,
    EXIT_UNTIL_COLLAPSED
}

data class AppSettings (
    val theme: ThemePreference = ThemePreference.SYSTEM,
    val appBar: AppBarPreference = AppBarPreference.SMALL,
    val bottomBar: BottomBarPreference = BottomBarPreference.SHOW_LABELS,
    val appBarScroll: AppBarScrollPreference =
        AppBarScrollPreference.EXIT_UNTIL_COLLAPSED
)