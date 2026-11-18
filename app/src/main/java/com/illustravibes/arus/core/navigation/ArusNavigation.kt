package com.illustravibes.arus.core.navigation

import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.illustravibes.arus.core.database.ArusDatabase
import com.illustravibes.arus.core.database.seed.DatabaseSeedHelper
import com.illustravibes.arus.core.designsystem.ArusBackground
import com.illustravibes.arus.core.designsystem.ArusBottomNavigation
import com.illustravibes.arus.core.designsystem.ArusTab
import com.illustravibes.arus.core.designsystem.formatRupiah
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

    // P0-4: ViewModel + scroll state layar tab di-hoist ke scope shell supaya
    // tidak reset tiap pindah tab (pencarian/filter Riwayat, periode Laporan,
    // posisi scroll). Entry tetap di-branch (form baru tiap dibuka).
    val homeViewModel = remember { HomeViewModel(walletRepository, categoryRepository, transactionRepository) }
    val historyViewModel = remember { TransactionHistoryViewModel(walletRepository, categoryRepository, transactionRepository) }
    val reportViewModel = remember { ReportViewModel(walletRepository, transactionRepository) }
    val settingsViewModel = remember { SettingsViewModel(walletRepository, categoryRepository, backupRepository) }
    val homeListState = rememberLazyListState()
    val historyListState = rememberLazyListState()
    val reportScrollState = rememberScrollState()
    val settingsScrollState = rememberScrollState()

    // P0-1: Snackbar sukses setelah simpan transaksi.
    val snackbarHostState = remember { SnackbarHostState() }

    // P0-3: hormati pengaturan kurangi-animasi perangkat (animator duration scale = 0).
    val animationsEnabled = remember {
        Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) != 0f
    }

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
        containerColor = ArusBackground,
        contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0)
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            AnimatedContent(
                targetState = currentScreen,
                transitionSpec = {
                    val duration = 200
                    val fromSub = initialState.isSubpage()
                    val toSub = targetState.isSubpage()
                    if (!animationsEnabled) {
                        fadeIn(animationSpec = snap()) togetherWith fadeOut(animationSpec = snap())
                    } else if (!fromSub && toSub) {
                        // Buka subpage: layar baru masuk dari kanan, yang lama keluar ke kiri.
                        (slideInHorizontally(animationSpec = tween(duration)) { it } + fadeIn(animationSpec = tween(duration))) togetherWith
                            (slideOutHorizontally(animationSpec = tween(duration)) { -it / 4 } + fadeOut(animationSpec = tween(duration)))
                    } else if (fromSub && !toSub) {
                        // Kembali ke tab: layar baru masuk dari kiri, yang lama keluar ke kanan.
                        (slideInHorizontally(animationSpec = tween(duration)) { -it } + fadeIn(animationSpec = tween(duration))) togetherWith
                            (slideOutHorizontally(animationSpec = tween(duration)) { it / 4 } + fadeOut(animationSpec = tween(duration)))
                    } else {
                        // Pindah tab: crossfade.
                        fadeIn(animationSpec = tween(duration)) togetherWith fadeOut(animationSpec = tween(duration))
                    }
                },
                modifier = Modifier.fillMaxSize(),
                label = "arus-screen-transition"
            ) { screen ->
                when (screen) {
                    is ArusScreen.Home -> {
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
                            },
                            listState = homeListState
                        )
                    }

                    is ArusScreen.TransactionHistory -> {
                        TransactionHistoryScreen(
                            viewModel = historyViewModel,
                            listState = historyListState
                        )
                    }

                    is ArusScreen.Report -> {
                        ReportScreen(
                            viewModel = reportViewModel,
                            scrollState = reportScrollState
                        )
                    }

                    is ArusScreen.Settings -> {
                        SettingsScreen(
                            viewModel = settingsViewModel,
                            onNavigateToWalletManagement = {
                                currentScreen = ArusScreen.WalletManagement
                            },
                            onNavigateToCategoryManagement = {
                                currentScreen = ArusScreen.CategoryManagement
                            },
                            scrollState = settingsScrollState
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
                            onSaveSuccess = { savedAmount ->
                                selectedTab = ArusTab.BERANDA
                                currentScreen = ArusScreen.Home
                                coroutineScope.launch {
                                    snackbarHostState.currentSnackbarData?.dismiss()
                                    val result = snackbarHostState.showSnackbar(
                                        message = "Transaksi ${savedAmount.formatRupiah()} tersimpan",
                                        actionLabel = "Lihat",
                                        duration = SnackbarDuration.Short
                                    )
                                    if (result == SnackbarResult.ActionPerformed) {
                                        selectedTab = ArusTab.TRANSAKSI
                                        currentScreen = ArusScreen.TransactionHistory
                                    }
                                }
                            }
                        )
                    }
                }
            }

            // Bottom bar melayang di atas konten (overlay): konten layar bisa
            // menggeser sampai ke bawah gradasi, bukan berhenti 140dp di atas.
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
                    },
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }

            // P0-1: Snackbar sukses simpan, melayang di atas pill bottom nav.
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 160.dp)
            )
        }
    }
}

/** Layar subpage (punya back, tanpa bottom bar) vs layar tab utama. */
private fun ArusScreen.isSubpage(): Boolean =
    this is ArusScreen.WalletManagement ||
        this is ArusScreen.CategoryManagement ||
        this is ArusScreen.TransactionEntry
