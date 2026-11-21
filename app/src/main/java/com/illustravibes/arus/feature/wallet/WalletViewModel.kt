package com.illustravibes.arus.feature.wallet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.illustravibes.arus.core.database.entity.WalletEntity
import com.illustravibes.arus.core.database.entity.WalletType
import com.illustravibes.arus.core.designsystem.WalletBrand
import com.illustravibes.arus.core.designsystem.WalletMasterData
import com.illustravibes.arus.data.repository.TransactionRepository
import com.illustravibes.arus.data.repository.WalletRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class WalletUiState(
    val isLoading: Boolean = true,
    val wallets: List<WalletEntity> = emptyList(),
    val walletBalances: Map<Long, Long> = emptyMap(),
    val showDialog: Boolean = false,
    val editingWallet: WalletEntity? = null,
    val nameInput: String = "",
    val initialBalanceInput: String = "",
    val selectedIcon: String = "fa_wallet",
    val selectedColorHex: String = "#62AEF0",
    val selectedType: WalletType = WalletType.CASH,
    val selectedTypeIndex: Int = 2,
    val selectedLogoSlug: String? = null,
    val errorMessage: String? = null
)

class WalletViewModel(
    private val walletRepository: WalletRepository,
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WalletUiState())
    val uiState: StateFlow<WalletUiState> = _uiState.asStateFlow()

    init {
        loadWallets()
    }

    private fun loadWallets() {
        combine(
            walletRepository.walletsFlow,
            transactionRepository.allTransactionsFlow
        ) { wallets, allTransactions ->
            val nowMillis = System.currentTimeMillis()
            val balances = wallets.associate { wallet ->
                val calculated = wallet.initialBalance +
                        allTransactions.filter { it.walletId == wallet.id && it.type == "INCOME" && it.dateMillis <= nowMillis }.sumOf { it.amount } -
                        allTransactions.filter { it.walletId == wallet.id && it.type == "EXPENSE" && it.dateMillis <= nowMillis }.sumOf { it.amount } +
                        allTransactions.filter { it.targetWalletId == wallet.id && it.type == "TRANSFER" && it.dateMillis <= nowMillis }.sumOf { it.amount } -
                        allTransactions.filter { it.walletId == wallet.id && it.type == "TRANSFER" && it.dateMillis <= nowMillis }.sumOf { it.amount }
                wallet.id to calculated
            }
            WalletUiState(
                wallets = wallets,
                walletBalances = balances,
                isLoading = false
            )
        }.onEach { state ->
            _uiState.value = state
        }.launchIn(viewModelScope)
    }

    fun openCreateDialog() {
        _uiState.update {
            it.copy(
                showDialog = true,
                editingWallet = null,
                nameInput = "",
                initialBalanceInput = "0",
                selectedIcon = "fa_wallet",
                selectedColorHex = "#62AEF0",
                selectedType = WalletType.CASH,
                selectedTypeIndex = 2,
                selectedLogoSlug = null,
                errorMessage = null
            )
        }
    }

    fun openEditDialog(wallet: WalletEntity) {
        _uiState.update {
            it.copy(
                showDialog = true,
                editingWallet = wallet,
                nameInput = wallet.name,
                initialBalanceInput = wallet.initialBalance.toString(),
                selectedIcon = wallet.icon,
                selectedColorHex = wallet.colorHex,
                selectedType = runCatching { WalletType.valueOf(wallet.type) }.getOrDefault(WalletType.CASH),
                selectedTypeIndex = when (runCatching { WalletType.valueOf(wallet.type) }.getOrDefault(WalletType.CASH)) {
                    WalletType.BANK -> 0
                    WalletType.EWALLET -> 1
                    WalletType.CASH -> 2
                },
                selectedLogoSlug = wallet.logoSlug,
                errorMessage = null
            )
        }
    }

    fun closeDialog() {
        _uiState.update { it.copy(showDialog = false) }
    }

    fun onNameChange(name: String) {
        _uiState.update { it.copy(nameInput = name, errorMessage = null) }
    }

    fun onInitialBalanceChange(balance: String) {
        val cleanDigits = balance.filter { it.isDigit() }
        _uiState.update { it.copy(initialBalanceInput = cleanDigits, errorMessage = null) }
    }

    fun onIconChange(icon: String) {
        _uiState.update { it.copy(selectedIcon = icon) }
    }

    fun onColorChange(colorHex: String) {
        _uiState.update { it.copy(selectedColorHex = colorHex) }
    }

    fun onTypeChange(index: Int) {
        val type = when (index) {
            0 -> WalletType.BANK
            1 -> WalletType.EWALLET
            else -> WalletType.CASH
        }
        _uiState.update { state ->
            // Logo hanya dipertahankan bila mereknya sejenis dengan tipe baru.
            val keepLogo = state.selectedLogoSlug?.let {
                WalletMasterData.brandForSlug(it)?.type == type
            } == true
            state.copy(
                selectedType = type,
                selectedTypeIndex = index,
                selectedLogoSlug = if (keepLogo) state.selectedLogoSlug else null
            )
        }
    }

    fun onBrandSelect(brand: WalletBrand) {
        _uiState.update { state ->
            // Nama terisi otomatis dari merek, kecuali user sudah mengetik
            // nama sendiri (bukan nama merek sebelumnya).
            val prevBrandName = state.selectedLogoSlug?.let {
                WalletMasterData.brandForSlug(it)?.name
            }
            val newName = if (state.nameInput.isBlank() || state.nameInput == prevBrandName) {
                brand.name
            } else {
                state.nameInput
            }
            state.copy(selectedLogoSlug = brand.slug, nameInput = newName, errorMessage = null)
        }
    }

    fun saveWallet() {
        viewModelScope.launch {
            val state = _uiState.value
            if (state.nameInput.isBlank()) {
                _uiState.update { it.copy(errorMessage = "Nama dompet tidak boleh kosong") }
                return@launch
            }

            // V2-P2-6: nama dompet duplikat (abaikan kapitalisasi/spasi) ditolak.
            val normalizedName = state.nameInput.trim()
            val isDuplicateName = state.wallets.any {
                it.name.trim().equals(normalizedName, ignoreCase = true) && it.id != state.editingWallet?.id
            }
            if (isDuplicateName) {
                _uiState.update { it.copy(errorMessage = "Nama dompet sudah dipakai") }
                return@launch
            }

            val initialBalance = state.initialBalanceInput.toLongOrNull() ?: 0L

            if (state.editingWallet == null) {
                walletRepository.createWallet(
                    name = state.nameInput,
                    initialBalance = initialBalance,
                    icon = state.selectedIcon,
                    colorHex = state.selectedColorHex,
                    type = state.selectedType.name,
                    logoSlug = state.selectedLogoSlug
                )
            } else {
                walletRepository.updateWallet(
                    state.editingWallet.copy(
                        name = state.nameInput.trim(),
                        initialBalance = initialBalance,
                        icon = state.selectedIcon,
                        colorHex = state.selectedColorHex,
                        type = state.selectedType.name,
                        logoSlug = state.selectedLogoSlug
                    )
                )
            }

            _uiState.update { it.copy(showDialog = false) }
        }
    }

    fun deleteWallet(wallet: WalletEntity) {
        viewModelScope.launch {
            walletRepository.deleteWallet(wallet)
        }
    }
}
