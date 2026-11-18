package com.illustravibes.arus.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val type: String, // "INCOME", "EXPENSE", or "TRANSFER"
    val amount: Long,
    val dateMillis: Long,
    val walletId: Long,
    val targetWalletId: Long? = null,
    val categoryId: Long? = null,
    val note: String? = null,
    val transferId: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
