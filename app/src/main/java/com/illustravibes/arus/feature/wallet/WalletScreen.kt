package com.illustravibes.arus.feature.wallet

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.illustravibes.arus.R
import com.illustravibes.arus.core.database.entity.WalletEntity
import com.illustravibes.arus.core.designsystem.*
import com.illustravibes.arus.feature.home.mapDrawableIcon
import com.illustravibes.arus.feature.home.parseHexColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletScreen(
    viewModel: WalletViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    var walletToDelete by remember { mutableStateOf<WalletEntity?>(null) }

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
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            ArusPrimaryButton(
                text = "+ Tambah Dompet Baru",
                onClick = { viewModel.openCreateDialog() }
            )

            ArusSectionHeader(title = "Daftar Dompet Aktif (${uiState.wallets.size})")

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                uiState.wallets.forEach { wallet ->
                    val calculatedBalance = uiState.walletBalances[wallet.id] ?: wallet.initialBalance
                    val iconTint = parseHexColor(wallet.colorHex, NotionSky)
                    val tileColor = iconTint.copy(alpha = 0.15f)

                    ArusCard {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                ArusCategoryTile(
                                    iconResId = mapDrawableIcon(wallet.icon),
                                    tileColor = tileColor,
                                    iconTint = iconTint,
                                    size = 44.dp
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = wallet.name,
                                        style = ArusTypography.titleMedium,
                                        color = ArusTextPrimary
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = calculatedBalance.formatRupiah(),
                                        style = ArusTypography.titleLarge,
                                        color = ArusTextPrimary
                                    )
                                }
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Box(
                                    modifier = Modifier
                                        .clip(PillShape)
                                        .background(ArusBrandSoft)
                                        .clickable { viewModel.openEditDialog(wallet) }
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = "Edit",
                                        style = ArusTypography.labelSmall,
                                        color = ArusBrandPrimary
                                    )
                                }

                                if (uiState.wallets.size > 1) {
                                    Box(
                                        modifier = Modifier
                                            .clip(PillShape)
                                            .background(ArusExpenseSoft)
                                            .clickable { walletToDelete = wallet }
                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = "Hapus",
                                            style = ArusTypography.labelSmall,
                                            color = ArusExpense
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(80.dp))
        }

        // Delete Wallet Confirmation Sheet
        if (walletToDelete != null) {
            val targetWallet = walletToDelete!!
            ArusConfirmationSheet(
                title = "Hapus Dompet Ini?",
                message = "Seluruh catatan transaksi pada dompet \"${targetWallet.name}\" akan terpengaruh. Lanjutkan?",
                confirmButtonText = "Ya, Hapus Dompet",
                onConfirm = {
                    viewModel.deleteWallet(targetWallet)
                    walletToDelete = null
                },
                onDismiss = { walletToDelete = null },
                isDestructive = true
            )
        }

        // Create / Edit Wallet Modal Sheet
        if (uiState.showDialog) {
            ModalBottomSheet(
                onDismissRequest = { viewModel.closeDialog() },
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                containerColor = ArusSurface,
                dragHandle = {
                    Box(
                        modifier = Modifier
                            .padding(vertical = 12.dp)
                            .width(36.dp)
                            .height(4.dp)
                            .clip(PillShape)
                            .background(ArusBorder)
                    )
                }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .padding(bottom = 32.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = if (uiState.editingWallet == null) "Tambah Dompet Baru" else "Edit Dompet",
                        style = ArusTypography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = ArusTextPrimary
                    )

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

                    Spacer(modifier = Modifier.height(8.dp))

                    ArusPrimaryButton(
                        text = "Simpan Dompet",
                        onClick = { viewModel.saveWallet() }
                    )
                }
            }
        }
    }
}
