package com.prajwalpawar.fiscus.data.repository

import com.prajwalpawar.fiscus.model.Category
import com.prajwalpawar.fiscus.model.CategoryKind
import com.prajwalpawar.fiscus.model.CategoryWithUsage
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {
    fun observeCategories(): Flow<List<Category>>
    fun observeActiveCategories(kind: CategoryKind): Flow<List<Category>>

    /** Every category with its transaction count (ADR-041). */
    fun observeCategoriesWithUsage(): Flow<List<CategoryWithUsage>>

    /** One category, or null once it's deleted. */
    fun observeCategory(id: Long): Flow<Category?>

    /** Inserts or updates; returns the category id. */
    suspend fun save(category: Category): Long
    suspend fun setArchived(id: Long, archived: Boolean)

    /** Only for categories no transaction uses; used ones are archived instead (ADR-040). */
    suspend fun delete(id: Long)
}
