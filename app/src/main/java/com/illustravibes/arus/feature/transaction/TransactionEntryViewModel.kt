package com.illustravibes.arus.feature.transaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.illustravibes.arus.core.database.entity.CategoryEntity
import com.illustravibes.arus.core.database.entity.WalletEntity
import com.illustravibes.arus.data.repository.CategoryRepository
import com.illustravibes.arus.data.repository.TransactionRepository
import com.illustravibes.arus.data.repository.WalletRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class TransactionEntryUiState(
    val typeIndex: Int = 1, // 0 = Pemasukan, 1 = Pengeluaran, 2 = Transfer
    val amountInput: String = "",
    val selectedWallet: WalletEntity? = null,
    val selectedTargetWallet: WalletEntity? = null,
    val selectedCategory: CategoryEntity? = null,
    val dateMillis: Long = System.currentTimeMillis(),
    val note: String = "",
    val wallets: List<WalletEntity> = emptyList(),
    val categories: List<CategoryEntity> = emptyList(),
    val errorMessage: String? = null,
    val isSuccess: Boolean = false
)

class TransactionEntryViewModel(
    private val walletRepository: WalletRepository,
    private val categoryRepository: CategoryRepository,
    private val transactionRepository: TransactionRepository,
    initialTypeIndex: Int = 1
) : ViewModel() {

    private val _uiState = MutableStateFlow(TransactionEntryUiState(typeIndex = initialTypeIndex))
    val uiState: StateFlow<TransactionEntryUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        combine(
            walletRepository.walletsFlow,
            categoryRepository.activeCategoriesFlow
        ) { wallets, categories ->
            val state = _uiState.value
            val currentType = if (state.typeIndex == 0) "INCOME" else "EXPENSE"
            val filteredCategories = categories.filter { it.type == currentType }

            state.copy(
                wallets = wallets,
                categories = filteredCategories,
                selectedWallet = state.selectedWallet ?: wallets.firstOrNull(),
                selectedTargetWallet = state.selectedTargetWallet ?: wallets.getOrNull(1),
                selectedCategory = state.selectedCategory ?: filteredCategories.firstOrNull()
            )
        }.onEach { state ->
            _uiState.value = state
        }.launchIn(viewModelScope)
    }

    fun onTypeChange(index: Int) {
        viewModelScope.launch {
            val type = if (index == 0) "INCOME" else "EXPENSE"
            val categories = categoryRepository.getCategoriesByTypeFlow(type).firstOrNull() ?: emptyList()
            _uiState.update { state ->
                state.copy(
                    typeIndex = index,
                    categories = categories,
                    selectedCategory = categories.firstOrNull(),
                    errorMessage = null
                )
            }
        }
    }

    fun onAmountChange(input: String) {
        val cleanDigits = input.filter { it.isDigit() }
        _uiState.update { it.copy(amountInput = cleanDigits, errorMessage = null) }
    }

    fun onWalletSelect(wallet: WalletEntity) {
        _uiState.update { it.copy(selectedWallet = wallet, errorMessage = null) }
    }

    fun onTargetWalletSelect(wallet: WalletEntity) {
        _uiState.update { it.copy(selectedTargetWallet = wallet, errorMessage = null) }
    }

    fun onCategorySelect(category: CategoryEntity) {
        _uiState.update { it.copy(selectedCategory = category, errorMessage = null) }
    }

    fun onDateChange(millis: Long) {
        _uiState.update { it.copy(dateMillis = millis) }
    }

    fun onNoteChange(note: String) {
        _uiState.update { it.copy(note = note) }
    }

    fun saveTransaction() {
        viewModelScope.launch {
            val state = _uiState.value
            val amount = state.amountInput.toLongOrNull() ?: 0L

            if (amount <= 0L) {
                _uiState.update { it.copy(errorMessage = "Nominal transaksi harus lebih dari Rp0") }
                return@launch
            }

            if (state.selectedWallet == null) {
                _uiState.update { it.copy(errorMessage = "Pilih dompet sumber transaksi") }
                return@launch
            }

            when (state.typeIndex) {
                0 -> { // Pemasukan
                    if (state.selectedCategory == null) {
                        _uiState.update { it.copy(errorMessage = "Pilih kategori pemasukan") }
                        return@launch
                    }
                    transactionRepository.addIncome(
                        amount = amount,
                        dateMillis = state.dateMillis,
                        walletId = state.selectedWallet.id,
                        categoryId = state.selectedCategory.id,
                        note = state.note
                    )
                }
                1 -> { // Pengeluaran
                    if (state.selectedCategory == null) {
                        _uiState.update { it.copy(errorMessage = "Pilih kategori pengeluaran") }
                        return@launch
                    }
                    transactionRepository.addExpense(
                        amount = amount,
                        dateMillis = state.dateMillis,
                        walletId = state.selectedWallet.id,
                        categoryId = state.selectedCategory.id,
                        note = state.note
                    )
                }
                2 -> { // Transfer
                    if (state.selectedTargetWallet == null) {
                        _uiState.update { it.copy(errorMessage = "Pilih dompet tujuan transfer") }
                        return@launch
                    }
                    if (state.selectedWallet.id == state.selectedTargetWallet.id) {
                        _uiState.update { it.copy(errorMessage = "Dompet asal dan tujuan tidak boleh sama") }
                        return@launch
                    }
                    transactionRepository.addTransfer(
                        amount = amount,
                        dateMillis = state.dateMillis,
                        sourceWalletId = state.selectedWallet.id,
                        targetWalletId = state.selectedTargetWallet.id,
                        note = state.note
                    )
                }
            }

            _uiState.update { it.copy(isSuccess = true) }
        }
    }
}
