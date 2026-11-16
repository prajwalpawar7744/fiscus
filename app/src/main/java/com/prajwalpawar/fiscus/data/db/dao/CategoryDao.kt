package com.prajwalpawar.fiscus.data.db.dao

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Upsert
import com.prajwalpawar.fiscus.data.db.entity.CategoryEntity
import com.prajwalpawar.fiscus.model.CategoryKind
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {

    @Query("SELECT * FROM categories ORDER BY kind, name")
    fun observeAll(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE kind = :kind AND archived = 0 ORDER BY name")
    fun observeActiveByKind(kind: CategoryKind): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE id = :id")
    fun observeById(id: Long): Flow<CategoryEntity?>

    /** How many transactions use each category (ADR-041). */
    @Query(
        """
        SELECT c.id AS categoryId,
            (SELECT COUNT(*) FROM transactions t WHERE t.categoryId = c.id) AS transactionCount
        FROM categories c
        """,
    )
    fun observeUsage(): Flow<List<CategoryUsage>>

    @Upsert
    suspend fun upsert(category: CategoryEntity): Long

    @Query("UPDATE categories SET archived = :archived WHERE id = :id")
    suspend fun setArchived(id: Long, archived: Boolean)

    /** Callers delete only unused categories (ADR-040); the FK would otherwise null `categoryId`. */
    @Query("DELETE FROM categories WHERE id = :id")
    suspend fun deleteById(id: Long)
}

data class CategoryUsage(val categoryId: Long, val transactionCount: Int)
