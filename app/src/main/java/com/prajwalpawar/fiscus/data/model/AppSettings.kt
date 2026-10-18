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

enum class IconShapePreference {
    CIRCLE,
    SQUARE,
    COOKIE,
    CLOVER,
    SUNNY,
    FLOWER
}

data class AppSettings(
    val theme: ThemePreference = ThemePreference.SYSTEM,
    val dynamicColor: Boolean = true,
    val iconShape: IconShapePreference = IconShapePreference.COOKIE,
    val appBar: AppBarPreference = AppBarPreference.SMALL,
    val bottomBar: BottomBarPreference = BottomBarPreference.SHOW_LABELS,
    val appBarScroll: AppBarScrollPreference = AppBarScrollPreference.PINNED,
    val profileName: String = "",
    // Changes each time a profile photo is saved (0 = none), so the avatar reloads the file.
    val profilePhotoVersion: Long = 0L
)