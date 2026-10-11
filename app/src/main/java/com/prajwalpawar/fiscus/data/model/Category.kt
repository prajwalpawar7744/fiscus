package com.prajwalpawar.fiscus.data.model

data class Category (
    val id: Long = 0L,
    val name: String,
    val type: CategoryType,
    val iconKey: String = "category",
    val isDefault: Boolean = false,
    val isArchived: Boolean = false
)