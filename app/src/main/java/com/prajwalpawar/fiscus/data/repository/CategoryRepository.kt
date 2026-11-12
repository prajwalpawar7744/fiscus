package com.prajwalpawar.fiscus.data.repository

import com.prajwalpawar.fiscus.model.Category
import com.prajwalpawar.fiscus.model.CategoryKind
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {
    fun observeCategories(): Flow<List<Category>>
    fun observeActiveCategories(kind: CategoryKind): Flow<List<Category>>

    /** Inserts or updates; returns the category id. */
    suspend fun save(category: Category): Long
    suspend fun setArchived(id: Long, archived: Boolean)
}
