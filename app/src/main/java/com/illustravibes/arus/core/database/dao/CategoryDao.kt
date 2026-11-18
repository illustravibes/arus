package com.illustravibes.arus.core.database.dao

import androidx.room.*
import com.illustravibes.arus.core.database.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {

    @Query("SELECT * FROM categories WHERE isArchived = 0 ORDER BY name ASC")
    fun getActiveCategoriesFlow(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE type = :type AND isArchived = 0 ORDER BY name ASC")
    fun getActiveCategoriesByTypeFlow(type: String): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE id = :id LIMIT 1")
    suspend fun getCategoryById(id: Long): CategoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: CategoryEntity): Long

    @Update
    suspend fun updateCategory(category: CategoryEntity)
}
