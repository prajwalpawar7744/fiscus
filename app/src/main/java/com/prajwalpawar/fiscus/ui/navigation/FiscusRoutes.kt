package com.prajwalpawar.fiscus.ui.navigation

import com.prajwalpawar.fiscus.data.model.CategoryType

// Full-screen pages outside the bottom navigation; open them from anywhere.
object FiscusRoutes {
    const val TYPE_ARG = "type"

    const val NEW_CATEGORY_BASE = "category/new"
    const val NEW_CATEGORY = "$NEW_CATEGORY_BASE?$TYPE_ARG={$TYPE_ARG}"

    const val NEW_ACCOUNT = "account/new"

    fun newCategory(type: CategoryType): String = "$NEW_CATEGORY_BASE?$TYPE_ARG=${type.name}"
}
