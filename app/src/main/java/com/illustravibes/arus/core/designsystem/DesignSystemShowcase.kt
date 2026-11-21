package com.illustravibes.arus.core.designsystem

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.illustravibes.arus.R

@Composable
fun DesignSystemShowcaseScreen() {
    var selectedTab by remember { mutableStateOf(ArusTab.BERANDA) }
    var inputText by remember { mutableStateOf("500000") }
    var chipSelected by remember { mutableStateOf(true) }
    var selectedPeriodIndex by remember { mutableStateOf(2) } // Bulanan
    var transactionTypeIndex by remember { mutableStateOf(1) } // Pengeluaran
    var showSheet by remember { mutableStateOf(false) }
    var showSelectionSheet by remember { mutableStateOf(false) }
    var showDetailSheet by remember { mutableStateOf(false) }
    var selectedWalletName by remember { mutableStateOf("Dompet Tunai") }
    var toggleState by remember { mutableStateOf(true) }

    val sampleBarEntries = listOf(
        BarChartEntry("Sen", 250000L, 100000L),
        BarChartEntry("Sel", 0L, 180000L),
        BarChartEntry("Rab", 500000L, 220000L),
        BarChartEntry("Kam", 0L, 85000L),
        BarChartEntry("Jum", 1200000L, 450000L),
        BarChartEntry("Sab", 150000L, 300000L),
        BarChartEntry("Min", 0L, 120000L)
    )

    ArusTheme {
        Scaffold(
            topBar = {
                ArusTopAppBar(
                    title = "Arus Design System",
                    showBackButton = true,
                    onBackClick = {}
                )
            },
            bottomBar = {
                ArusBottomNavigation(
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTab = it },
                    onAddClick = {}
                )
            },
            containerColor = ArusBackground
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = paddingValues.calculateTopPadding())
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Section 1: Financial Stat Cards (Bevel Style B&W Base)
                Text(text = "Ringkasan Saldo (Bevel Style)", style = ArusTypography.titleLarge)
                ArusStatCard(
                    title = "Total Saldo",
                    amount = 8450000L,
                    subtitle = "Semua dompet · Terkini"
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ArusStatCard(
                        title = "Pemasukan",
                        amount = 3500000L,
                        isIncome = true,
                        modifier = Modifier.weight(1f)
                    )
                    ArusStatCard(
                        title = "Pengeluaran",
                        amount = 1250000L,
                        isIncome = false,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Section 2: Wallet Carousel Cards
                Text(text = "Daftar Dompet (Bevel Wallet Cards)", style = ArusTypography.titleLarge)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ArusWalletCard(
                        walletName = "Dompet Tunai",
                        balance = 1200000L,
                        tileColor = NotionMarigoldSoft,
                        iconTint = NotionMarigold,
                        iconResId = R.drawable.fa_money_bill
                    )
                    ArusWalletCard(
                        walletName = "Bank BCA",
                        balance = 5250000L,
                        tileColor = NotionSkySoft,
                        iconTint = NotionSky,
                        iconResId = R.drawable.fa_building_columns
                    )
                    ArusWalletCard(
                        walletName = "GoPay",
                        balance = 2000000L,
                        tileColor = NotionMintSoft,
                        iconTint = NotionMint,
                        iconResId = R.drawable.fa_wallet
                    )
                }

                // Section 3: Bevel Form Input Reference
                Text(text = "Form Input (Bevel Style Reference)", style = ArusTypography.titleLarge)
                ArusSegmentedControl(
                    options = listOf("Pemasukan", "Pengeluaran", "Transfer"),
                    selectedIndex = transactionTypeIndex,
                    onOptionSelected = { transactionTypeIndex = it }
                )
                ArusAmountHeroInput(
                    value = inputText,
                    onValueChange = { inputText = it },
                    label = "Nominal " + listOf("Pemasukan", "Pengeluaran", "Transfer")[transactionTypeIndex],
                    amountColor = when (transactionTypeIndex) {
                        0 -> ArusIncome
                        1 -> ArusExpense
                        else -> ArusTextPrimary
                    }
                )
                ArusPropertySelectorRow(
                    label = "Pilih Dompet",
                    selectedValue = selectedWalletName,
                    onClick = { showSelectionSheet = true },
                    iconResId = R.drawable.fa_wallet,
                    tileColor = NotionMarigoldSoft,
                    iconTint = NotionMarigold
                )
                ArusPropertySelectorRow(
                    label = "Kategori",
                    selectedValue = "Makanan & Minuman",
                    onClick = { showSelectionSheet = true },
                    iconResId = R.drawable.fa_utensils,
                    tileColor = NotionCoralSoft,
                    iconTint = NotionCoral
                )

                // Section 4: Segmented Control & Bar Chart
                Text(text = "Laporan & Grafik (Canvas Dual Bar Chart)", style = ArusTypography.titleLarge)
                ArusSegmentedControl(
                    options = listOf("Harian", "Mingguan", "Bulanan"),
                    selectedIndex = selectedPeriodIndex,
                    onOptionSelected = { selectedPeriodIndex = it }
                )
                ArusBarChart(entries = sampleBarEntries)

                // Section 5: Notion Accent Category Soft Tiles & Transaction Rows
                Text(text = "Daftar Transaksi (Notion Category Soft Tiles)", style = ArusTypography.titleLarge)
                ArusTransactionRow(
                    title = "Gaji Bulan Ini",
                    subtitle = "Pemasukan • Bank BCA • Hari ini",
                    amount = 5000000L,
                    type = TransactionType.INCOME,
                    iconResId = R.drawable.fa_money_bill,
                    tileColor = NotionMintSoft,
                    iconTint = NotionMint
                )
                ArusTransactionRow(
                    title = "Makan Siang & Kopi",
                    subtitle = "Makanan • Tunai • Kemarin",
                    amount = 65000L,
                    type = TransactionType.EXPENSE,
                    iconResId = R.drawable.fa_utensils,
                    tileColor = NotionCoralSoft,
                    iconTint = NotionCoral
                )
                ArusTransactionRow(
                    title = "Transfer ke GoPay",
                    subtitle = "Transfer • Tunai ➔ GoPay • 2 hari lalu",
                    amount = 200000L,
                    type = TransactionType.TRANSFER,
                    iconResId = R.drawable.fa_right_left,
                    tileColor = NotionSkySoft,
                    iconTint = NotionSky
                )

                // Section 6: Buttons & Chips
                Text(text = "Tombol & Filter Chip", style = ArusTypography.titleLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ArusChip(
                        text = "Semua Dompet",
                        selected = chipSelected,
                        onClick = { chipSelected = !chipSelected }
                    )
                    ArusChip(
                        text = "Dompet Tunai",
                        selected = !chipSelected,
                        onClick = { chipSelected = !chipSelected }
                    )
                }

                ArusPrimaryButton(
                    text = "Simpan Transaksi",
                    onClick = {}
                )

                ArusSecondaryButton(
                    text = "Uji Bottom Sheet Konfirmasi",
                    onClick = { showSheet = true }
                )

                if (showSheet) {
                    ArusConfirmationSheet(
                        title = "Hapus Transaksi Ini?",
                        message = "Transaksi senilai Rp65.000 akan dihapus dari riwayat dan saldo dompet akan diperbarui.",
                        confirmButtonText = "Ya, Hapus",
                        onConfirm = { showSheet = false },
                        onDismiss = { showSheet = false },
                        isDestructive = true
                    )
                }

                if (showSelectionSheet) {
                    ArusSelectionSheet(
                        title = "Pilih Dompet",
                        items = listOf(
                            SelectionItem("1", "Dompet Tunai", "Saldo: Rp1.200.000", R.drawable.fa_money_bill, NotionMarigoldSoft, NotionMarigold),
                            SelectionItem("2", "Bank BCA", "Saldo: Rp5.250.000", R.drawable.fa_building_columns, NotionSkySoft, NotionSky),
                            SelectionItem("3", "GoPay", "Saldo: Rp2.000.000", R.drawable.fa_wallet, NotionMintSoft, NotionMint)
                        ),
                        selectedItemId = if (selectedWalletName == "Bank BCA") "2" else if (selectedWalletName == "GoPay") "3" else "1",
                        onItemSelected = { selectedItem ->
                            selectedWalletName = selectedItem.title
                        },
                        onDismiss = { showSelectionSheet = false }
                    )
                }

                ArusSecondaryButton(
                    text = "Uji Detail Transaksi Sheet",
                    onClick = { showDetailSheet = true }
                )

                if (showDetailSheet) {
                    ArusTransactionDetailSheet(
                        data = TransactionDetailData(
                            title = "Makan Siang & Kopi",
                            amount = 65000L,
                            type = TransactionType.EXPENSE,
                            dateText = "Sabtu, 14 November 2026 · 12:30",
                            walletText = "Dompet Tunai",
                            noteText = "Makan siang bersama Budi di Warung Steak",
                            iconResId = R.drawable.fa_utensils,
                            tileColor = NotionCoralSoft,
                            iconTint = NotionCoral
                        ),
                        onDeleteClick = { showDetailSheet = false },
                        onEditClick = { },
                        onDismiss = { showDetailSheet = false }
                    )
                }

                // Section 7: Progres & Setting Toggle (Bevel Advanced)
                Text(text = "Progres & Pengaturan (Bevel Style)", style = ArusTypography.titleLarge)
                ArusProgressCard(
                    title = "Rasio Pengeluaran Bulan Ini",
                    currentAmount = 1250000L,
                    totalAmount = 3500000L,
                    subtitle = "Pengeluaran masih dalam batas aman (35% dari pemasukan)"
                )
                ArusToggleRow(
                    title = "Kunci Aplikasi dengan PIN / Biometrik",
                    subtitle = "Minta verifikasi keamanan saat membuka aplikasi",
                    checked = toggleState,
                    onCheckedChange = { toggleState = it },
                    iconResId = R.drawable.fa_sliders
                )

                // Section 8: Keypad Numerik In-App
                Text(text = "Keypad Cepat In-App (Bevel Style)", style = ArusTypography.titleLarge)
                ArusKeypadView(
                    onKeyPress = { digit -> inputText += digit },
                    onDeletePress = { if (inputText.isNotEmpty()) inputText = inputText.dropLast(1) }
                )

                // Section 9: Empty State
                Text(text = "Empty State", style = ArusTypography.titleLarge)
                ArusEmptyState(
                    title = "Belum ada transaksi",
                    description = "Mulai catat transaksi pertamamu untuk memantau arus keuangan dengan akurat.",
                    actionButtonText = "+ Catat Transaksi",
                    onActionClick = {}
                )

                // Extra bottom spacing so content can scroll well above the floating bottom nav
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DesignSystemShowcasePreview() {
    DesignSystemShowcaseScreen()
}
