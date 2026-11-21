package com.illustravibes.arus.feature.category

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.illustravibes.arus.core.database.entity.CategoryEntity
import com.illustravibes.arus.data.repository.CategoryRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class CategoryUiState(
    val isLoading: Boolean = true,
    val categories: List<CategoryEntity> = emptyList(),
    // V2-P1-5: daftar arsip + semua kategori (untuk cek duplikat lintas status).
    val archivedCategories: List<CategoryEntity> = emptyList(),
    val allCategories: List<CategoryEntity> = emptyList(),
    val selectedTypeTab: String = "EXPENSE", // "EXPENSE" or "INCOME"
    val showDialog: Boolean = false,
    val editingCategory: CategoryEntity? = null,
    val nameInput: String = "",
    val selectedIcon: String = "fa_tags",
    val selectedColorHex: String = "#00A86B",
    val errorMessage: String? = null
)

class CategoryViewModel(
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CategoryUiState())
    val uiState: StateFlow<CategoryUiState> = _uiState.asStateFlow()

    init {
        loadCategories()
    }

    private fun loadCategories() {
        combine(
            categoryRepository.activeCategoriesFlow,
            categoryRepository.archivedCategoriesFlow,
            categoryRepository.allCategoriesFlow,
            _uiState.map { it.selectedTypeTab }.distinctUntilChanged()
        ) { active, archived, all, type ->
            _uiState.value.copy(
                categories = active.filter { it.type == type },
                archivedCategories = archived.filter { it.type == type },
                allCategories = all,
                isLoading = false
            )
        }.onEach { state ->
            _uiState.value = state
        }.launchIn(viewModelScope)
    }

    fun onTypeTabChange(type: String) {
        _uiState.update { it.copy(selectedTypeTab = type) }
    }

    fun openCreateDialog() {
        _uiState.update {
            it.copy(
                showDialog = true,
                editingCategory = null,
                nameInput = "",
                selectedIcon = "fa_tags",
                selectedColorHex = if (it.selectedTypeTab == "EXPENSE") "#F64932" else "#00A86B",
                errorMessage = null
            )
        }
    }

    fun openEditDialog(category: CategoryEntity) {
        _uiState.update {
            it.copy(
                showDialog = true,
                editingCategory = category,
                nameInput = category.name,
                selectedIcon = category.icon,
                selectedColorHex = category.colorHex,
                errorMessage = null
            )
        }
    }

    fun closeDialog() {
        _uiState.update { it.copy(showDialog = false) }
    }

    fun onNameChange(name: String) {
        _uiState.update { it.copy(nameInput = name, errorMessage = null) }
    }

    fun onIconChange(icon: String) {
        _uiState.update { it.copy(selectedIcon = icon) }
    }

    fun onColorChange(colorHex: String) {
        _uiState.update { it.copy(selectedColorHex = colorHex) }
    }

    fun saveCategory() {
        viewModelScope.launch {
            val state = _uiState.value
            if (state.nameInput.isBlank()) {
                _uiState.update { it.copy(errorMessage = "Nama kategori tidak boleh kosong") }
                return@launch
            }

            // V2-P2-6: nama duplikat (abaikan kapitalisasi/spasi, termasuk
            // yang terarsip) ditolak — pulihkan yang lama, jangan buat baru.
            // Saat edit, nama sendiri dikecualikan; tipe ikut kategori
            // yang diedit (tipe tidak berubah).
            val type = state.editingCategory?.type ?: state.selectedTypeTab
            val normalizedName = state.nameInput.trim()
            val isDuplicateName = state.allCategories.any {
                it.type == type && it.name.trim().equals(normalizedName, ignoreCase = true) &&
                    it.id != state.editingCategory?.id
            }
            if (isDuplicateName) {
                _uiState.update { it.copy(errorMessage = "Nama kategori sudah dipakai") }
                return@launch
            }

            if (state.editingCategory == null) {
                categoryRepository.createCategory(
                    name = state.nameInput.trim(),
                    type = state.selectedTypeTab,
                    icon = state.selectedIcon,
                    colorHex = state.selectedColorHex
                )
            } else {
                categoryRepository.updateCategory(
                    state.editingCategory.copy(
                        name = state.nameInput.trim(),
                        icon = state.selectedIcon,
                        colorHex = state.selectedColorHex
                    )
                )
            }

            _uiState.update { it.copy(showDialog = false) }
        }
    }

    fun archiveCategory(category: CategoryEntity) {
        viewModelScope.launch {
            categoryRepository.archiveCategory(category)
        }
    }

    // V2-P1-5: batalkan arsip — kategori muncul lagi di pemilih.
    fun unarchiveCategory(category: CategoryEntity) {
        viewModelScope.launch {
            categoryRepository.unarchiveCategory(category)
        }
    }

    fun deleteCategory(category: CategoryEntity) {
        viewModelScope.launch {
            categoryRepository.deleteCategory(category)
        }
    }
}
