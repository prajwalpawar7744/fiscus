package com.prajwalpawar.fiscus.model

/** A category with how many transactions use it (ADR-041). */
data class CategoryWithUsage(val category: Category, val transactionCount: Int)
