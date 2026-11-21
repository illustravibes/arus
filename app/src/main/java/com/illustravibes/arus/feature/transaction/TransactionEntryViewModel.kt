package com.illustravibes.arus.feature.transaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.illustravibes.arus.core.database.entity.CategoryEntity
import com.illustravibes.arus.core.database.entity.TransactionEntity
import com.illustravibes.arus.core.database.entity.WalletEntity
import com.illustravibes.arus.data.repository.CategoryRepository
import com.illustravibes.arus.data.repository.TransactionRepository
import com.illustravibes.arus.data.repository.WalletRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.TimeZone

data class TransactionEntryUiState(
    val typeIndex: Int = 1, // 0 = Pemasukan, 1 = Pengeluaran, 2 = Transfer
    val amountInput: String = "",
    val selectedWallet: WalletEntity? = null,
    val selectedTargetWallet: WalletEntity? = null,
    val selectedCategory: CategoryEntity? = null,
    val dateMillis: Long = System.currentTimeMillis(),
    // A1 (audit awam): true bila tanggal/jam dipilih manual lewat sheet
    // atau berasal dari transaksi yang diedit — saat itu jam disimpan apa
    // adanya; bila false, jam diambil saat Simpan ditekan, bukan saat
    // form dibuka.
    val dateManuallyPicked: Boolean = false,
    val note: String = "",
    val wallets: List<WalletEntity> = emptyList(),
    val categories: List<CategoryEntity> = emptyList(),
    val errorMessage: String? = null,
    val isSuccess: Boolean = false,
    val editTransactionId: Long? = null
)

class TransactionEntryViewModel(
    private val walletRepository: WalletRepository,
    private val categoryRepository: CategoryRepository,
    private val transactionRepository: TransactionRepository,
    initialTypeIndex: Int = 1,
    editTransactionId: Long? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(TransactionEntryUiState(typeIndex = initialTypeIndex))
    val uiState: StateFlow<TransactionEntryUiState> = _uiState.asStateFlow()

    // Mode ubah: id dompet/kategori asal transaksi yang sedang diedit.
    // Dipakai sekali saat data pertama dimuat, lalu dikosongkan supaya
    // pilihan pengguna sesudahnya tidak tertimpa.
    private var pendingWalletId: Long? = null
    private var pendingTargetWalletId: Long? = null
    private var pendingCategoryId: Long? = null
    private var editOriginalCreatedAt: Long = 0L
    private var editOriginalTransferId: String? = null

    init {
        viewModelScope.launch {
            val tx = editTransactionId?.let { transactionRepository.getTransactionById(it) }
            if (tx != null) {
                pendingWalletId = tx.walletId
                pendingTargetWalletId = tx.targetWalletId
                pendingCategoryId = tx.categoryId
                editOriginalCreatedAt = tx.createdAt
                editOriginalTransferId = tx.transferId
                _uiState.update {
                    it.copy(
                        editTransactionId = tx.id,
                        typeIndex = when (tx.type) {
                            "INCOME" -> 0
                            "TRANSFER" -> 2
                            else -> 1
                        },
                        amountInput = tx.amount.toString(),
                        dateMillis = tx.dateMillis,
                        dateManuallyPicked = true,
                        note = tx.note ?: ""
                    )
                }
            }
            loadData()
        }
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
                selectedWallet = wallets.find { it.id == pendingWalletId }
                    ?: state.selectedWallet ?: wallets.firstOrNull(),
                selectedTargetWallet = wallets.find { it.id == pendingTargetWalletId }
                    ?: state.selectedTargetWallet ?: wallets.getOrNull(1),
                selectedCategory = filteredCategories.find { it.id == pendingCategoryId }
                    ?: state.selectedCategory ?: filteredCategories.firstOrNull()
            )
        }.onEach { state ->
            pendingWalletId = null
            pendingTargetWalletId = null
            pendingCategoryId = null
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

    // Sheet tanggal & jam mengembalikan timestamp lokal lengkap (tanggal +
    // jam yang dipilih) — langsung dipakai apa adanya, tanpa merge.
    fun onDatePicked(dateTimeMillis: Long) {
        _uiState.update { it.copy(dateMillis = dateTimeMillis, dateManuallyPicked = true) }
    }

    fun onNoteChange(note: String) {
        _uiState.update { it.copy(note = note) }
    }

    private fun buildUpdatedTransaction(state: TransactionEntryUiState, amount: Long, dateMillis: Long): TransactionEntity {
        val type = when (state.typeIndex) {
            0 -> "INCOME"
            1 -> "EXPENSE"
            else -> "TRANSFER"
        }
        return TransactionEntity(
            id = state.editTransactionId!!,
            type = type,
            amount = amount,
            dateMillis = dateMillis,
            walletId = state.selectedWallet!!.id,
            targetWalletId = if (state.typeIndex == 2) state.selectedTargetWallet?.id else null,
            categoryId = if (state.typeIndex == 2) null else state.selectedCategory?.id,
            note = state.note.trim(),
            transferId = if (state.typeIndex == 2) editOriginalTransferId else null,
            createdAt = editOriginalCreatedAt
        )
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

            // Validasi per tipe (berlaku untuk buat baru maupun ubah).
            when (state.typeIndex) {
                0, 1 -> {
                    if (state.selectedCategory == null) {
                        _uiState.update { it.copy(errorMessage = if (state.typeIndex == 0) "Pilih kategori pemasukan" else "Pilih kategori pengeluaran") }
                        return@launch
                    }
                }
                2 -> {
                    if (state.selectedTargetWallet == null) {
                        _uiState.update { it.copy(errorMessage = "Pilih dompet tujuan transfer") }
                        return@launch
                    }
                    if (state.selectedWallet.id == state.selectedTargetWallet.id) {
                        _uiState.update { it.copy(errorMessage = "Dompet asal dan tujuan tidak boleh sama") }
                        return@launch
                    }
                }
            }

            // A1 (audit awam): jam transaksi = jam Simpan ditekan, kecuali
            // pengguna memilih tanggal/jam sendiri atau sedang mengedit
            // (saat itu tanggal asli dipertahankan).
            val effectiveDateMillis = if (state.editTransactionId != null || state.dateManuallyPicked) state.dateMillis else System.currentTimeMillis()

            if (state.editTransactionId != null) {
                transactionRepository.updateTransaction(buildUpdatedTransaction(state, amount, effectiveDateMillis))
            } else when (state.typeIndex) {
                0 -> { // Pemasukan
                    transactionRepository.addIncome(
                        amount = amount,
                        dateMillis = effectiveDateMillis,
                        walletId = state.selectedWallet.id,
                        categoryId = state.selectedCategory!!.id,
                        note = state.note
                    )
                }
                1 -> { // Pengeluaran
                    transactionRepository.addExpense(
                        amount = amount,
                        dateMillis = effectiveDateMillis,
                        walletId = state.selectedWallet.id,
                        categoryId = state.selectedCategory!!.id,
                        note = state.note
                    )
                }
                2 -> { // Transfer
                    transactionRepository.addTransfer(
                        amount = amount,
                        dateMillis = effectiveDateMillis,
                        sourceWalletId = state.selectedWallet.id,
                        targetWalletId = state.selectedTargetWallet!!.id,
                        note = state.note
                    )
                }
            }

            _uiState.update { it.copy(isSuccess = true) }
        }
    }
}
