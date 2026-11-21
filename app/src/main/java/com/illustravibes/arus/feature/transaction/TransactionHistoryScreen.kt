package com.illustravibes.arus.feature.transaction

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.illustravibes.arus.R
import com.illustravibes.arus.core.database.entity.TransactionEntity
import com.illustravibes.arus.core.designsystem.*
import com.illustravibes.arus.feature.home.mapDrawableIcon
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TransactionHistoryScreen(
    viewModel: TransactionHistoryViewModel,
    onBackClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedDetailTx by remember { mutableStateOf<TransactionEntity?>(null) }

    Scaffold(
        topBar = {
            ArusTopAppBar(
                title = "Riwayat Transaksi",
                showBackButton = onBackClick != null,
                onBackClick = onBackClick
            )
        },
        containerColor = ArusBackground,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { paddingValues ->
        if (uiState.isLoading) {
            ArusSkeletonLoading(showHero = false, modifier = Modifier.padding(paddingValues))
        } else {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .imePadding()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            // 1. Search Bar
            ArusTextField(
                value = uiState.searchQuery,
                onValueChange = { query -> viewModel.onSearchChange(query) },
                label = "Cari Transaksi",
                placeholder = "Cari nama kategori, catatan, nominal...",
                showClearButton = true
            )

            // 2. Type & Wallet Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ArusChip(
                    text = "Semua",
                    selected = uiState.selectedTypeFilter == null && uiState.selectedWalletFilterId == null,
                    onClick = {
                        viewModel.onTypeFilterSelect(null)
                        viewModel.onWalletFilterSelect(null)
                    }
                )
                ArusChip(
                    text = "Pemasukan",
                    selected = uiState.selectedTypeFilter == "INCOME",
                    onClick = { viewModel.onTypeFilterSelect(if (uiState.selectedTypeFilter == "INCOME") null else "INCOME") }
                )
                ArusChip(
                    text = "Pengeluaran",
                    selected = uiState.selectedTypeFilter == "EXPENSE",
                    onClick = { viewModel.onTypeFilterSelect(if (uiState.selectedTypeFilter == "EXPENSE") null else "EXPENSE") }
                )
                ArusChip(
                    text = "Transfer",
                    selected = uiState.selectedTypeFilter == "TRANSFER",
                    onClick = { viewModel.onTypeFilterSelect(if (uiState.selectedTypeFilter == "TRANSFER") null else "TRANSFER") }
                )

                uiState.wallets.forEach { wallet ->
                    val isSelected = uiState.selectedWalletFilterId == wallet.id
                    ArusChip(
                        text = wallet.name,
                        selected = isSelected,
                        onClick = { viewModel.onWalletFilterSelect(if (isSelected) null else wallet.id) }
                    )
                }
            }

            // 3. Grouped Transaction History List
            if (uiState.transactions.isEmpty()) {
                ArusEmptyState(
                    title = "Tidak ada riwayat",
                    description = if (uiState.searchQuery.isNotBlank()) "Tidak ditemukan transaksi sesuai pencarian \"${uiState.searchQuery}\"." else "Belum ada riwayat transaksi tersimpan.",
                    iconResId = if (uiState.searchQuery.isNotBlank()) R.drawable.fa_magnifying_glass else R.drawable.fa_money_bill
                )
            } else {
                val groupedTransactions = groupTransactionsByDate(uiState.transactions)

                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    groupedTransactions.forEach { (dateHeader, txList) ->
                    val dailyTotal = txList.sumOf { tx ->
                        when (tx.type) {
                            "INCOME" -> tx.amount
                            "EXPENSE" -> -tx.amount
                            else -> 0L
                        }
                    }

                        item(key = "header-$dateHeader") {
                            ArusDateHeaderRow(
                                dateText = dateHeader,
                                totalDailyText = if (dailyTotal != 0L) (if (dailyTotal > 0) "+ " else "") + dailyTotal.formatRupiah() else null
                            )
                        }

                        items(txList, key = { it.id }) { tx ->
                            val category = uiState.categoriesMap[tx.categoryId]
                            val txType = when (tx.type) {
                                "INCOME" -> TransactionType.INCOME
                                "EXPENSE" -> TransactionType.EXPENSE
                                else -> TransactionType.TRANSFER
                            }
                            val (tileColor, iconTint) = when (txType) {
                                TransactionType.INCOME -> NotionMintSoft to NotionMint
                                TransactionType.EXPENSE -> NotionCoralSoft to NotionCoral
                                TransactionType.TRANSFER -> NotionSkySoft to NotionSky
                            }

                            val wallet = uiState.wallets.find { it.id == tx.walletId }
                            ArusTransactionRow(
                                title = category?.name ?: if (txType == TransactionType.TRANSFER) "Transfer Antardompet" else "Transaksi",
                                subtitle = tx.note.orEmpty().ifBlank { wallet?.name ?: "Dompet" },
                                amount = tx.amount,
                                type = txType,
                                iconResId = mapDrawableIcon(category?.icon ?: if (txType == TransactionType.TRANSFER) "fa_right_left" else "fa_tags"),
                                tileColor = tileColor,
                                iconTint = iconTint,
                                onClick = { selectedDetailTx = tx }
                            )
                        }
                    }

                    item { Spacer(modifier = Modifier.height(102.dp)) }
                }
            }
        }
        }

        // Transaction Detail Preview Sheet
        if (selectedDetailTx != null) {
            val tx = selectedDetailTx!!
            val category = uiState.categoriesMap[tx.categoryId]
            val wallet = uiState.wallets.find { it.id == tx.walletId }
            val targetWallet = uiState.wallets.find { it.id == tx.targetWalletId }

            val txType = when (tx.type) {
                "INCOME" -> TransactionType.INCOME
                "EXPENSE" -> TransactionType.EXPENSE
                else -> TransactionType.TRANSFER
            }
            val (tileColor, iconTint) = when (txType) {
                TransactionType.INCOME -> NotionMintSoft to NotionMint
                TransactionType.EXPENSE -> NotionCoralSoft to NotionCoral
                TransactionType.TRANSFER -> NotionSkySoft to NotionSky
            }

            val dateText = SimpleDateFormat("EEEE, d MMMM yyyy · HH:mm", Locale.forLanguageTag("id-ID")).format(Date(tx.dateMillis))
            val walletText = wallet?.name ?: "Dompet"
            val targetWalletText = if (txType == TransactionType.TRANSFER) targetWallet?.name else null

            ArusTransactionDetailSheet(
                data = TransactionDetailData(
                    title = category?.name ?: if (txType == TransactionType.TRANSFER) "Transfer Antardompet" else "Transaksi",
                    amount = tx.amount,
                    type = txType,
                    dateText = dateText,
                    walletText = walletText,
                    targetWalletText = targetWalletText,
                    noteText = tx.note,
                    iconResId = mapDrawableIcon(category?.icon ?: if (txType == TransactionType.TRANSFER) "fa_right_left" else "fa_tags"),
                    tileColor = tileColor,
                    iconTint = iconTint
                ),
                onDeleteClick = {
                    selectedDetailTx = null
                    viewModel.onDeleteRequest(tx)
                },
                onDismiss = { selectedDetailTx = null }
            )
        }

        // Delete Confirmation Sheet
        if (uiState.transactionToDelete != null) {
            val txToDelete = uiState.transactionToDelete!!
            ArusConfirmationSheet(
                title = "Hapus Transaksi Ini?",
                message = "Transaksi senilai ${txToDelete.amount.formatRupiah()} akan dihapus secara permanen dan saldo dompet akan diperbarui.",
                confirmButtonText = "Ya, Hapus",
                onConfirm = { viewModel.confirmDelete() },
                onDismiss = { viewModel.cancelDelete() },
                isDestructive = true
            )
        }
    }
}

private fun groupTransactionsByDate(transactions: List<TransactionEntity>): Map<String, List<TransactionEntity>> {
    val formatter = SimpleDateFormat("EEEE, d MMMM yyyy", Locale.forLanguageTag("id-ID"))
    return transactions.groupBy { tx ->
        formatter.format(Date(tx.dateMillis))
    }
}
