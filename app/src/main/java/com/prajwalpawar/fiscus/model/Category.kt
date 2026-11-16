package com.prajwalpawar.fiscus.model

/**
 * Classifies income or expense. [iconKey] is a Material Symbols name (e.g. "restaurant"),
 * mapped to a drawable in the UI layer (ADR-016).
 */
data class Category(
    val id: Long = 0L,
    val name: String,
    val kind: CategoryKind,
    val iconKey: String,
    val archived: Boolean = false,
    /** A starter category (shown as "Default"); false when the user added it (ADR-041). */
    val isSeeded: Boolean = false,
) {
    init {
        require(name.isNotBlank()) { "Category name must not be blank" }
    }
}

enum class CategoryKind { INCOME, EXPENSE }
