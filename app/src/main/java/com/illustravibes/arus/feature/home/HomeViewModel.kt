package com.illustravibes.arus.feature.home

import android.content.Context
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

data class HomeUiState(
    val isLoading: Boolean = true,
    val totalBalance: Long = 0L,
    val incomeThisMonth: Long = 0L,
    val expenseThisMonth: Long = 0L,
    val isBalanceHidden: Boolean = false,
    val wallets: List<WalletEntity> = emptyList(),
    val walletBalances: Map<Long, Long> = emptyMap(),
    val recentTransactions: List<TransactionEntity> = emptyList(),
    val categoriesMap: Map<Long, CategoryEntity> = emptyMap(),
    val transactionToDelete: TransactionEntity? = null
)

class HomeViewModel(
    private val walletRepository: WalletRepository,
    private val categoryRepository: CategoryRepository,
    private val transactionRepository: TransactionRepository,
    context: Context
) : ViewModel() {

    // V2-P2-2: pilihan sembunyikan saldo bertahan setelah aplikasi ditutup.
    // Hanya applicationContext yang disimpan (bukan Activity) supaya aman.
    private val prefs = context.applicationContext.getSharedPreferences("arus_prefs", Context.MODE_PRIVATE)

    private val _uiState = MutableStateFlow(HomeUiState(isBalanceHidden = prefs.getBoolean("balance_hidden", false)))
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadHomeData()
    }

    private fun loadHomeData() {
        combine(
            walletRepository.walletsFlow,
            combine(transactionRepository.allTransactionsFlow, categoryRepository.allCategoriesFlow) { t, c -> Pair(t, c) }
        ) { wallets, (allTransactions, categories) ->
            val categoriesMap = categories.associateBy { it.id }
            val nowMillis = System.currentTimeMillis()
            val (startOfMonth, endOfMonth) = getMonthBounds()

            val effectiveRecentTransactions = allTransactions
                .filter { it.dateMillis <= nowMillis }
                .take(5)

            val walletBalances = wallets.associate { wallet ->
                val calculated = wallet.initialBalance +
                        allTransactions.filter { it.walletId == wallet.id && it.type == "INCOME" && it.dateMillis <= nowMillis }.sumOf { it.amount } -
                        allTransactions.filter { it.walletId == wallet.id && it.type == "EXPENSE" && it.dateMillis <= nowMillis }.sumOf { it.amount } +
                        allTransactions.filter { it.targetWalletId == wallet.id && it.type == "TRANSFER" && it.dateMillis <= nowMillis }.sumOf { it.amount } -
                        allTransactions.filter { it.walletId == wallet.id && it.type == "TRANSFER" && it.dateMillis <= nowMillis }.sumOf { it.amount }
                wallet.id to calculated
            }

            // Total + bulanan dihitung di Kotlin dari aliran yang sama (bukan query
            // SQL dengan nowMillis beku) supaya langsung benar setelah simpan/hapus
            // tanpa restart aplikasi.
            val totalBalance = walletBalances.values.sum()
            val incomeThisMonth = allTransactions
                .filter { it.type == "INCOME" && it.dateMillis in startOfMonth..endOfMonth && it.dateMillis <= nowMillis }
                .sumOf { it.amount }
            val expenseThisMonth = allTransactions
                .filter { it.type == "EXPENSE" && it.dateMillis in startOfMonth..endOfMonth && it.dateMillis <= nowMillis }
                .sumOf { it.amount }

            HomeUiState(
                isLoading = false,
                totalBalance = totalBalance,
                incomeThisMonth = incomeThisMonth,
                expenseThisMonth = expenseThisMonth,
                wallets = wallets,
                walletBalances = walletBalances,
                recentTransactions = effectiveRecentTransactions,
                categoriesMap = categoriesMap
            )
        }.onEach { state ->
            _uiState.update { currentState ->
                state.copy(
                    isBalanceHidden = currentState.isBalanceHidden,
                    transactionToDelete = currentState.transactionToDelete
                )
            }
        }.launchIn(viewModelScope)
    }

    fun toggleHideBalance() {
        val newValue = !_uiState.value.isBalanceHidden
        prefs.edit().putBoolean("balance_hidden", newValue).apply()
        _uiState.update { it.copy(isBalanceHidden = newValue) }
    }

    // V2-P1-2: hapus transaksi dari detail sheet Beranda (cermin Riwayat).
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

    private fun getMonthBounds(): Pair<Long, Long> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfMonth = calendar.timeInMillis

        calendar.add(Calendar.MONTH, 1)
        val endOfMonth = calendar.timeInMillis - 1
        return Pair(startOfMonth, endOfMonth)
    }
}
