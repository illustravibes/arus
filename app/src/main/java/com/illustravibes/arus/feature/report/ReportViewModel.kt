package com.illustravibes.arus.feature.report

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.illustravibes.arus.core.database.entity.WalletEntity
import com.illustravibes.arus.core.designsystem.BarChartEntry
import com.illustravibes.arus.data.repository.TransactionRepository
import com.illustravibes.arus.data.repository.WalletRepository
import kotlinx.coroutines.flow.*
import java.text.SimpleDateFormat
import java.util.*

data class ReportUiState(
    val selectedPeriodIndex: Int = 2, // 0 = Harian, 1 = Mingguan, 2 = Bulanan
    val selectedWalletId: Long? = null,
    val totalIncome: Long = 0L,
    val totalExpense: Long = 0L,
    val netDifference: Long = 0L,
    val barChartEntries: List<BarChartEntry> = emptyList(),
    val wallets: List<WalletEntity> = emptyList()
)

class ReportViewModel(
    private val walletRepository: WalletRepository,
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReportUiState())
    val uiState: StateFlow<ReportUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        combine(
            walletRepository.walletsFlow,
            transactionRepository.allTransactionsFlow,
            _uiState.map { Pair(it.selectedPeriodIndex, it.selectedWalletId) }.distinctUntilChanged()
        ) { wallets, allTransactions, (periodIndex, walletId) ->
            val nowMillis = System.currentTimeMillis()

            // Filter transactions according to selected wallet and excluding future-dated
            val filteredTxs = allTransactions.filter { tx ->
                val matchesWallet = walletId == null || tx.walletId == walletId
                val isEffective = tx.dateMillis <= nowMillis
                matchesWallet && isEffective
            }

            val (entries, startPeriod, endPeriod) = generateChartEntriesAndBounds(periodIndex, filteredTxs)

            val totalIncome = filteredTxs
                .filter { it.type == "INCOME" && it.dateMillis >= startPeriod && it.dateMillis <= endPeriod }
                .sumOf { it.amount }

            val totalExpense = filteredTxs
                .filter { it.type == "EXPENSE" && it.dateMillis >= startPeriod && it.dateMillis <= endPeriod }
                .sumOf { it.amount }

            ReportUiState(
                selectedPeriodIndex = periodIndex,
                selectedWalletId = walletId,
                totalIncome = totalIncome,
                totalExpense = totalExpense,
                netDifference = totalIncome - totalExpense,
                barChartEntries = entries,
                wallets = wallets
            )
        }.onEach { state ->
            _uiState.value = state
        }.launchIn(viewModelScope)
    }

    fun onPeriodChange(index: Int) {
        _uiState.update { it.copy(selectedPeriodIndex = index) }
    }

    fun onWalletFilterSelect(walletId: Long?) {
        _uiState.update { it.copy(selectedWalletId = walletId) }
    }

    private fun generateChartEntriesAndBounds(
        periodIndex: Int,
        transactions: List<com.illustravibes.arus.core.database.entity.TransactionEntity>
    ): Triple<List<BarChartEntry>, Long, Long> {
        val calendar = Calendar.getInstance()
        val entries = mutableListOf<BarChartEntry>()
        val dayFormatter = SimpleDateFormat("EEE", Locale.forLanguageTag("id-ID"))
        val monthFormatter = SimpleDateFormat("MMM", Locale.forLanguageTag("id-ID"))

        when (periodIndex) {
            0 -> { // Harian (7 hari terakhir)
                val endPeriod = System.currentTimeMillis()
                calendar.add(Calendar.DAY_OF_YEAR, -6)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                val startPeriod = calendar.timeInMillis

                for (i in 0..6) {
                    val dayStart = calendar.timeInMillis
                    val label = dayFormatter.format(calendar.time)
                    calendar.add(Calendar.DAY_OF_YEAR, 1)
                    val dayEnd = calendar.timeInMillis - 1

                    val income = transactions.filter { it.type == "INCOME" && it.dateMillis in dayStart..dayEnd }.sumOf { it.amount }
                    val expense = transactions.filter { it.type == "EXPENSE" && it.dateMillis in dayStart..dayEnd }.sumOf { it.amount }
                    entries.add(BarChartEntry(label, income, expense))
                }
                return Triple(entries, startPeriod, endPeriod)
            }
            1 -> { // Mingguan (4 minggu terakhir)
                val endPeriod = System.currentTimeMillis()
                calendar.add(Calendar.WEEK_OF_YEAR, -3)
                calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                val startPeriod = calendar.timeInMillis

                for (i in 1..4) {
                    val weekStart = calendar.timeInMillis
                    val label = "M$i"
                    calendar.add(Calendar.WEEK_OF_YEAR, 1)
                    val weekEnd = calendar.timeInMillis - 1

                    val income = transactions.filter { it.type == "INCOME" && it.dateMillis in weekStart..weekEnd }.sumOf { it.amount }
                    val expense = transactions.filter { it.type == "EXPENSE" && it.dateMillis in weekStart..weekEnd }.sumOf { it.amount }
                    entries.add(BarChartEntry(label, income, expense))
                }
                return Triple(entries, startPeriod, endPeriod)
            }
            else -> { // Bulanan (6 bulan terakhir)
                val endPeriod = System.currentTimeMillis()
                calendar.add(Calendar.MONTH, -5)
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                val startPeriod = calendar.timeInMillis

                for (i in 0..5) {
                    val monthStart = calendar.timeInMillis
                    val label = monthFormatter.format(calendar.time)
                    calendar.add(Calendar.MONTH, 1)
                    val monthEnd = calendar.timeInMillis - 1

                    val income = transactions.filter { it.type == "INCOME" && it.dateMillis in monthStart..monthEnd }.sumOf { it.amount }
                    val expense = transactions.filter { it.type == "EXPENSE" && it.dateMillis in monthStart..monthEnd }.sumOf { it.amount }
                    entries.add(BarChartEntry(label, income, expense))
                }
                return Triple(entries, startPeriod, endPeriod)
            }
        }
    }
}
