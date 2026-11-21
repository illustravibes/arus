package com.illustravibes.arus.feature.wallet

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.illustravibes.arus.R
import com.illustravibes.arus.core.database.entity.WalletEntity
import com.illustravibes.arus.core.database.entity.WalletType
import com.illustravibes.arus.core.designsystem.*
import com.illustravibes.arus.feature.home.mapDrawableIcon
import com.illustravibes.arus.feature.home.parseHexColor

@Composable
fun WalletScreen(
    viewModel: WalletViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    var walletToDelete by remember { mutableStateOf<WalletEntity?>(null) }
    var menuWallet by remember { mutableStateOf<WalletEntity?>(null) }

    Scaffold(
        topBar = {
            ArusTopAppBar(
                title = "Kelola Dompet",
                showBackButton = true,
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
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            ArusPrimaryButton(
                text = "+ Tambah Dompet Baru",
                onClick = { viewModel.openCreateDialog() }
            )

            ArusSectionHeader(title = "Daftar Dompet Aktif (${uiState.wallets.size})")

            if (uiState.wallets.isEmpty()) {
                ArusEmptyState(
                    title = "Belum ada dompet",
                    description = "Tambah dompet pertama Anda dengan tombol di atas.",
                    iconResId = R.drawable.fa_wallet
                )
            } else {
                ArusCard {
                    Column {
                        uiState.wallets.forEachIndexed { index, wallet ->
                            if (index > 0) {
                                HorizontalDivider(color = ArusBorder, thickness = 1.dp)
                            }
                            val calculatedBalance = uiState.walletBalances[wallet.id] ?: wallet.initialBalance
                            val iconTint = parseHexColor(wallet.colorHex, NotionSky)
                            val tileColor = iconTint.copy(alpha = 0.15f)

                            // Baris dompet: ketuk barisnya untuk buka menu
                            // Edit/Hapus (tombol ••• dihapus, keputusan user
                            // 2026-11-21). Nama dompet dominan di atas,
                            // nominal di bawahnya — ini daftar dompet, bukan
                            // daftar saldo (koreksi user 2026-11-21).
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { menuWallet = wallet }
                                    .padding(vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                ArusWalletTile(
                                    logoResId = WalletMasterData.logoResIdForSlug(wallet.logoSlug),
                                    iconResId = mapDrawableIcon(wallet.icon),
                                    tileColor = tileColor,
                                    iconTint = iconTint,
                                    size = 40.dp
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = wallet.name,
                                        style = ArusTypography.titleLarge,
                                        color = ArusTextPrimary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = calculatedBalance.formatRupiah(),
                                        style = ArusTypography.bodyMedium,
                                        color = ArusTextSecondary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                        }
                    }
                }
            }
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
        }

        // Delete Wallet Confirmation Sheet
        if (walletToDelete != null) {
            val targetWallet = walletToDelete!!
            ArusConfirmationSheet(
                title = "Hapus Dompet Ini?",
                message = "Dompet \"${targetWallet.name}\" akan dihapus. Transaksi yang memakai dompet ini tetap tersimpan, tetapi tidak lagi terhubung ke dompet mana pun. Lanjutkan?",
                confirmButtonText = "Ya, Hapus Dompet",
                onConfirm = {
                    viewModel.deleteWallet(targetWallet)
                    walletToDelete = null
                },
                onDismiss = { walletToDelete = null },
                isDestructive = true
            )
        }

        // Action Sheet (bottom sheet ala Notion) untuk satu dompet.
        if (menuWallet != null) {
            val targetWallet = menuWallet!!
            ArusActionSheet(
                title = targetWallet.name,
                items = buildList {
                    add(
                        ArusActionSheetItem(
                            label = "Edit",
                            iconResId = R.drawable.fa_pen,
                            onClick = { viewModel.openEditDialog(targetWallet) }
                        )
                    )
                    if (uiState.wallets.size > 1) {
                        add(
                            ArusActionSheetItem(
                                label = "Hapus",
                                iconResId = R.drawable.fa_trash_can,
                                destructive = true,
                                onClick = { walletToDelete = targetWallet }
                            )
                        )
                    }
                },
                onDismissRequest = { menuWallet = null }
            )
        }

        // Create / Edit Wallet Modal Sheet
        if (uiState.showDialog) {
            ArusFormSheet(
                title = if (uiState.editingWallet == null) "Tambah Dompet Baru" else "Edit Dompet",
                onDismiss = { viewModel.closeDialog() }
            ) {

                    ArusTextField(
                        value = uiState.nameInput,
                        onValueChange = { name -> viewModel.onNameChange(name) },
                        label = "Nama Dompet",
                        placeholder = "mis. Gopay, Bank BCA, Dompet Tunai",
                        errorMessage = uiState.errorMessage
                    )

                    ArusTextField(
                        value = uiState.initialBalanceInput,
                        onValueChange = { bal -> viewModel.onInitialBalanceChange(bal) },
                        label = "Saldo Awal",
                        placeholder = "0",
                        isCurrency = true
                    )

                    // V2-P2-5: di mode Edit, saldo awal bukan saldo berjalan —
                    // jelaskan efek mengubahnya.
                    val editingWallet = uiState.editingWallet
                    if (editingWallet != null) {
                        Text(
                            text = "Saldo berjalan saat ini: ${(uiState.walletBalances[editingWallet.id] ?: editingWallet.initialBalance).formatRupiah()} · Mengubah saldo awal akan menggeser saldo dompet.",
                            style = ArusTypography.labelSmall,
                            color = ArusTextSecondary
                        )
                    }

                    Text(
                        text = "Jenis Dompet",
                        style = ArusTypography.labelLarge,
                        color = ArusTextPrimary
                    )
                    ArusSegmentedControl(
                        options = listOf("Bank", "E-Wallet", "Tunai"),
                        selectedIndex = uiState.selectedTypeIndex,
                        onOptionSelected = { index -> viewModel.onTypeChange(index) }
                    )

                    // Logo dari master data (idn-finlogos): pilih bank /
                    // e-wallet, nama terisi otomatis. Tunai tetap pakai
                    // ikon + warna seperti sebelumnya.
                    if (uiState.selectedType != WalletType.CASH) {
                        Text(
                            text = if (uiState.selectedType == WalletType.BANK) "Pilih Bank" else "Pilih E-Wallet",
                            style = ArusTypography.labelLarge,
                            color = ArusTextPrimary
                        )
                        ArusWalletBrandPicker(
                            brands = WalletMasterData.brandsFor(uiState.selectedType),
                            selectedSlug = uiState.selectedLogoSlug,
                            onBrandSelected = { brand -> viewModel.onBrandSelect(brand) },
                            searchPlaceholder = if (uiState.selectedType == WalletType.BANK) "Cari bank..." else "Cari e-wallet..."
                        )
                    }

                    if (uiState.selectedType == WalletType.CASH) {
                        Text(
                            text = "Ikon",
                            style = ArusTypography.labelLarge,
                            color = ArusTextPrimary
                        )
                        ArusIconPicker(
                            selectedIcon = uiState.selectedIcon,
                            onIconChange = { icon -> viewModel.onIconChange(icon) },
                            selectedColorHex = uiState.selectedColorHex
                        )

                        Text(
                            text = "Warna",
                            style = ArusTypography.labelLarge,
                            color = ArusTextPrimary
                        )
                        ArusColorPicker(
                            selectedColorHex = uiState.selectedColorHex,
                            onColorChange = { hex -> viewModel.onColorChange(hex) }
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    ArusPrimaryButton(
                        text = "Simpan Dompet",
                        onClick = { viewModel.saveWallet() },
                        // V2-P1-6: Simpan nonaktif sampai nama terisi (pola layar Catat).
                        enabled = uiState.nameInput.isNotBlank()
                    )
            }
        }
    }
}
