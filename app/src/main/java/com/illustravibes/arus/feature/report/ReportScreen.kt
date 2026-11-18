package com.illustravibes.arus.feature.report

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
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
    modifier: Modifier = Modifier
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
            ArusLoadingState()
        } else {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
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
                    modifier = Modifier.weight(1f)
                )
                ArusStatCard(
                    title = "Pengeluaran",
                    amount = uiState.totalExpense,
                    isIncome = false,
                    modifier = Modifier.weight(1f)
                )
            }

            ArusStatCard(
                title = "Selisih (Cash Flow)",
                amount = uiState.netDifference,
                isIncome = if (uiState.netDifference >= 0L) true else false,
                subtitle = if (uiState.netDifference >= 0L) "Pemasukan lebih besar dari pengeluaran" else "Pengeluaran melebihi pemasukan"
            )

            // 4. Progress Ratio Card
            if (uiState.totalIncome > 0L) {
                ArusProgressCard(
                    title = "Rasio Pengeluaran",
                    currentAmount = uiState.totalExpense,
                    totalAmount = uiState.totalIncome,
                    progressColor = ArusExpense,
                    subtitle = if (uiState.totalExpense <= uiState.totalIncome) "Pengeluaran masih dalam batas aman" else "Pengeluaran melebihi total pemasukan"
                )
            }

            // 5. Dual Bar Chart
            ArusBarChart(entries = uiState.barChartEntries)

            Spacer(modifier = Modifier.height(110.dp))
        }
        }
    }
}
