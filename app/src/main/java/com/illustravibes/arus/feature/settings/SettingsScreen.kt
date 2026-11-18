package com.illustravibes.arus.feature.settings

import android.net.Uri
import android.os.Process
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.illustravibes.arus.R
import com.illustravibes.arus.core.designsystem.*

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateToWalletManagement: () -> Unit,
    onNavigateToCategoryManagement: () -> Unit,
    onBackClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    var showExportPasswordDialog by remember { mutableStateOf(false) }
    var showRestorePasswordDialog by remember { mutableStateOf(false) }
    var passwordInput by remember { mutableStateOf("") }
    var selectedExportUri by remember { mutableStateOf<Uri?>(null) }
    var selectedRestoreUri by remember { mutableStateOf<Uri?>(null) }

    val context = LocalContext.current

    // Setelah restore, database lama sudah ditutup (lihat BackupRepository),
    // jadi aplikasi harus dimulai ulang — tidak ada jalan kembali ke data lama.
    fun restartApp() {
        val launchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        launchIntent?.addFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK or android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
        if (launchIntent != null) context.startActivity(launchIntent)
        Process.killProcess(Process.myPid())
    }

    // SAF Export Launcher
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri ->
        if (uri != null) {
            selectedExportUri = uri
            showExportPasswordDialog = true
        }
    }

    // SAF Restore Launcher
    val restoreLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            selectedRestoreUri = uri
            showRestorePasswordDialog = true
        }
    }

    Scaffold(
        topBar = {
            ArusTopAppBar(
                title = "Pengaturan",
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
                .imePadding()
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            // 1. Management Section
            ArusSectionHeader(title = "Kelola Data")
            ArusPropertySelectorRow(
                label = "Kelola Dompet",
                selectedValue = "${uiState.wallets.size} Dompet",
                onClick = onNavigateToWalletManagement,
                iconResId = R.drawable.fa_wallet,
                tileColor = NotionSkySoft,
                iconTint = NotionSky
            )
            ArusPropertySelectorRow(
                label = "Kelola Kategori",
                selectedValue = "${uiState.categories.size} Kategori",
                onClick = onNavigateToCategoryManagement,
                iconResId = R.drawable.fa_tags,
                tileColor = NotionMintSoft,
                iconTint = NotionMint
            )

            // 2. Backup & Restore Section
            ArusSectionHeader(title = "Backup & Restore Terenkripsi")
            ArusCard {
                Text(
                    text = "Simpan cadangan data terenkripsi AES-GCM ke penyimpanan HP Anda, atau pulihkan data dari file backup.",
                    style = ArusTypography.bodyMedium,
                    color = ArusTextSecondary
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ArusPrimaryButton(
                        text = "Ekspor Backup",
                        onClick = { exportLauncher.launch("arus_backup_${System.currentTimeMillis()}.arus") },
                        modifier = Modifier.weight(1f)
                    )
                    ArusSecondaryButton(
                        text = "Pulihkan Data",
                        onClick = { restoreLauncher.launch(arrayOf("*/*")) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Pesan hasil backup/restore — sebelumnya tidak pernah ditampilkan.
            val backupStatusMessage = uiState.backupMessage
            if (backupStatusMessage != null) {
                ArusCard {
                    Text(
                        text = backupStatusMessage,
                        style = ArusTypography.bodyMedium,
                        color = if (uiState.isBackupSuccess) ArusIncome else ArusExpense
                    )
                }
            }

            // 3. Security & Application Info Section
            ArusSectionHeader(title = "Keamanan & Aplikasi")
            ArusToggleRow(
                title = "Kunci PIN / Biometrik",
                subtitle = "Minta verifikasi keamanan saat membuka Arus",
                checked = uiState.isSecurityLockEnabled,
                onCheckedChange = { enabled -> viewModel.onSecurityToggleChange(enabled) },
                iconResId = R.drawable.fa_sliders,
                tileColor = NotionLavenderSoft,
                iconTint = NotionLavender
            )

            ArusCard {
                Text(
                    text = "Arus Android MVP v0.1.0",
                    style = ArusTypography.titleMedium,
                    color = ArusTextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Aplikasi pencatatan keuangan pribadi offline-first. Data tersimpan 100% aman di HP Anda tanpa koneksi internet.",
                    style = ArusTypography.bodyMedium,
                    color = ArusTextSecondary
                )
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
        }

        // Export Password Prompt Sheet/Dialog
        if (showExportPasswordDialog) {
            ArusPasswordSheet(
                title = "Buat Kata Sandi Backup",
                message = "Masukkan kata sandi minimal 4 karakter untuk mengamankan file backup Anda.",
                confirmButtonText = "Simpan Backup",
                password = passwordInput,
                onPasswordChange = { passwordInput = it },
                onConfirm = {
                    val uri = selectedExportUri
                    if (uri != null && passwordInput.length >= 4) {
                        viewModel.exportBackup(uri, passwordInput)
                        passwordInput = ""
                        showExportPasswordDialog = false
                    }
                },
                onDismiss = {
                    passwordInput = ""
                    showExportPasswordDialog = false
                }
            )
        }

        // Restore Password Prompt Sheet/Dialog
        if (showRestorePasswordDialog) {
            ArusPasswordSheet(
                title = "Masukkan Kata Sandi Restore",
                message = "Masukkan kata sandi yang digunakan saat membuat file backup ini.",
                confirmButtonText = "Pulihkan Data",
                password = passwordInput,
                onPasswordChange = { passwordInput = it },
                onConfirm = {
                    val uri = selectedRestoreUri
                    if (uri != null && passwordInput.length >= 4) {
                        viewModel.restoreBackup(uri, passwordInput)
                        passwordInput = ""
                        showRestorePasswordDialog = false
                    }
                },
                onDismiss = {
                    passwordInput = ""
                    showRestorePasswordDialog = false
                },
                isDestructive = true
            )
        }

        // Restore berhasil = database sudah diganti. Aplikasi wajib restart;
        // menutup dialog ini pun tetap me-restart, karena database lama
        // sudah ditutup dan tidak bisa dipakai lagi.
        if (uiState.restoreCompleted) {
            ArusConfirmationSheet(
                title = "Data Dipulihkan",
                message = "Data Arus berhasil dipulihkan dari backup. Aplikasi perlu dimulai ulang untuk memakai data yang baru dipulihkan.",
                confirmButtonText = "Mulai Ulang Aplikasi",
                onConfirm = { restartApp() },
                onDismiss = { restartApp() }
            )
        }
    }
}
