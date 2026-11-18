package com.illustravibes.arus.feature.transaction

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.illustravibes.arus.R
import com.illustravibes.arus.core.designsystem.*
import com.illustravibes.arus.feature.home.mapDrawableIcon
import com.illustravibes.arus.feature.home.parseHexColor
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TransactionEntryScreen(
    viewModel: TransactionEntryViewModel,
    onBackClick: () -> Unit,
    onSaveSuccess: (savedAmount: Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    var showWalletSheet by remember { mutableStateOf(false) }
    var showTargetWalletSheet by remember { mutableStateOf(false) }
    var showCategorySheet by remember { mutableStateOf(false) }

    // P0-2: keypad in-app tampil saat field nominal fokus.
    var keypadVisible by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onSaveSuccess(uiState.amountInput.toLongOrNull() ?: 0L)
        }
    }

    Scaffold(
        topBar = {
            ArusTopAppBar(
                title = when (uiState.typeIndex) {
                    0 -> "Catat Pemasukan"
                    1 -> "Catat Pengeluaran"
                    else -> "Catat Transfer"
                },
                showBackButton = true,
                onBackClick = onBackClick
            )
        },
        containerColor = ArusBackground,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .imePadding()
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            // 1. Type Switcher (Pemasukan, Pengeluaran, Transfer)
            ArusSegmentedControl(
                options = listOf("Pemasukan", "Pengeluaran", "Transfer"),
                selectedIndex = uiState.typeIndex,
                onOptionSelected = { index -> viewModel.onTypeChange(index) }
            )

            // 2. Hero Amount Input (36sp bold with IDR formatting)
            ArusAmountHeroInput(
                value = uiState.amountInput,
                onValueChange = { input -> viewModel.onAmountChange(input) },
                label = "NOMINAL " + when (uiState.typeIndex) {
                    0 -> "PEMASUKAN"
                    1 -> "PENGELUARAN"
                    else -> "TRANSFER"
                },
                errorMessage = uiState.errorMessage,
                amountColor = when (uiState.typeIndex) {
                    0 -> ArusIncome
                    1 -> ArusExpense
                    else -> ArusTextPrimary
                },
                readOnly = true,
                onFocusChange = { keypadVisible = it }
            )

            // Keypad in-app (P0-2): muncul saat field nominal fokus; keyboard
            // sistem tetap dipakai untuk field Catatan.
            AnimatedVisibility(
                visible = keypadVisible,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                ArusKeypadView(
                    onKeyPress = { key ->
                        if (uiState.amountInput.length + key.length <= 18) {
                            viewModel.onAmountChange(uiState.amountInput + key)
                        }
                    },
                    onDeletePress = {
                        viewModel.onAmountChange(uiState.amountInput.dropLast(1))
                    }
                )
            }

            // 3. Property Selector Rows
            if (uiState.typeIndex == 2) {
                val walletTint = parseHexColor(uiState.selectedWallet?.colorHex ?: "#62AEF0", NotionSky)
                val targetWalletTint = parseHexColor(uiState.selectedTargetWallet?.colorHex ?: "#00A86B", NotionMint)

                // Transfer Mode: Source Wallet & Target Wallet
                ArusPropertySelectorRow(
                    label = "Dompet Asal",
                    selectedValue = uiState.selectedWallet?.name ?: "Pilih Dompet",
                    onClick = { showWalletSheet = true },
                    iconResId = mapDrawableIcon(uiState.selectedWallet?.icon ?: "fa_wallet"),
                    tileColor = walletTint.copy(alpha = 0.15f),
                    iconTint = walletTint
                )

                ArusPropertySelectorRow(
                    label = "Dompet Tujuan",
                    selectedValue = uiState.selectedTargetWallet?.name ?: "Pilih Dompet Tujuan",
                    onClick = { showTargetWalletSheet = true },
                    iconResId = mapDrawableIcon(uiState.selectedTargetWallet?.icon ?: "fa_wallet"),
                    tileColor = targetWalletTint.copy(alpha = 0.15f),
                    iconTint = targetWalletTint
                )
            } else {
                val walletTint = parseHexColor(uiState.selectedWallet?.colorHex ?: "#62AEF0", NotionSky)
                val categoryTint = parseHexColor(uiState.selectedCategory?.colorHex ?: "#00A86B", NotionMint)

                // Income/Expense Mode: Wallet & Category
                ArusPropertySelectorRow(
                    label = "Pilih Dompet",
                    selectedValue = uiState.selectedWallet?.name ?: "Pilih Dompet",
                    onClick = { showWalletSheet = true },
                    iconResId = mapDrawableIcon(uiState.selectedWallet?.icon ?: "fa_wallet"),
                    tileColor = walletTint.copy(alpha = 0.15f),
                    iconTint = walletTint
                )

                ArusPropertySelectorRow(
                    label = "Kategori",
                    selectedValue = uiState.selectedCategory?.name ?: "Pilih Kategori",
                    onClick = { showCategorySheet = true },
                    iconResId = mapDrawableIcon(uiState.selectedCategory?.icon ?: "fa_tags"),
                    tileColor = categoryTint.copy(alpha = 0.15f),
                    iconTint = categoryTint
                )
            }

            // 4. Note Field
            ArusTextField(
                value = uiState.note,
                onValueChange = { note -> viewModel.onNoteChange(note) },
                label = "Catatan (Opsional)",
                placeholder = "mis. Makan siang bersama Budi"
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 5. Submit Button
            ArusPrimaryButton(
                text = "Simpan Transaksi",
                onClick = { viewModel.saveTransaction() }
            )

            Spacer(modifier = Modifier.height(48.dp))
        }

        // Selection Bottom Sheets
        if (showWalletSheet) {
            ArusSelectionSheet(
                title = "Pilih Dompet",
                items = uiState.wallets.map { wallet ->
                    SelectionItem(
                        id = wallet.id.toString(),
                        title = wallet.name,
                        iconResId = mapDrawableIcon(wallet.icon),
                        tileColor = parseHexColor(wallet.colorHex, NotionSkySoft).copy(alpha = 0.15f),
                        iconTint = parseHexColor(wallet.colorHex, NotionSky)
                    )
                },
                selectedItemId = uiState.selectedWallet?.id?.toString(),
                onItemSelected = { item ->
                    val wallet = uiState.wallets.find { it.id.toString() == item.id }
                    if (wallet != null) viewModel.onWalletSelect(wallet)
                },
                onDismiss = { showWalletSheet = false }
            )
        }

        if (showTargetWalletSheet) {
            ArusSelectionSheet(
                title = "Pilih Dompet Tujuan",
                items = uiState.wallets.map { wallet ->
                    SelectionItem(
                        id = wallet.id.toString(),
                        title = wallet.name,
                        iconResId = mapDrawableIcon(wallet.icon),
                        tileColor = parseHexColor(wallet.colorHex, NotionMintSoft).copy(alpha = 0.15f),
                        iconTint = parseHexColor(wallet.colorHex, NotionMint)
                    )
                },
                selectedItemId = uiState.selectedTargetWallet?.id?.toString(),
                onItemSelected = { item ->
                    val wallet = uiState.wallets.find { it.id.toString() == item.id }
                    if (wallet != null) viewModel.onTargetWalletSelect(wallet)
                },
                onDismiss = { showTargetWalletSheet = false }
            )
        }

        if (showCategorySheet) {
            ArusSelectionSheet(
                title = "Pilih Kategori",
                items = uiState.categories.map { category ->
                    SelectionItem(
                        id = category.id.toString(),
                        title = category.name,
                        iconResId = mapDrawableIcon(category.icon),
                        tileColor = parseHexColor(category.colorHex, NotionMintSoft).copy(alpha = 0.15f),
                        iconTint = parseHexColor(category.colorHex, NotionMint)
                    )
                },
                selectedItemId = uiState.selectedCategory?.id?.toString(),
                onItemSelected = { item ->
                    val category = uiState.categories.find { it.id.toString() == item.id }
                    if (category != null) viewModel.onCategorySelect(category)
                },
                onDismiss = { showCategorySheet = false }
            )
        }
    }
}
