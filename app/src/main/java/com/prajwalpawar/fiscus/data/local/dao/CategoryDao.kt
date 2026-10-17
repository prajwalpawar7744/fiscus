package com.prajwalpawar.fiscus.data.local.dao

import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.prajwalpawar.fiscus.data.local.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow

data class CategoryUsageRow(
    @Embedded val category: CategoryEntity,
    val usageCount: Int
)

@Dao
interface CategoryDao {
    @Query(
        """
            SELECT *
        FROM categories
        WHERE type = :type
          AND isArchived = 0
        ORDER BY isDefault DESC, name COLLATE NOCASE ASC
        """
    )
    fun observeCategories(type: String): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE id = :id")
    suspend fun getCategory(id: Long): CategoryEntity?

    @Insert
    suspend fun insertCategory(category: CategoryEntity): Long

    @Update
    suspend fun updateCategory(category: CategoryEntity)

    // Every category, archived ones last, with how many transactions use each.
    @Query(
        """
        SELECT
            c.*,
            (SELECT COUNT(*) FROM transactions AS t WHERE t.categoryId = c.id) AS usageCount
        FROM categories AS c
        ORDER BY c.isArchived ASC, c.type ASC, c.isDefault DESC, c.name COLLATE NOCASE ASC
        """
    )
    fun observeCategoriesWithUsage(): Flow<List<CategoryUsageRow>>

    @Query("SELECT COUNT(*) FROM transactions WHERE categoryId = :id")
    suspend fun countUsage(id: Long): Int

    @Query("SELECT COUNT(*) FROM categories WHERE type = :type AND isArchived = 0")
    suspend fun countActive(type: String): Int

    @Query("UPDATE categories SET isArchived = :archived WHERE id = :id")
    suspend fun setArchived(id: Long, archived: Boolean)

    @Query("DELETE FROM categories WHERE id = :id")
    suspend fun deleteCategory(id: Long)
}