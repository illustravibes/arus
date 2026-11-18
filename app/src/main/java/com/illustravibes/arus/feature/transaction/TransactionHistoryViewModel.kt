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

data class TransactionHistoryUiState(
    val isLoading: Boolean = true,
    val searchQuery: String = "",
    val selectedWalletFilterId: Long? = null,
    val selectedTypeFilter: String? = null, // "INCOME", "EXPENSE", "TRANSFER", or null
    val transactions: List<TransactionEntity> = emptyList(),
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
            categoryRepository.activeCategoriesFlow,
            _uiState.map { Triple(it.searchQuery, it.selectedWalletFilterId, it.selectedTypeFilter) }.distinctUntilChanged()
        ) { allTransactions, wallets, categories, (query, walletFilter, typeFilter) ->
            val categoriesMap = categories.associateBy { it.id }

            val filtered = allTransactions.filter { tx ->
                val category = categoriesMap[tx.categoryId]
                val matchesWallet = walletFilter == null || tx.walletId == walletFilter || tx.targetWalletId == walletFilter
                val matchesType = typeFilter == null || tx.type == typeFilter
                val matchesQuery = query.isBlank() ||
                        (category?.name?.contains(query, ignoreCase = true) == true) ||
                        (tx.note?.contains(query, ignoreCase = true) == true) ||
                        tx.amount.toString().contains(query)

                matchesWallet && matchesType && matchesQuery
            }

            _uiState.value.copy(
                transactions = filtered,
                wallets = wallets,
                categoriesMap = categoriesMap,
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
