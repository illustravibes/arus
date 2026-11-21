package com.illustravibes.arus.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.illustravibes.arus.R
import com.illustravibes.arus.core.database.entity.TransactionEntity
import com.illustravibes.arus.core.database.entity.WalletEntity
import com.illustravibes.arus.core.designsystem.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToEntry: (typeIndex: Int) -> Unit,
    onNavigateToTransactionHistory: () -> Unit,
    onNavigateToWalletManagement: () -> Unit,
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState()
) {
    val uiState by viewModel.uiState.collectAsState()

    if (uiState.isLoading) {
        ArusSkeletonLoading(modifier = modifier)
        return
    }

    HomeContent(
        uiState = uiState,
        onNavigateToEntry = onNavigateToEntry,
        onNavigateToTransactionHistory = onNavigateToTransactionHistory,
        onNavigateToWalletManagement = onNavigateToWalletManagement,
        onToggleHideBalance = { viewModel.toggleHideBalance() },
        transactionToDelete = uiState.transactionToDelete,
        onDeleteRequest = { viewModel.onDeleteRequest(it) },
        onConfirmDelete = { viewModel.confirmDelete() },
        onCancelDelete = { viewModel.cancelDelete() },
        modifier = modifier,
        listState = listState
    )
}

@Composable
fun HomeContent(
    uiState: HomeUiState,
    onNavigateToEntry: (typeIndex: Int) -> Unit,
    onNavigateToTransactionHistory: () -> Unit,
    onNavigateToWalletManagement: () -> Unit,
    onToggleHideBalance: () -> Unit = {},
    transactionToDelete: TransactionEntity? = null,
    onDeleteRequest: (TransactionEntity) -> Unit = {},
    onConfirmDelete: () -> Unit = {},
    onCancelDelete: () -> Unit = {},
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState()
) {
    // V2-P1-2: detail transaksi dibuka dari Beranda (cermin Riwayat).
    var selectedDetailTx by remember { mutableStateOf<TransactionEntity?>(null) }
    val greetingText = getGreetingText()
    val todayDateText = getTodayDateText()

    LazyColumn(
        state = listState,
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .imePadding(),
        contentPadding = PaddingValues(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Spacer(modifier = Modifier.height(8.dp)) }

        // 1. Viewing Area: Greeting Header
        item {
        Column {
            Text(
                text = greetingText,
                style = ArusTypography.displayMedium,
                color = ArusTextPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = todayDateText,
                style = ArusTypography.bodyMedium,
                color = ArusTextSecondary
            )
        }
        }

        // 2. Hero Total Balance Card (Bevel Style B&W)
        item {
        ArusStatCard(
            title = "Total Saldo",
            amount = uiState.totalBalance,
            subtitle = "Semua dompet aktif · Terkini",
            showEyeButton = true,
            isBalanceHidden = uiState.isBalanceHidden,
            onToggleHideBalance = onToggleHideBalance
        )
        }

        // 3. Monthly Income vs Expense 2-Col Grid
        item {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ArusStatCard(
                title = "Pemasukan",
                amount = uiState.incomeThisMonth,
                isIncome = true,
                subtitle = "Bulan ini",
                compactAmount = true,
                modifier = Modifier.weight(1f)
            )
            ArusStatCard(
                title = "Pengeluaran",
                amount = uiState.expenseThisMonth,
                isIncome = false,
                subtitle = "Bulan ini",
                compactAmount = true,
                modifier = Modifier.weight(1f)
            )
        }
        }

        // 4. Quick Actions Grid (3 Bevel Action Pills)
        item { ArusSectionHeader(title = "Aksi Cepat") }
        item {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            QuickActionButton(
                title = "Pemasukan",
                iconResId = R.drawable.fa_arrow_down_to_bracket,
                tileColor = NotionMintSoft,
                contentColor = NotionMint,
                onClick = { onNavigateToEntry(0) },
                modifier = Modifier.weight(1f)
            )
            QuickActionButton(
                title = "Pengeluaran",
                iconResId = R.drawable.fa_arrow_up_from_bracket,
                tileColor = NotionCoralSoft,
                contentColor = NotionCoral,
                onClick = { onNavigateToEntry(1) },
                modifier = Modifier.weight(1f)
            )
            QuickActionButton(
                title = "Transfer",
                iconResId = R.drawable.fa_right_left,
                tileColor = NotionSkySoft,
                contentColor = NotionSky,
                onClick = { onNavigateToEntry(2) },
                modifier = Modifier.weight(1f)
            )
        }
        }

        // 5. Wallet Carousel Section
        item {
            ArusSectionHeader(title = "Daftar Dompet")
        }
        item {
        if (uiState.wallets.isEmpty()) {
            ArusCard {
                Text(
                    text = "Belum ada dompet tersimpan",
                    style = ArusTypography.bodyMedium,
                    color = ArusTextSecondary
                )
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                uiState.wallets.forEach { wallet ->
                    val balance = uiState.walletBalances[wallet.id] ?: wallet.initialBalance
                    val iconTint = parseHexColor(wallet.colorHex, NotionSky)
                    val tileColor = iconTint.copy(alpha = 0.15f)

                    ArusWalletCard(
                        walletName = wallet.name,
                        balance = balance,
                        iconResId = mapDrawableIcon(wallet.icon),
                        tileColor = tileColor,
                        iconTint = iconTint,
                        onClick = onNavigateToWalletManagement,
                        logoResId = WalletMasterData.logoResIdForSlug(wallet.logoSlug)
                    )
                }
            }
        }
        }

        // 6. Recent Activity Section
        item {
            ArusSectionHeader(
                title = "Aktivitas Terbaru",
                actionText = if (uiState.recentTransactions.isNotEmpty()) "Lihat Semua" else null,
                onActionClick = onNavigateToTransactionHistory
            )
        }

        if (uiState.recentTransactions.isEmpty()) {
            item {
            ArusEmptyState(
                title = "Belum ada transaksi",
                description = "Mulai catat transaksi pertamamu untuk memantau arus keuangan dengan akurat.",
                actionButtonText = "+ Catat Transaksi",
                onActionClick = { onNavigateToEntry(1) }
            )
            }
        } else {
            items(uiState.recentTransactions, key = { it.id }) { tx ->
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

                    ArusTransactionRow(
                        title = category?.name ?: if (txType == TransactionType.TRANSFER) "Transfer Antardompet" else "Transaksi",
                        subtitle = formatDateSubtitle(tx.dateMillis, tx.note),
                        amount = tx.amount,
                        type = txType,
                        iconResId = mapDrawableIcon(category?.icon ?: if (txType == TransactionType.TRANSFER) "fa_right_left" else "fa_tags"),
                        tileColor = tileColor,
                        iconTint = iconTint,
                        onClick = { selectedDetailTx = tx }
                    )
            }
        }

        item { Spacer(modifier = Modifier.height(110.dp)) }
    }

    // V2-P1-2: detail transaksi dari Beranda (cermin Riwayat).
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
                onDeleteRequest(tx)
            },
            onDismiss = { selectedDetailTx = null }
        )
    }

    if (transactionToDelete != null) {
        val txToDelete = transactionToDelete!!
        ArusConfirmationSheet(
            title = "Hapus Transaksi Ini?",
            message = "Transaksi senilai ${txToDelete.amount.formatRupiah()} akan dihapus secara permanen dan saldo dompet akan diperbarui.",
            confirmButtonText = "Ya, Hapus",
            onConfirm = { onConfirmDelete() },
            onDismiss = { onCancelDelete() },
            isDestructive = true
        )
    }
}

@Composable
private fun QuickActionButton(
    title: String,
    iconResId: Int,
    tileColor: Color,
    contentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(48.dp)
            .clip(PillShape)
            .background(tileColor)
            .border(1.dp, contentColor.copy(alpha = 0.3f), PillShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = painterResource(id = iconResId),
                contentDescription = title,
                tint = contentColor,
                modifier = Modifier.size(15.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = title,
                style = ArusTypography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = contentColor
                )
            )
        }
    }
}

private fun getGreetingText(): String {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when (hour) {
        in 4..11 -> "Selamat Pagi"
        in 12..15 -> "Selamat Siang"
        in 16..18 -> "Selamat Sore"
        else -> "Selamat Malam"
    }
}

private fun getTodayDateText(): String {
    val formatter = SimpleDateFormat("EEEE, d MMMM yyyy", Locale.forLanguageTag("id-ID"))
    return formatter.format(Date())
}

fun mapDrawableIcon(iconName: String): Int {
    return when (iconName) {
        "fa_house" -> R.drawable.fa_house
        "fa_receipt" -> R.drawable.fa_receipt
        "fa_chart_column" -> R.drawable.fa_chart_column
        "fa_sliders" -> R.drawable.fa_sliders
        "fa_utensils" -> R.drawable.fa_utensils
        "fa_basket_shopping" -> R.drawable.fa_basket_shopping
        "fa_car" -> R.drawable.fa_car
        "fa_money_bill" -> R.drawable.fa_money_bill
        "fa_building_columns" -> R.drawable.fa_building_columns
        "fa_wallet" -> R.drawable.fa_wallet
        "fa_gift" -> R.drawable.fa_gift
        "fa_heart_pulse" -> R.drawable.fa_heart_pulse
        "fa_gamepad" -> R.drawable.fa_gamepad
        "fa_right_left" -> R.drawable.fa_right_left
        "fa_credit_card" -> R.drawable.fa_credit_card
        "fa_store" -> R.drawable.fa_store
        "fa_briefcase" -> R.drawable.fa_briefcase
        "fa_graduation_cap" -> R.drawable.fa_graduation_cap
        "fa_bolt" -> R.drawable.fa_bolt
        "fa_money_bill_trend_up" -> R.drawable.fa_money_bill_trend_up
        "fa_arrow_down_to_bracket" -> R.drawable.fa_arrow_down_to_bracket
        "fa_arrow_up_from_bracket" -> R.drawable.fa_arrow_up_from_bracket
        "fa_chevron_down" -> R.drawable.fa_chevron_down
        "fa_eye" -> R.drawable.fa_eye
        "fa_eye_slash" -> R.drawable.fa_eye_slash
        "fa_magnifying_glass" -> R.drawable.fa_magnifying_glass
        else -> R.drawable.fa_tags
    }
}

fun parseHexColor(hexString: String, defaultColor: Color): Color {
    return runCatching {
        val colorInt = android.graphics.Color.parseColor(hexString)
        Color(colorInt)
    }.getOrDefault(defaultColor)
}

private fun formatDateSubtitle(dateMillis: Long, note: String?): String {
    val formatter = SimpleDateFormat("d MMM yyyy", Locale.forLanguageTag("id-ID"))
    val dateString = formatter.format(Date(dateMillis))
    return if (!note.isNullOrBlank()) "$dateString • $note" else dateString
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun HomeScreenPreview() {
    ArusTheme {
        HomeContent(
            uiState = HomeUiState(
                isLoading = false,
                totalBalance = 8450000L,
                incomeThisMonth = 3500000L,
                expenseThisMonth = 1250000L,
                wallets = listOf(
                    WalletEntity(id = 1L, name = "Dompet Tunai", icon = "fa_money_bill", colorHex = "#FFB110", initialBalance = 1200000L),
                    WalletEntity(id = 2L, name = "Bank BCA", icon = "fa_building_columns", colorHex = "#62AEF0", initialBalance = 5250000L)
                ),
                walletBalances = mapOf(1L to 1200000L, 2L to 5250000L)
            ),
            onNavigateToEntry = {},
            onNavigateToTransactionHistory = {},
            onNavigateToWalletManagement = {}
        )
    }
}
