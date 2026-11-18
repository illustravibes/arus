package com.illustravibes.arus.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "wallets")
data class WalletEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String,
    val icon: String = "fa_wallet",
    val colorHex: String = "#62AEF0",
    val initialBalance: Long = 0L,
    val createdAt: Long = System.currentTimeMillis()
)
