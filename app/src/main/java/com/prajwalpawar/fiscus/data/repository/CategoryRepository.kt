package com.prajwalpawar.fiscus.data.repository

import com.prajwalpawar.fiscus.data.local.dao.CategoryDao
import com.prajwalpawar.fiscus.data.local.entity.toCategory
import com.prajwalpawar.fiscus.data.local.entity.toEntity
import com.prajwalpawar.fiscus.data.model.Category
import com.prajwalpawar.fiscus.data.model.CategoryWithUsage
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

    val categoriesWithUsage: Flow<List<CategoryWithUsage>> =
        categoryDao.observeCategoriesWithUsage().map { rows ->
            rows.map { CategoryWithUsage(it.category.toCategory(), it.usageCount) }
        }

    suspend fun usageCount(id: Long): Int {
        return categoryDao.countUsage(id)
    }

    suspend fun activeCount(type: CategoryType): Int {
        return categoryDao.countActive(type.name)
    }

    suspend fun setArchived(id: Long, archived: Boolean) {
        categoryDao.setArchived(id, archived)
    }

    // Only unused, non-default categories can be deleted; anything else is archived instead.
    suspend fun deleteCategory(id: Long) {
        val category = requireNotNull(categoryDao.getCategory(id)) {
            "Category $id doesn't exist."
        }
        require(!category.isDefault) {
            "Default categories can't be deleted."
        }
        require(categoryDao.countUsage(id) == 0) {
            "A category used by transactions can't be deleted."
        }

        categoryDao.deleteCategory(id)
    }
}