package com.illustravibes.arus.data.repository

import com.illustravibes.arus.core.database.ArusDatabase
import com.illustravibes.arus.core.database.entity.WalletEntity
import com.illustravibes.arus.core.database.entity.WalletType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class WalletRepository(private val db: ArusDatabase) {

    val walletsFlow: Flow<List<WalletEntity>> = db.walletDao().getWalletsFlow()

    fun getWalletCalculatedBalanceFlow(walletId: Long): Flow<Long> {
        return db.walletDao().getCalculatedBalanceFlow(walletId).map { it ?: 0L }
    }

    val totalCombinedBalanceFlow: Flow<Long> = db.walletDao().getTotalCombinedBalanceFlow().map { it ?: 0L }

    suspend fun getWalletById(id: Long): WalletEntity? {
        return db.walletDao().getWalletById(id)
    }

    suspend fun createWallet(
        name: String,
        initialBalance: Long,
        icon: String = "fa_wallet",
        colorHex: String = "#62AEF0",
        type: String = WalletType.CASH.name,
        logoSlug: String? = null
    ): Long {
        require(name.isNotBlank()) { "Nama dompet tidak boleh kosong" }
        require(initialBalance >= 0L) { "Saldo awal tidak boleh negatif" }
        val wallet = WalletEntity(
            name = name.trim(),
            initialBalance = initialBalance,
            icon = icon,
            colorHex = colorHex,
            type = type,
            logoSlug = logoSlug
        )
        return db.walletDao().insertWallet(wallet)
    }

    suspend fun updateWallet(wallet: WalletEntity) {
        require(wallet.name.isNotBlank()) { "Nama dompet tidak boleh kosong" }
        db.walletDao().updateWallet(wallet)
    }

    suspend fun deleteWallet(wallet: WalletEntity) {
        db.walletDao().deleteWallet(wallet)
    }
}
