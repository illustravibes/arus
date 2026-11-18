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
    val selectedTypeTab: String = "EXPENSE", // "EXPENSE" or "INCOME"
    val showDialog: Boolean = false,
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
            _uiState.map { it.selectedTypeTab }.distinctUntilChanged()
        ) { allCategories, type ->
            val filtered = allCategories.filter { it.type == type }
            _uiState.value.copy(categories = filtered, isLoading = false)
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
                nameInput = "",
                selectedIcon = "fa_tags",
                selectedColorHex = if (it.selectedTypeTab == "EXPENSE") "#F64932" else "#00A86B",
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

    fun saveCategory() {
        viewModelScope.launch {
            val state = _uiState.value
            if (state.nameInput.isBlank()) {
                _uiState.update { it.copy(errorMessage = "Nama kategori tidak boleh kosong") }
                return@launch
            }

            categoryRepository.createCategory(
                name = state.nameInput.trim(),
                type = state.selectedTypeTab,
                icon = state.selectedIcon,
                colorHex = state.selectedColorHex
            )

            _uiState.update { it.copy(showDialog = false) }
        }
    }

    fun archiveCategory(category: CategoryEntity) {
        viewModelScope.launch {
            categoryRepository.archiveCategory(category)
        }
    }
}
