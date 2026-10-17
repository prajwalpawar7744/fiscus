package com.prajwalpawar.fiscus.ui.navigation

import com.prajwalpawar.fiscus.data.model.CategoryType

// Full-screen pages outside the bottom navigation; open them from anywhere.
object FiscusRoutes {
    const val TYPE_ARG = "type"
    const val ID_ARG = "id"

    const val MANAGE_CATEGORIES = "categories"
    const val MANAGE_ACCOUNTS = "accounts"

    const val NEW_CATEGORY_BASE = "category/new"
    const val NEW_CATEGORY = "$NEW_CATEGORY_BASE?$TYPE_ARG={$TYPE_ARG}"

    const val NEW_ACCOUNT = "account/new"

    const val EDIT_CATEGORY_BASE = "category/edit"
    const val EDIT_CATEGORY = "$EDIT_CATEGORY_BASE/{$ID_ARG}"

    const val EDIT_ACCOUNT_BASE = "account/edit"
    const val EDIT_ACCOUNT = "$EDIT_ACCOUNT_BASE/{$ID_ARG}"

    const val TRANSACTION_DETAILS_BASE = "transaction"
    const val TRANSACTION_DETAILS = "$TRANSACTION_DETAILS_BASE/{$ID_ARG}"

    fun transactionDetails(id: Long): String = "$TRANSACTION_DETAILS_BASE/$id"

    fun newCategory(type: CategoryType): String = "$NEW_CATEGORY_BASE?$TYPE_ARG=${type.name}"

    fun editCategory(id: Long): String = "$EDIT_CATEGORY_BASE/$id"

    fun editAccount(id: Long): String = "$EDIT_ACCOUNT_BASE/$id"
}
