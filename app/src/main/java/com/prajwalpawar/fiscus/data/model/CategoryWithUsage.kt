package com.prajwalpawar.fiscus.data.model

// A category and the number of transactions that use it.
data class CategoryWithUsage(
    val category: Category,
    val usageCount: Int
)
