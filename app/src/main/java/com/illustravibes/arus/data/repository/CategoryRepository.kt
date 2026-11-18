package com.illustravibes.arus.data.repository

import com.illustravibes.arus.core.database.ArusDatabase
import com.illustravibes.arus.core.database.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow

class CategoryRepository(private val db: ArusDatabase) {

    val activeCategoriesFlow: Flow<List<CategoryEntity>> = db.categoryDao().getActiveCategoriesFlow()

    fun getCategoriesByTypeFlow(type: String): Flow<List<CategoryEntity>> {
        return db.categoryDao().getActiveCategoriesByTypeFlow(type)
    }

    suspend fun getCategoryById(id: Long): CategoryEntity? {
        return db.categoryDao().getCategoryById(id)
    }

    suspend fun createCategory(name: String, type: String, icon: String = "fa_tags", colorHex: String = "#00A86B"): Long {
        require(name.isNotBlank()) { "Nama kategori tidak boleh kosong" }
        require(type == "INCOME" || type == "EXPENSE") { "Tipe kategori tidak valid" }
        val category = CategoryEntity(
            name = name.trim(),
            type = type,
            icon = icon,
            colorHex = colorHex
        )
        return db.categoryDao().insertCategory(category)
    }

    suspend fun archiveCategory(category: CategoryEntity) {
        db.categoryDao().updateCategory(category.copy(isArchived = true))
    }
}
