package com.illustravibes.arus.feature.transaction

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.illustravibes.arus.R
import com.illustravibes.arus.core.designsystem.*
import com.illustravibes.arus.feature.home.mapDrawableIcon
import com.illustravibes.arus.feature.home.parseHexColor
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
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
    var showDatePicker by remember { mutableStateOf(false) }

    // Nominal pakai keyboard sistem (numerik). Keypad in-app
    // (ArusKeypadView) disimpan untuk input yang memang fokus nominal,
    // mis. PIN.

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onSaveSuccess(uiState.amountInput.toLongOrNull() ?: 0L)
        }
    }

    Scaffold(
        topBar = {
            ArusTopAppBar(
                title = (if (uiState.editTransactionId != null) "Ubah " else "Catat ") + when (uiState.typeIndex) {
                    0 -> "Pemasukan"
                    1 -> "Pengeluaran"
                    else -> "Transfer"
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
                .padding(horizontal = 12.dp)
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
            )

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
                    iconTint = walletTint,
                    logoResId = WalletMasterData.logoResIdForSlug(uiState.selectedWallet?.logoSlug)
                )

                ArusPropertySelectorRow(
                    label = "Dompet Tujuan",
                    selectedValue = uiState.selectedTargetWallet?.name ?: "Pilih Dompet Tujuan",
                    onClick = { showTargetWalletSheet = true },
                    iconResId = mapDrawableIcon(uiState.selectedTargetWallet?.icon ?: "fa_wallet"),
                    tileColor = targetWalletTint.copy(alpha = 0.15f),
                    iconTint = targetWalletTint,
                    logoResId = WalletMasterData.logoResIdForSlug(uiState.selectedTargetWallet?.logoSlug)
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
                    iconTint = walletTint,
                    logoResId = WalletMasterData.logoResIdForSlug(uiState.selectedWallet?.logoSlug)
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

            // 3b. Tanggal transaksi (bisa diubah; maks. hari ini).
            ArusPropertySelectorRow(
                label = "Tanggal",
                selectedValue = SimpleDateFormat("EEEE, d MMMM yyyy", Locale("id", "ID")).format(Date(uiState.dateMillis)),
                onClick = { showDatePicker = true },
                iconResId = R.drawable.fa_calendar,
                tileColor = NotionSkySoft.copy(alpha = 0.15f),
                iconTint = NotionSky
            )

            // 4. Note Field
            ArusTextField(
                value = uiState.note,
                onValueChange = { note -> viewModel.onNoteChange(note) },
                label = "Catatan (Opsional)",
                placeholder = "mis. Makan siang bersama Budi"
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 5. Submit Button — P1-6: nonaktif sampai input valid.
            val isFormValid = (uiState.amountInput.toLongOrNull() ?: 0L) > 0 &&
                uiState.selectedWallet != null &&
                if (uiState.typeIndex == 2) uiState.selectedTargetWallet != null
                else uiState.selectedCategory != null
            ArusPrimaryButton(
                text = if (uiState.editTransactionId != null) "Simpan Perubahan" else "Simpan Transaksi",
                onClick = { viewModel.saveTransaction() },
                enabled = isFormValid
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
                        iconTint = parseHexColor(wallet.colorHex, NotionSky),
                        logoResId = WalletMasterData.logoResIdForSlug(wallet.logoSlug)
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
                        iconTint = parseHexColor(wallet.colorHex, NotionMint),
                        logoResId = WalletMasterData.logoResIdForSlug(wallet.logoSlug)
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

        // Pemilih tanggal (maks. hari ini — transaksi masa depan tidak
        // masuk saldo). DatePicker bekerja dalam UTC, jadi tanggal lokal
        // dikonversi ke tengah malam UTC sebelum diserahkan ke picker.
        if (showDatePicker) {
            val initialPickerMillis = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                val local = Calendar.getInstance().apply { timeInMillis = uiState.dateMillis }
                set(Calendar.YEAR, local.get(Calendar.YEAR))
                set(Calendar.MONTH, local.get(Calendar.MONTH))
                set(Calendar.DAY_OF_MONTH, local.get(Calendar.DAY_OF_MONTH))
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis
            val datePickerState = rememberDatePickerState(
                initialSelectedDateMillis = initialPickerMillis,
                selectableDates = object : SelectableDates {
                    override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                        val endOfToday = Calendar.getInstance().apply {
                            set(Calendar.HOUR_OF_DAY, 23)
                            set(Calendar.MINUTE, 59)
                            set(Calendar.SECOND, 59)
                            set(Calendar.MILLISECOND, 999)
                        }.timeInMillis
                        return utcTimeMillis <= endOfToday
                    }
                }
            )
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        datePickerState.selectedDateMillis?.let { viewModel.onDatePicked(it) }
                        showDatePicker = false
                    }) { Text("Pilih") }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) { Text("Batal") }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }
    }
}
