package com.prajwalpawar.fiscus.data.db.entity

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.Index
import androidx.room3.PrimaryKey
import com.prajwalpawar.fiscus.model.CategoryKind

@Entity(tableName = "categories", indices = [Index("kind")])
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val kind: CategoryKind,
    val iconKey: String,
    val archived: Boolean,
    /** True for the starter categories (ADR-016), false for ones the user added (ADR-041). */
    @ColumnInfo(defaultValue = "0") val isSeeded: Boolean = false,
)
