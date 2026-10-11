package com.prajwalpawar.fiscus.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.prajwalpawar.fiscus.data.model.Category
import com.prajwalpawar.fiscus.data.model.CategoryType

@Entity(
    tableName = "categories",
    indices = [
        Index(
            value = ["name", "type"],
            unique = true
        )
    ]
)
data class CategoryEntity (
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String,
    val type: String,
    val iconKey: String = "category",
    val isDefault: Boolean = false,
    val isArchived: Boolean = false
)

fun CategoryEntity.toCategory(): Category {
    return Category(
        id = id,
        name = name,
        type = CategoryType.valueOf(type),
        iconKey = iconKey,
        isDefault = isDefault,
        isArchived = isArchived
    )
}

fun Category.toEntity(): CategoryEntity {
    return CategoryEntity(
        id = id,
        name = name,
        type = type.name,
        iconKey = iconKey,
        isDefault = isDefault,
        isArchived = isArchived
    )
}