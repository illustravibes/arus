package com.illustravibes.arus.feature.transaction

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
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
    onEditTransaction: (TransactionEntity) -> Unit,
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
                .padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            // 1. Search Bar
            ArusSearchField(
                value = uiState.searchQuery,
                onValueChange = { query -> viewModel.onSearchChange(query) },
                placeholder = "Cari nama kategori, catatan, nominal..."
            )

            // 2. Filter Chips — B2 (audit awam): dua dimensi filter (jenis
            // dan dompet) dipisah jadi dua baris berlabel. Sebelumnya satu
            // baris tanpa label: chip dompet tersembunyi di luar layar dan
            // tidak bisa dibedakan dari chip jenis.
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterRow(label = "Jenis") {
                    ArusChip(
                        text = "Semua",
                        selected = uiState.selectedTypeFilter == null,
                        onClick = { viewModel.onTypeFilterSelect(null) }
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
                }
                FilterRow(label = "Dompet") {
                    ArusChip(
                        text = "Semua",
                        selected = uiState.selectedWalletFilterId == null,
                        onClick = { viewModel.onWalletFilterSelect(null) }
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

                        item(key = "day-$dateHeader") {
                            ArusDayCard(
                                dateText = dateHeader,
                                totalDailyText = if (dailyTotal != 0L) (if (dailyTotal > 0) "+ " else "") + dailyTotal.formatRupiah() else null
                            ) {
                                txList.forEachIndexed { index, tx ->
                                    if (index > 0) {
                                        HorizontalDivider(color = ArusBorder, thickness = 1.dp)
                                    }
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
                                    val targetWallet = uiState.wallets.find { it.id == tx.targetWalletId }
                                    val timeString = SimpleDateFormat("HH:mm", Locale.forLanguageTag("id-ID")).format(Date(tx.dateMillis))
                                    // B1 (audit awam): baris transfer menampilkan tujuan.
                                    val rowSubtitle = if (txType == TransactionType.TRANSFER) {
                                        "${wallet?.name ?: "Dompet"} ❯ ${targetWallet?.name ?: "Dompet"} • $timeString"
                                    } else {
                                        "${wallet?.name ?: "Dompet"} • ${category?.name ?: "Transaksi"} • $timeString"
                                    }
                                    ArusTransactionRow(
                                        title = tx.note.orEmpty().ifBlank { category?.name ?: if (txType == TransactionType.TRANSFER) "Transfer Antardompet" else "Transaksi" },
                                        subtitle = rowSubtitle,
                                        amount = tx.amount,
                                        type = txType,
                                        iconResId = mapDrawableIcon(category?.icon ?: if (txType == TransactionType.TRANSFER) "fa_right_left" else "fa_tags"),
                                        tileColor = tileColor,
                                        iconTint = iconTint,
                                        inCard = true,
                                        onClick = { selectedDetailTx = tx }
                                    )
                                }
                            }
                        }
                    }

                    item { Spacer(modifier = Modifier.height(110.dp)) }
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
                onEditClick = {
                    selectedDetailTx = null
                    onEditTransaction(tx)
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

// B2 (audit awam): satu baris filter = label + chip yang bisa digeser.
// Dipakai Riwayat untuk memisah filter Jenis dan Dompet.
@Composable
private fun FilterRow(
    label: String,
    content: @Composable RowScope.() -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = label,
            style = ArusTypography.labelMedium,
            color = ArusTextSecondary,
            modifier = Modifier.width(56.dp)
        )
        Row(
            modifier = Modifier
                .weight(1f)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            content = content
        )
    }
}
