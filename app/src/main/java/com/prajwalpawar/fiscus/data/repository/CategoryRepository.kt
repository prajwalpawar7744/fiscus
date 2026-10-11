package com.prajwalpawar.fiscus.data.repository

import com.prajwalpawar.fiscus.data.local.dao.CategoryDao
import com.prajwalpawar.fiscus.data.local.entity.toCategory
import com.prajwalpawar.fiscus.data.local.entity.toEntity
import com.prajwalpawar.fiscus.data.model.Category
import com.prajwalpawar.fiscus.data.model.CategoryType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CategoryRepository (
    private val categoryDao: CategoryDao
) {
    fun observeCategories(type: CategoryType): Flow<List<Category>> {
        return categoryDao.observeCategories(type.name).map { category ->
            category.map { it.toCategory() }
        }
    }

    suspend fun getCategory(id: Long): Category? {
        return categoryDao.getCategory(id)?.toCategory()
    }

    suspend fun addCategory(category: Category): Long {
        require(category.name.isNotBlank()) {
            "Category name cannot be empty."
        }

        return categoryDao.insertCategory(category.toEntity())
    }

    suspend fun updateCategory(category: Category) {
        require(category.name.isNotBlank()) {
            "Category name cannot be empty."
        }

        categoryDao.updateCategory(category.toEntity())
    }

    suspend fun archiveCategory(id: Long) {
        categoryDao.archiveCategory(id)
    }
}