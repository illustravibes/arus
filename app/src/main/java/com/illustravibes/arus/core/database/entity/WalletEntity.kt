package com.illustravibes.arus.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class WalletType { BANK, EWALLET, CASH }

@Entity(tableName = "wallets")
data class WalletEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String,
    val icon: String = "fa_wallet",
    val colorHex: String = "#62AEF0",
    val initialBalance: Long = 0L,
    val createdAt: Long = System.currentTimeMillis(),
    // Jenis dompet: BANK | EWALLET | CASH. Slug logo (idn-finlogos) hanya
    // untuk BANK/EWALLET; null = tampilkan ikon fa + warna seperti dulu.
    val type: String = WalletType.CASH.name,
    val logoSlug: String? = null
)
