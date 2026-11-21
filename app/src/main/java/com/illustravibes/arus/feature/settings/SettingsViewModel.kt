package com.illustravibes.arus.feature.settings

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.illustravibes.arus.core.database.entity.CategoryEntity
import com.illustravibes.arus.core.database.entity.WalletEntity
import com.illustravibes.arus.data.repository.BackupRepository
import com.illustravibes.arus.data.repository.CategoryRepository
import com.illustravibes.arus.data.repository.WalletRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class SettingsUiState(
    val isLoading: Boolean = true,
    val wallets: List<WalletEntity> = emptyList(),
    val categories: List<CategoryEntity> = emptyList(),
    val isBackupLoading: Boolean = false,
    val backupMessage: String? = null,
    val isBackupSuccess: Boolean = false,
    // B7 (audit awam): waktu backup terakhir yang berhasil (millis sejak
    // epoch, null bila belum pernah) — tersimpan di SharedPreferences.
    val lastBackupMillis: Long? = null,
    // True hanya sesudah restore berhasil: database sudah diganti dan
    // aplikasi wajib dimulai ulang (lihat SettingsScreen).
    val restoreCompleted: Boolean = false
)

class SettingsViewModel(
    private val walletRepository: WalletRepository,
    private val categoryRepository: CategoryRepository,
    private val backupRepository: BackupRepository,
    context: Context
) : ViewModel() {

    private val prefs = context.applicationContext.getSharedPreferences("arus_prefs", Context.MODE_PRIVATE)

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        // B7 (audit awam): baca waktu backup terakhir sebelum loadData —
        // onEach di loadData() mempertahankan nilai ini.
        _uiState.update {
            it.copy(lastBackupMillis = prefs.getLong("last_backup_millis", -1L).takeIf { millis -> millis >= 0L })
        }
        loadData()
    }

    private fun loadData() {
        combine(
            walletRepository.walletsFlow,
            categoryRepository.activeCategoriesFlow
        ) { wallets, categories ->
            SettingsUiState(
                wallets = wallets,
                categories = categories,
                isLoading = false
            )
        }.onEach { state ->
            // Jangan menimpa status backup: loadData() jalan ulang tiap kali
            // Flow dompet/kategori berubah, dan pesan hasil backup/restore
            // harus tetap terlihat sampai pengguna menutupnya.
            _uiState.update { current ->
                state.copy(
                    isBackupLoading = current.isBackupLoading,
                    backupMessage = current.backupMessage,
                    isBackupSuccess = current.isBackupSuccess,
                    lastBackupMillis = current.lastBackupMillis,
                    restoreCompleted = current.restoreCompleted
                )
            }
        }.launchIn(viewModelScope)
    }

    fun exportBackup(uri: Uri, password: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isBackupLoading = true, backupMessage = null) }
            val result = backupRepository.exportBackup(uri, password)
            result.onSuccess {
                // B7 (audit awam): catat waktu backup berhasil.
                val now = System.currentTimeMillis()
                prefs.edit().putLong("last_backup_millis", now).apply()
                _uiState.update { it.copy(isBackupLoading = false, backupMessage = "Backup terenkripsi berhasil disimpan!", isBackupSuccess = true, lastBackupMillis = now) }
            }.onFailure { err ->
                _uiState.update { it.copy(isBackupLoading = false, backupMessage = "Gagal backup: ${err.localizedMessage}") }
            }
        }
    }

    fun restoreBackup(uri: Uri, password: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isBackupLoading = true, backupMessage = null) }
            val result = backupRepository.restoreBackup(uri, password)
            result.onSuccess {
                _uiState.update { it.copy(isBackupLoading = false, backupMessage = "Data berhasil dipulihkan dari backup!", isBackupSuccess = true, restoreCompleted = true) }
            }.onFailure { err ->
                _uiState.update { it.copy(isBackupLoading = false, backupMessage = "Gagal memulihkan backup: ${err.localizedMessage}", restoreCompleted = false) }
            }
        }
    }

    fun clearBackupMessage() {
        _uiState.update { it.copy(backupMessage = null, isBackupSuccess = false, restoreCompleted = false) }
    }
}
