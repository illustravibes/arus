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

// Filter tanggal Riwayat: preset yang ditawarkan sheet Filter Transaksi.
enum class HistoryDateFilter { ALL, TODAY, LAST_7_DAYS, LAST_30_DAYS, THIS_MONTH }

private const val DAY_MILLIS = 24L * 60 * 60 * 1000

private data class HistoryFilters(
    val query: String,
    val walletId: Long?,
    val type: String?,
    val categoryId: Long?,
    val date: HistoryDateFilter
)

data class TransactionHistoryUiState(
    val isLoading: Boolean = true,
    val searchQuery: String = "",
    val selectedWalletFilterId: Long? = null,
    val selectedTypeFilter: String? = null, // "INCOME", "EXPENSE", "TRANSFER", or null
    val selectedCategoryFilterId: Long? = null,
    val selectedDateFilter: HistoryDateFilter = HistoryDateFilter.ALL,
    val categories: List<CategoryEntity> = emptyList(), // aktif saja, untuk sheet filter
    val transactions: List<TransactionEntity> = emptyList(),
    val totalTransactionCount: Int = 0,
    val wallets: List<WalletEntity> = emptyList(),
    val categoriesMap: Map<Long, CategoryEntity> = emptyMap(),
    val transactionToDelete: TransactionEntity? = null
)

class TransactionHistoryViewModel(
    private val walletRepository: WalletRepository,
    private val categoryRepository: CategoryRepository,
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TransactionHistoryUiState())
    val uiState: StateFlow<TransactionHistoryUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        combine(
            transactionRepository.allTransactionsFlow,
            walletRepository.walletsFlow,
            // V2-P1-5: peta tampilan pakai semua kategori (termasuk terarsip).
            categoryRepository.allCategoriesFlow,
            categoryRepository.activeCategoriesFlow,
            _uiState.map {
                HistoryFilters(
                    it.searchQuery,
                    it.selectedWalletFilterId,
                    it.selectedTypeFilter,
                    it.selectedCategoryFilterId,
                    it.selectedDateFilter
                )
            }.distinctUntilChanged()
        ) { allTransactions, wallets, categories, activeCategories, filters ->
            val (query, walletFilter, typeFilter, categoryFilter, dateFilter) = filters
            val categoriesMap = categories.associateBy { it.id }

            // Batas tanggal lokal (jam 00:00) untuk filter Tanggal.
            val startOfToday = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
            }.timeInMillis
            val startOfTomorrow = startOfToday + DAY_MILLIS
            val startOfMonth = Calendar.getInstance().apply {
                set(Calendar.DAY_OF_MONTH, 1); set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
            }.timeInMillis
            val startOfNextMonth = Calendar.getInstance().apply {
                set(Calendar.DAY_OF_MONTH, 1); set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
                add(Calendar.MONTH, 1)
            }.timeInMillis

            val filtered = allTransactions.filter { tx ->
                val category = categoriesMap[tx.categoryId]
                val matchesWallet = walletFilter == null || tx.walletId == walletFilter || tx.targetWalletId == walletFilter
                val matchesType = typeFilter == null || tx.type == typeFilter
                val matchesCategory = categoryFilter == null || tx.categoryId == categoryFilter
                val matchesDate = when (dateFilter) {
                    HistoryDateFilter.ALL -> true
                    HistoryDateFilter.TODAY -> tx.dateMillis in startOfToday..(startOfTomorrow - 1)
                    HistoryDateFilter.LAST_7_DAYS -> tx.dateMillis in (startOfToday - 6 * DAY_MILLIS)..(startOfTomorrow - 1)
                    HistoryDateFilter.LAST_30_DAYS -> tx.dateMillis in (startOfToday - 29 * DAY_MILLIS)..(startOfTomorrow - 1)
                    HistoryDateFilter.THIS_MONTH -> tx.dateMillis in startOfMonth..(startOfNextMonth - 1)
                }
                // V2-P1-1: "100.000" / "Rp100.000" harus ketemu seperti
                // "100000" — bandingkan juga versi digit saja dari query.
                val queryDigits = query.filter { it.isDigit() }
                val matchesQuery = query.isBlank() ||
                        (category?.name?.contains(query, ignoreCase = true) == true) ||
                        (tx.note?.contains(query, ignoreCase = true) == true) ||
                        tx.amount.toString().contains(query) ||
                        (queryDigits.isNotEmpty() && tx.amount.toString().contains(queryDigits))

                matchesWallet && matchesType && matchesCategory && matchesDate && matchesQuery
            }

            _uiState.value.copy(
                transactions = filtered,
                totalTransactionCount = allTransactions.size,
                wallets = wallets,
                categoriesMap = categoriesMap,
                categories = activeCategories,
                isLoading = false
            )
        }.onEach { state ->
            _uiState.value = state
        }.launchIn(viewModelScope)
    }

    fun onSearchChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun onWalletFilterSelect(walletId: Long?) {
        _uiState.update { it.copy(selectedWalletFilterId = walletId) }
    }

    fun onTypeFilterSelect(type: String?) {
        _uiState.update { it.copy(selectedTypeFilter = type) }
    }

    fun onCategoryFilterSelect(categoryId: Long?) {
        _uiState.update { it.copy(selectedCategoryFilterId = categoryId) }
    }

    fun onDateFilterSelect(date: HistoryDateFilter) {
        _uiState.update { it.copy(selectedDateFilter = date) }
    }

    fun clearAllFilters() {
        _uiState.update {
            it.copy(
                selectedTypeFilter = null,
                selectedWalletFilterId = null,
                selectedCategoryFilterId = null,
                selectedDateFilter = HistoryDateFilter.ALL
            )
        }
    }

    fun onDeleteRequest(transaction: TransactionEntity) {
        _uiState.update { it.copy(transactionToDelete = transaction) }
    }

    fun cancelDelete() {
        _uiState.update { it.copy(transactionToDelete = null) }
    }

    fun confirmDelete() {
        viewModelScope.launch {
            val tx = _uiState.value.transactionToDelete
            if (tx != null) {
                transactionRepository.deleteTransaction(tx)
                _uiState.update { it.copy(transactionToDelete = null) }
            }
        }
    }
}
