package com.illustravibes.arus.data.repository

import com.illustravibes.arus.core.database.ArusDatabase
import com.illustravibes.arus.core.database.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow

class CategoryRepository(private val db: ArusDatabase) {

    val activeCategoriesFlow: Flow<List<CategoryEntity>> = db.categoryDao().getActiveCategoriesFlow()

    // V2-P1-5: peta tampilan memakai SEMUA kategori (termasuk terarsip).
    val allCategoriesFlow: Flow<List<CategoryEntity>> = db.categoryDao().getAllCategoriesFlow()

    val archivedCategoriesFlow: Flow<List<CategoryEntity>> = db.categoryDao().getArchivedCategoriesFlow()

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

    // V2-P1-5: batalkan arsip — kategori muncul lagi di pemilih.
    suspend fun unarchiveCategory(category: CategoryEntity) {
        db.categoryDao().updateCategory(category.copy(isArchived = false))
    }

    suspend fun updateCategory(category: CategoryEntity) {
        db.categoryDao().updateCategory(category)
    }

    suspend fun deleteCategory(category: CategoryEntity) {
        db.categoryDao().deleteCategory(category)
    }
}
