package com.illustravibes.arus.feature.transaction

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.illustravibes.arus.R
import com.illustravibes.arus.core.designsystem.*
import com.illustravibes.arus.feature.home.mapDrawableIcon
import com.illustravibes.arus.feature.home.parseHexColor
import java.text.SimpleDateFormat
import java.util.*

// B11 (audit awam): nominal sebesar ini ke atas meminta konfirmasi dulu
// sebelum disimpan — salah ketik nol berlebih langsung merusak total.
private const val LARGE_AMOUNT_THRESHOLD = 100_000_000L

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
    var showLargeAmountConfirm by remember { mutableStateOf(false) }

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
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
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
                    label = "Nominal " + when (uiState.typeIndex) {
                        0 -> "pemasukan"
                        1 -> "pengeluaran"
                        else -> "transfer"
                    },
                    errorMessage = uiState.errorMessage,
                    amountColor = when (uiState.typeIndex) {
                        0 -> ArusIncome
                        1 -> ArusExpense
                        else -> ArusTextPrimary
                    },
                )

                // 3. Detail transaksi: tiap field kartu sendiri + label di atasnya
                // (form lebih informatif: Dompet, Kategori, Tanggal).
                if (uiState.typeIndex == 2) {
                    // Transfer: Dompet Asal & Dompet Tujuan
                    val walletTint = parseHexColor(uiState.selectedWallet?.colorHex ?: "#62AEF0", NotionSky)
                    val targetWalletTint = parseHexColor(uiState.selectedTargetWallet?.colorHex ?: "#00A86B", NotionMint)

                    LabeledField(label = "Dompet Asal") {
                        ArusPropertySelectorRow(
                            label = "Dompet Asal",
                            valueOnLeft = true,
                            selectedValue = uiState.selectedWallet?.name ?: "",
                            onClick = { showWalletSheet = true },
                            iconResId = mapDrawableIcon(uiState.selectedWallet?.icon ?: "fa_wallet"),
                            tileColor = walletTint.copy(alpha = 0.15f),
                            iconTint = walletTint,
                            logoResId = WalletMasterData.logoResIdForSlug(uiState.selectedWallet?.logoSlug),
                            compact = true
                        )
                    }
                    LabeledField(label = "Dompet Tujuan") {
                        ArusPropertySelectorRow(
                            label = "Dompet Tujuan",
                            valueOnLeft = true,
                            selectedValue = uiState.selectedTargetWallet?.name ?: "",
                            onClick = { showTargetWalletSheet = true },
                            iconResId = mapDrawableIcon(uiState.selectedTargetWallet?.icon ?: "fa_wallet"),
                            tileColor = targetWalletTint.copy(alpha = 0.15f),
                            iconTint = targetWalletTint,
                            logoResId = WalletMasterData.logoResIdForSlug(uiState.selectedTargetWallet?.logoSlug),
                            compact = true
                        )
                    }
                } else {
                    // Pemasukan/Pengeluaran: Dompet & Kategori
                    val walletTint = parseHexColor(uiState.selectedWallet?.colorHex ?: "#62AEF0", NotionSky)
                    val categoryTint = parseHexColor(uiState.selectedCategory?.colorHex ?: "#00A86B", NotionMint)

                    LabeledField(label = "Dompet") {
                        ArusPropertySelectorRow(
                            label = "Pilih Dompet",
                            valueOnLeft = true,
                            selectedValue = uiState.selectedWallet?.name ?: "",
                            onClick = { showWalletSheet = true },
                            iconResId = mapDrawableIcon(uiState.selectedWallet?.icon ?: "fa_wallet"),
                            tileColor = walletTint.copy(alpha = 0.15f),
                            iconTint = walletTint,
                            logoResId = WalletMasterData.logoResIdForSlug(uiState.selectedWallet?.logoSlug),
                            compact = true
                        )
                    }
                    LabeledField(label = "Kategori") {
                        ArusPropertySelectorRow(
                            label = "Kategori",
                            valueOnLeft = true,
                            selectedValue = uiState.selectedCategory?.name ?: "",
                            onClick = { showCategorySheet = true },
                            iconResId = mapDrawableIcon(uiState.selectedCategory?.icon ?: "fa_tags"),
                            tileColor = categoryTint.copy(alpha = 0.15f),
                            iconTint = categoryTint,
                            compact = true
                        )
                    }
                }
                // Tanggal transaksi (bisa diubah; maks. hari ini).
                // tileColor: NotionSkySoft polos (dulu NotionSkySoft 15% — nyaris
                // tak terlihat di atas putih; pola yang sama dengan Kelola Data).
                LabeledField(label = "Tanggal") {
                    ArusPropertySelectorRow(
                        label = "Tanggal",
                        valueOnLeft = true,
                        selectedValue = SimpleDateFormat("EEEE, d MMM yyyy · HH:mm", Locale("id", "ID")).format(Date(uiState.dateMillis)),
                        onClick = { showDatePicker = true },
                        iconResId = R.drawable.fa_calendar,
                        tileColor = NotionSkySoft,
                        iconTint = NotionSky,
                        compact = true
                    )
                }

                // 4. Note Field
                ArusTextField(
                    value = uiState.note,
                    onValueChange = { note -> viewModel.onNoteChange(note) },
                    label = "Catatan Transaksi",
                    placeholder = "mis. Makan siang bersama Budi",
                    helperText = "Ditampilkan sebagai judul transaksi di Beranda & Riwayat"
                )

                Spacer(modifier = Modifier.height(8.dp))
            }

            // 5. Submit Button — ditempel ke bawah layar; P1-6: nonaktif
            // sampai input valid.
            val isFormValid = (uiState.amountInput.toLongOrNull() ?: 0L) > 0 &&
                uiState.selectedWallet != null &&
                if (uiState.typeIndex == 2) uiState.selectedTargetWallet != null
                else uiState.selectedCategory != null
            ArusPrimaryButton(
                text = if (uiState.editTransactionId != null) "Simpan Perubahan" else "Simpan Transaksi",
                onClick = {
                    // B11 (audit awam): nominal besar minta konfirmasi dulu.
                    val amount = uiState.amountInput.toLongOrNull() ?: 0L
                    if (amount >= LARGE_AMOUNT_THRESHOLD) showLargeAmountConfirm = true
                    else viewModel.saveTransaction()
                },
                enabled = isFormValid
            )

            Spacer(modifier = Modifier.height(16.dp))
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
        // masuk saldo). Bottom sheet kalender ala Bevel, locale id-ID.
        // DatePicker bekerja dalam UTC, jadi tanggal lokal dikonversi ke
        // tengah malam UTC sebelum diserahkan ke picker.
        if (showDatePicker) {
            ArusDatePickerSheet(
                initialDateMillis = uiState.dateMillis,
                onDateSelected = { dateTimeMillis ->
                    viewModel.onDatePicked(dateTimeMillis)
                    showDatePicker = false
                },
                onDismiss = { showDatePicker = false }
            )
        }

        // B11 (audit awam): konfirmasi nominal besar.
        if (showLargeAmountConfirm) {
            val amount = uiState.amountInput.toLongOrNull() ?: 0L
            ArusConfirmationSheet(
                title = "Nominal besar",
                message = "Anda mencatat ${amount.formatRupiah()} — pastikan nominalnya sudah benar sebelum disimpan.",
                confirmButtonText = "Ya, Simpan",
                onConfirm = {
                    showLargeAmountConfirm = false
                    viewModel.saveTransaction()
                },
                onDismiss = { showLargeAmountConfirm = false }
            )
        }
    }
}

/**
 * Satu field form: label di atas + isi berupa pill penuh (baris selector
 * standalone, 56dp — sama tinggi dengan field Catatan Transaksi).
 * Dipakai di form Catat (Dompet, Kategori, Tanggal) agar tiap baris jelas
 * maksudnya — gaya label sama dengan [ArusTextField].
 */
@Composable
private fun LabeledField(
    label: String,
    content: @Composable () -> Unit
) {
    Column {
        Text(
            text = label,
            style = ArusTypography.labelLarge,
            color = ArusTextPrimary,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        content()
    }
}
