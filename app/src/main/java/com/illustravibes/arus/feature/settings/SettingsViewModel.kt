package com.illustravibes.arus.feature.settings

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
    val wallets: List<WalletEntity> = emptyList(),
    val categories: List<CategoryEntity> = emptyList(),
    val isSecurityLockEnabled: Boolean = false,
    val isBackupLoading: Boolean = false,
    val backupMessage: String? = null,
    val isBackupSuccess: Boolean = false,
    // True hanya sesudah restore berhasil: database sudah diganti dan
    // aplikasi wajib dimulai ulang (lihat SettingsScreen).
    val restoreCompleted: Boolean = false
)

class SettingsViewModel(
    private val walletRepository: WalletRepository,
    private val categoryRepository: CategoryRepository,
    private val backupRepository: BackupRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        combine(
            walletRepository.walletsFlow,
            categoryRepository.activeCategoriesFlow
        ) { wallets, categories ->
            SettingsUiState(
                wallets = wallets,
                categories = categories
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
                    restoreCompleted = current.restoreCompleted
                )
            }
        }.launchIn(viewModelScope)
    }

    fun onSecurityToggleChange(enabled: Boolean) {
        _uiState.update { it.copy(isSecurityLockEnabled = enabled) }
    }

    fun exportBackup(uri: Uri, password: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isBackupLoading = true, backupMessage = null) }
            val result = backupRepository.exportBackup(uri, password)
            result.onSuccess {
                _uiState.update { it.copy(isBackupLoading = false, backupMessage = "Backup terenkripsi berhasil disimpan!", isBackupSuccess = true) }
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
