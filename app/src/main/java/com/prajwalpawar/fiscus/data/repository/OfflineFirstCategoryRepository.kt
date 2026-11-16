package com.prajwalpawar.fiscus.data.repository

import com.prajwalpawar.fiscus.core.coroutines.IoDispatcher
import com.prajwalpawar.fiscus.data.db.dao.CategoryDao
import com.prajwalpawar.fiscus.data.db.mapper.asEntity
import com.prajwalpawar.fiscus.data.db.mapper.asModel
import com.prajwalpawar.fiscus.model.Category
import com.prajwalpawar.fiscus.model.CategoryKind
import com.prajwalpawar.fiscus.model.CategoryWithUsage
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

class OfflineFirstCategoryRepository @Inject constructor(
    private val categoryDao: CategoryDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : CategoryRepository {

    override fun observeCategories(): Flow<List<Category>> =
        categoryDao.observeAll().map { entities -> entities.map { it.asModel() } }.flowOn(ioDispatcher)

    override fun observeActiveCategories(kind: CategoryKind): Flow<List<Category>> =
        categoryDao.observeActiveByKind(kind).map { entities -> entities.map { it.asModel() } }
            .flowOn(ioDispatcher)

    override fun observeCategoriesWithUsage(): Flow<List<CategoryWithUsage>> =
        combine(categoryDao.observeAll(), categoryDao.observeUsage()) { entities, usage ->
            val countById = usage.associate { it.categoryId to it.transactionCount }
            entities.map { CategoryWithUsage(it.asModel(), countById[it.id] ?: 0) }
        }.flowOn(ioDispatcher)

    override fun observeCategory(id: Long): Flow<Category?> =
        categoryDao.observeById(id).map { it?.asModel() }.flowOn(ioDispatcher)

    override suspend fun save(category: Category): Long = withContext(ioDispatcher) {
        categoryDao.upsert(category.asEntity()).takeIf { it > 0 } ?: category.id
    }

    override suspend fun setArchived(id: Long, archived: Boolean) = withContext(ioDispatcher) {
        categoryDao.setArchived(id, archived)
    }

    override suspend fun delete(id: Long) = withContext(ioDispatcher) {
        categoryDao.deleteById(id)
    }
}
