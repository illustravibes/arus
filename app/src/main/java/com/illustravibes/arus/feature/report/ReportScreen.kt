package com.illustravibes.arus.feature.report

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.illustravibes.arus.core.designsystem.*

@Composable
fun ReportScreen(
    viewModel: ReportViewModel,
    onBackClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    scrollState: ScrollState = rememberScrollState()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            ArusTopAppBar(
                title = "Laporan Keuangan",
                showBackButton = onBackClick != null,
                onBackClick = onBackClick
            )
        },
        containerColor = ArusBackground,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { paddingValues ->
        if (uiState.isLoading) {
            // V2-P2-3: skeleton, bukan spinner fullscreen.
            ArusSkeletonLoading(showHero = false, modifier = Modifier.padding(paddingValues))
        } else {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 12.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            // 1. Period Switcher (Harian, Mingguan, Bulanan)
            ArusSegmentedControl(
                options = listOf("Harian", "Mingguan", "Bulanan"),
                selectedIndex = uiState.selectedPeriodIndex,
                onOptionSelected = { index -> viewModel.onPeriodChange(index) }
            )

            // 2. Wallet Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ArusChip(
                    text = "Semua Dompet",
                    selected = uiState.selectedWalletId == null,
                    onClick = { viewModel.onWalletFilterSelect(null) }
                )
                uiState.wallets.forEach { wallet ->
                    val isSelected = uiState.selectedWalletId == wallet.id
                    ArusChip(
                        text = wallet.name,
                        selected = isSelected,
                        onClick = { viewModel.onWalletFilterSelect(if (isSelected) null else wallet.id) }
                    )
                }
            }

            // 3. Financial Summary Cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ArusStatCard(
                    title = "Pemasukan",
                    amount = uiState.totalIncome,
                    isIncome = true,
                    compactAmount = true,
                    modifier = Modifier.weight(1f)
                )
                ArusStatCard(
                    title = "Pengeluaran",
                    amount = uiState.totalExpense,
                    isIncome = false,
                    compactAmount = true,
                    modifier = Modifier.weight(1f)
                )
            }

            ArusStatCard(
                title = "Selisih",
                amount = uiState.netDifference,
                isIncome = if (uiState.netDifference >= 0L) true else false,
                // V2-P2-1: saat keduanya nol, tidak ada yang "lebih besar".
                subtitle = when {
                    uiState.totalIncome == 0L && uiState.totalExpense == 0L -> "Belum ada pemasukan maupun pengeluaran pada periode ini"
                    uiState.netDifference > 0L -> "Pemasukan lebih besar dari pengeluaran"
                    uiState.netDifference < 0L -> "Pengeluaran melebihi pemasukan"
                    else -> "Pemasukan dan pengeluaran sama besar"
                }
            )

            // 4. Progress Ratio Card
            if (uiState.totalIncome > 0L) {
                ArusProgressCard(
                    title = "Rasio Pengeluaran",
                    currentAmount = uiState.totalExpense,
                    totalAmount = uiState.totalIncome,
                    progressColor = ArusExpense,
                    subtitle = if (uiState.totalExpense <= uiState.totalIncome) "Pengeluaran masih dalam batas aman (tidak melebihi pemasukan)" else "Pengeluaran melebihi total pemasukan"
                )
            } else {
                // A4 (audit awam): kartu Rasio tidak boleh hilang saat
                // pemasukan Rp0 — tampilkan dengan penjelasan kenapa belum
                // bisa dihitung.
                ArusCard {
                    Text(
                        text = "Rasio Pengeluaran",
                        style = ArusTypography.labelMedium,
                        color = ArusTextSecondary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Belum ada pemasukan bulan ini, jadi rasio belum bisa dihitung.",
                        style = ArusTypography.bodyMedium,
                        color = ArusTextPrimary
                    )
                }
            }

            // 5. Dual Bar Chart
            ArusBarChart(entries = uiState.barChartEntries)

            Spacer(modifier = Modifier.height(110.dp))
        }
        }
    }
}
