package com.illustravibes.arus.core.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.illustravibes.arus.core.database.ArusDatabase
import com.illustravibes.arus.core.database.seed.DatabaseSeedHelper
import com.illustravibes.arus.core.designsystem.ArusBackground
import com.illustravibes.arus.core.designsystem.ArusBottomNavigation
import com.illustravibes.arus.core.designsystem.ArusTab
import com.illustravibes.arus.data.repository.BackupRepository
import com.illustravibes.arus.data.repository.CategoryRepository
import com.illustravibes.arus.data.repository.TransactionRepository
import com.illustravibes.arus.data.repository.WalletRepository
import com.illustravibes.arus.feature.category.CategoryScreen
import com.illustravibes.arus.feature.category.CategoryViewModel
import com.illustravibes.arus.feature.home.HomeScreen
import com.illustravibes.arus.feature.home.HomeViewModel
import com.illustravibes.arus.feature.report.ReportScreen
import com.illustravibes.arus.feature.report.ReportViewModel
import com.illustravibes.arus.feature.settings.SettingsScreen
import com.illustravibes.arus.feature.settings.SettingsViewModel
import com.illustravibes.arus.feature.transaction.TransactionEntryScreen
import com.illustravibes.arus.feature.transaction.TransactionEntryViewModel
import com.illustravibes.arus.feature.transaction.TransactionHistoryScreen
import com.illustravibes.arus.feature.transaction.TransactionHistoryViewModel
import com.illustravibes.arus.feature.wallet.WalletScreen
import com.illustravibes.arus.feature.wallet.WalletViewModel
import kotlinx.coroutines.launch

sealed class ArusScreen {
    data object Home : ArusScreen()
    data object TransactionHistory : ArusScreen()
    data object Report : ArusScreen()
    data object Settings : ArusScreen()
    data object WalletManagement : ArusScreen()
    data object CategoryManagement : ArusScreen()
    data class TransactionEntry(val initialTypeIndex: Int = 1) : ArusScreen()
}

@Composable
fun ArusAppShell() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val database = remember { ArusDatabase.getInstance(context) }
    val walletRepository = remember { WalletRepository(database) }
    val categoryRepository = remember { CategoryRepository(database) }
    val transactionRepository = remember { TransactionRepository(database) }
    val backupRepository = remember { BackupRepository(context, database) }

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            DatabaseSeedHelper.seedDefaultDataIfEmpty(database)
        }
    }

    var currentScreen by remember { mutableStateOf<ArusScreen>(ArusScreen.Home) }
    var selectedTab by remember { mutableStateOf(ArusTab.BERANDA) }

    val isNotHome = currentScreen != ArusScreen.Home
    BackHandler(enabled = isNotHome) {
        when (currentScreen) {
            is ArusScreen.WalletManagement, is ArusScreen.CategoryManagement -> {
                currentScreen = ArusScreen.Settings
            }
            is ArusScreen.TransactionEntry -> {
                currentScreen = ArusScreen.Home
            }
            else -> {
                selectedTab = ArusTab.BERANDA
                currentScreen = ArusScreen.Home
            }
        }
    }

    val showBottomBar = currentScreen is ArusScreen.Home ||
            currentScreen is ArusScreen.TransactionHistory ||
            currentScreen is ArusScreen.Report ||
            currentScreen is ArusScreen.Settings

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                ArusBottomNavigation(
                    selectedTab = selectedTab,
                    onTabSelected = { tab ->
                        selectedTab = tab
                        currentScreen = when (tab) {
                            ArusTab.BERANDA -> ArusScreen.Home
                            ArusTab.TRANSAKSI -> ArusScreen.TransactionHistory
                            ArusTab.LAPORAN -> ArusScreen.Report
                            ArusTab.PENGATURAN -> ArusScreen.Settings
                        }
                    },
                    onAddClick = {
                        currentScreen = ArusScreen.TransactionEntry(1)
                    }
                )
            }
        },
        containerColor = ArusBackground,
        contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0)
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = paddingValues.calculateBottomPadding())
        ) {
            when (val screen = currentScreen) {
                is ArusScreen.Home -> {
                    val homeViewModel = remember { HomeViewModel(walletRepository, categoryRepository, transactionRepository) }
                    HomeScreen(
                        viewModel = homeViewModel,
                        onNavigateToEntry = { typeIndex ->
                            currentScreen = ArusScreen.TransactionEntry(typeIndex)
                        },
                        onNavigateToTransactionHistory = {
                            selectedTab = ArusTab.TRANSAKSI
                            currentScreen = ArusScreen.TransactionHistory
                        },
                        onNavigateToWalletManagement = {
                            currentScreen = ArusScreen.WalletManagement
                        }
                    )
                }

                is ArusScreen.TransactionHistory -> {
                    val historyViewModel = remember { TransactionHistoryViewModel(walletRepository, categoryRepository, transactionRepository) }
                    TransactionHistoryScreen(
                        viewModel = historyViewModel
                    )
                }

                is ArusScreen.Report -> {
                    val reportViewModel = remember { ReportViewModel(walletRepository, transactionRepository) }
                    ReportScreen(
                        viewModel = reportViewModel
                    )
                }

                is ArusScreen.Settings -> {
                    val settingsViewModel = remember { SettingsViewModel(walletRepository, categoryRepository, backupRepository) }
                    SettingsScreen(
                        viewModel = settingsViewModel,
                        onNavigateToWalletManagement = {
                            currentScreen = ArusScreen.WalletManagement
                        },
                        onNavigateToCategoryManagement = {
                            currentScreen = ArusScreen.CategoryManagement
                        }
                    )
                }

                is ArusScreen.WalletManagement -> {
                    val walletViewModel = remember { WalletViewModel(walletRepository, transactionRepository) }
                    WalletScreen(
                        viewModel = walletViewModel,
                        onBackClick = {
                            currentScreen = ArusScreen.Settings
                        }
                    )
                }

                is ArusScreen.CategoryManagement -> {
                    val categoryViewModel = remember { CategoryViewModel(categoryRepository) }
                    CategoryScreen(
                        viewModel = categoryViewModel,
                        onBackClick = {
                            currentScreen = ArusScreen.Settings
                        }
                    )
                }

                is ArusScreen.TransactionEntry -> {
                    val entryViewModel = remember(screen.initialTypeIndex) {
                        TransactionEntryViewModel(walletRepository, categoryRepository, transactionRepository, screen.initialTypeIndex)
                    }
                    TransactionEntryScreen(
                        viewModel = entryViewModel,
                        onBackClick = {
                            currentScreen = ArusScreen.Home
                        },
                        onSaveSuccess = {
                            selectedTab = ArusTab.BERANDA
                            currentScreen = ArusScreen.Home
                        }
                    )
                }
            }
        }
    }
}
