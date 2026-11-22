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
import com.illustravibes.arus.feature.home.parseHexColor
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
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
    var showFilterSheet by remember { mutableStateOf(false) }

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

            // 1. Search + tombol Filter (drawer filter ala Notion/Bevel:
            // satu tombol di kanan search, membuka sheet Filter Transaksi).
            val hasActiveFilters = uiState.selectedTypeFilter != null ||
                uiState.selectedWalletFilterId != null ||
                uiState.selectedCategoryFilterId != null ||
                uiState.selectedDateFilter != HistoryDateFilter.ALL
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ArusSearchField(
                    value = uiState.searchQuery,
                    onValueChange = { query -> viewModel.onSearchChange(query) },
                    modifier = Modifier.weight(1f),
                    placeholder = "Cari nama kategori, catatan, nominal..."
                )
                FilterButton(
                    hasActiveFilters = hasActiveFilters,
                    onClick = { showFilterSheet = true }
                )
            }

            // 2. Chip filter aktif — tiap chip bisa di-close (ketuk = hapus
            // filter dimensi itu). Muncul hanya bila ada filter aktif.
            if (hasActiveFilters) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (uiState.selectedTypeFilter != null) {
                        ActiveFilterChip(
                            label = when (uiState.selectedTypeFilter) {
                                "INCOME" -> "Pemasukan"
                                "EXPENSE" -> "Pengeluaran"
                                else -> "Transfer"
                            },
                            onClose = { viewModel.onTypeFilterSelect(null) }
                        )
                    }
                    val activeWallet = uiState.wallets.find { it.id == uiState.selectedWalletFilterId }
                    if (activeWallet != null) {
                        ActiveFilterChip(
                            label = activeWallet.name,
                            onClose = { viewModel.onWalletFilterSelect(null) }
                        )
                    }
                    val activeCategory = uiState.categories.find { it.id == uiState.selectedCategoryFilterId }
                    if (activeCategory != null) {
                        ActiveFilterChip(
                            label = activeCategory.name,
                            onClose = { viewModel.onCategoryFilterSelect(null) }
                        )
                    }
                    if (uiState.selectedDateFilter != HistoryDateFilter.ALL) {
                        ActiveFilterChip(
                            label = dateFilterLabel(uiState.selectedDateFilter),
                            onClose = { viewModel.onDateFilterSelect(HistoryDateFilter.ALL) }
                        )
                    }
                }
            }

            // 3. Grouped Transaction History List
            if (uiState.transactions.isEmpty()) {
                ArusEmptyState(
                    title = "Tidak ada riwayat",
                    description = when {
                        uiState.searchQuery.isNotBlank() -> "Tidak ditemukan transaksi sesuai pencarian \"${uiState.searchQuery}\"."
                        hasActiveFilters -> "Tidak ada transaksi yang cocok dengan filter yang dipilih."
                        else -> "Belum ada riwayat transaksi tersimpan."
                    },
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

        // Sheet Filter Transaksi
        if (showFilterSheet) {
            FilterSheet(
                uiState = uiState,
                onTypeSelect = { viewModel.onTypeFilterSelect(it) },
                onWalletSelect = { viewModel.onWalletFilterSelect(it) },
                onCategorySelect = { viewModel.onCategoryFilterSelect(it) },
                onDateSelect = { viewModel.onDateFilterSelect(it) },
                onResetAll = { viewModel.clearAllFilters() },
                onDismiss = { showFilterSheet = false }
            )
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

// Tombol Filter di kanan search: pill 48dp, ikon funnel + label; hitam
// saat ada filter aktif (pola pemicu aktif Bevel: "3 groups").
@Composable
private fun FilterButton(
    hasActiveFilters: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .height(48.dp)
            .clip(PillShape)
            .background(if (hasActiveFilters) ArusBrandPrimary else ArusBrandSoft)
            .clickable { onClick() }
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            painter = painterResource(id = R.drawable.fa_filter),
            contentDescription = "Filter",
            tint = if (hasActiveFilters) Color.White else ArusTextPrimary,
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = "Filter",
            style = ArusTypography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
            color = if (hasActiveFilters) Color.White else ArusTextPrimary
        )
    }
}

// Chip filter aktif: pill 36dp putih ber-border + label + ×; ketuk =
// hapus filter dimensi itu (pola chip terpilih Bevel).
@Composable
private fun ActiveFilterChip(
    label: String,
    onClose: () -> Unit
) {
    Row(
        modifier = Modifier
            .height(36.dp)
            .clip(PillShape)
            .background(ArusSurface)
            .border(1.dp, ArusBorder, PillShape)
            .clickable { onClose() }
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = label,
            style = ArusTypography.labelMedium,
            color = ArusTextPrimary
        )
        Icon(
            painter = painterResource(id = R.drawable.ic_close),
            contentDescription = "Hapus filter $label",
            tint = ArusTextSecondary,
            modifier = Modifier.size(16.dp)
        )
    }
}

private fun dateFilterLabel(date: HistoryDateFilter): String = when (date) {
    HistoryDateFilter.ALL -> "Semua"
    HistoryDateFilter.TODAY -> "Hari ini"
    HistoryDateFilter.LAST_7_DAYS -> "7 hari terakhir"
    HistoryDateFilter.LAST_30_DAYS -> "30 hari terakhir"
    HistoryDateFilter.THIS_MONTH -> "Bulan ini"
}

// Sheet Filter Transaksi (pola Notion: satu sheet berisi semua dimensi;
// pola Bevel: pilihan langsung diterapkan, Reset menghapus semuanya).
// Chrome picker: full rounded 28dp + mengambang 16dp, seperti
// ArusSelectionSheet. Sheet TETAP terbuka saat memilih supaya keempat
// dimensi bisa diatur sekaligus.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterSheet(
    uiState: TransactionHistoryUiState,
    onTypeSelect: (String?) -> Unit,
    onWalletSelect: (Long?) -> Unit,
    onCategorySelect: (Long?) -> Unit,
    onDateSelect: (HistoryDateFilter) -> Unit,
    onResetAll: () -> Unit,
    onDismiss: () -> Unit
) {
    val hasActiveFilters = uiState.selectedTypeFilter != null ||
        uiState.selectedWalletFilterId != null ||
        uiState.selectedCategoryFilterId != null ||
        uiState.selectedDateFilter != HistoryDateFilter.ALL
    ModalBottomSheet(
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(28.dp),
        containerColor = ArusSurface,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .width(36.dp)
                    .height(4.dp)
                    .clip(PillShape)
                    .background(ArusBorder)
            )
        },
        modifier = Modifier.padding(bottom = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp)
                .imePadding()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Filter Transaksi",
                    style = ArusTypography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = ArusTextPrimary
                )
                if (hasActiveFilters) {
                    Text(
                        text = "Reset",
                        style = ArusTypography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = ArusExpense,
                        modifier = Modifier.clickable { onResetAll() }
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (hasActiveFilters || uiState.searchQuery.isNotBlank()) {
                    "Menampilkan ${uiState.transactions.size} dari ${uiState.totalTransactionCount} transaksi"
                } else {
                    "${uiState.totalTransactionCount} transaksi"
                },
                style = ArusTypography.bodyMedium,
                color = ArusTextSecondary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                FilterSection(label = "Jenis") {
                    FilterOptionPill(
                        title = "Semua",
                        selected = uiState.selectedTypeFilter == null,
                        onClick = { onTypeSelect(null) }
                    )
                    listOf(
                        "INCOME" to "Pemasukan",
                        "EXPENSE" to "Pengeluaran",
                        "TRANSFER" to "Transfer"
                    ).forEach { (type, label) ->
                        FilterOptionPill(
                            title = label,
                            selected = uiState.selectedTypeFilter == type,
                            onClick = { onTypeSelect(if (uiState.selectedTypeFilter == type) null else type) }
                        )
                    }
                }
                FilterSection(label = "Dompet") {
                    FilterOptionPill(
                        title = "Semua",
                        selected = uiState.selectedWalletFilterId == null,
                        onClick = { onWalletSelect(null) }
                    )
                    uiState.wallets.forEach { wallet ->
                        val isSelected = uiState.selectedWalletFilterId == wallet.id
                        FilterOptionPill(
                            title = wallet.name,
                            selected = isSelected,
                            onClick = { onWalletSelect(if (isSelected) null else wallet.id) },
                            tile = {
                                ArusWalletTile(
                                    logoResId = WalletMasterData.logoResIdForSlug(wallet.logoSlug),
                                    iconResId = mapDrawableIcon(wallet.icon),
                                    tileColor = parseHexColor(wallet.colorHex, NotionSkySoft).copy(alpha = 0.15f),
                                    iconTint = parseHexColor(wallet.colorHex, NotionSky),
                                    size = 36.dp,
                                    shape = CircleShape
                                )
                            }
                        )
                    }
                }
                FilterSection(label = "Kategori") {
                    FilterOptionPill(
                        title = "Semua",
                        selected = uiState.selectedCategoryFilterId == null,
                        onClick = { onCategorySelect(null) }
                    )
                    uiState.categories.forEach { category ->
                        val isSelected = uiState.selectedCategoryFilterId == category.id
                        FilterOptionPill(
                            title = category.name,
                            selected = isSelected,
                            onClick = { onCategorySelect(if (isSelected) null else category.id) },
                            tile = {
                                ArusCategoryTile(
                                    iconResId = mapDrawableIcon(category.icon),
                                    tileColor = parseHexColor(category.colorHex, NotionMintSoft).copy(alpha = 0.15f),
                                    iconTint = parseHexColor(category.colorHex, NotionMint),
                                    size = 36.dp,
                                    shape = CircleShape
                                )
                            }
                        )
                    }
                }
                FilterSection(label = "Tanggal") {
                    listOf(
                        HistoryDateFilter.ALL to "Semua",
                        HistoryDateFilter.TODAY to "Hari ini",
                        HistoryDateFilter.LAST_7_DAYS to "7 hari terakhir",
                        HistoryDateFilter.LAST_30_DAYS to "30 hari terakhir",
                        HistoryDateFilter.THIS_MONTH to "Bulan ini"
                    ).forEach { (date, label) ->
                        FilterOptionPill(
                            title = label,
                            selected = uiState.selectedDateFilter == date,
                            onClick = { onDateSelect(date) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterSection(
    label: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = label,
            style = ArusTypography.labelMedium,
            color = ArusTextSecondary
        )
        Column(verticalArrangement = Arrangement.spacedBy(8.dp), content = content)
    }
}

// Satu opsi filter: pill penuh 48dp (tile lingkaran 36dp bila ada +
// radio kustom 20dp) — pola yang sama dengan ArusSelectionSheet.
@Composable
private fun FilterOptionPill(
    title: String,
    selected: Boolean,
    onClick: () -> Unit,
    tile: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(PillShape)
            .background(if (selected) ArusBrandSoft else ArusSurface)
            .border(1.dp, if (selected) ArusBrandPrimary else ArusBorder, PillShape)
            .clickable { onClick() }
            .padding(start = if (tile != null) 6.dp else 16.dp, end = 12.dp, top = 6.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            if (tile != null) {
                tile()
                Spacer(modifier = Modifier.width(12.dp))
            }
            Text(
                text = title,
                style = ArusTypography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = ArusTextPrimary
            )
        }
        Box(
            modifier = Modifier
                .size(20.dp)
                .border(
                    width = 2.dp,
                    color = if (selected) ArusBrandPrimary else ArusTextSecondary,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (selected) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(ArusBrandPrimary)
                )
            }
        }
    }
}
