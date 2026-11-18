package com.illustravibes.arus.core.database.dao

import androidx.room.*
import com.illustravibes.arus.core.database.entity.WalletEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WalletDao {

    @Query("SELECT * FROM wallets ORDER BY createdAt ASC")
    fun getWalletsFlow(): Flow<List<WalletEntity>>

    @Query("SELECT * FROM wallets WHERE id = :id LIMIT 1")
    suspend fun getWalletById(id: Long): WalletEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWallet(wallet: WalletEntity): Long

    @Update
    suspend fun updateWallet(wallet: WalletEntity)

    @Delete
    suspend fun deleteWallet(wallet: WalletEntity)

    // Reactive Ledger Query: Calculate balance for a specific wallet ignoring future-dated transactions
    @Query("""
        SELECT 
            (
                w.initialBalance 
                + COALESCE((SELECT SUM(amount) FROM transactions WHERE walletId = w.id AND type = 'INCOME' AND dateMillis <= :nowMillis), 0)
                - COALESCE((SELECT SUM(amount) FROM transactions WHERE walletId = w.id AND type = 'EXPENSE' AND dateMillis <= :nowMillis), 0)
                + COALESCE((SELECT SUM(amount) FROM transactions WHERE targetWalletId = w.id AND type = 'TRANSFER' AND dateMillis <= :nowMillis), 0)
                - COALESCE((SELECT SUM(amount) FROM transactions WHERE walletId = w.id AND type = 'TRANSFER' AND dateMillis <= :nowMillis), 0)
            )
        FROM wallets w
        WHERE w.id = :walletId
    """)
    fun getCalculatedBalanceFlow(walletId: Long, nowMillis: Long = System.currentTimeMillis()): Flow<Long?>

    // Total combined balance of all wallets
    @Query("""
        SELECT 
            COALESCE(SUM(initialBalance), 0)
            + COALESCE((SELECT SUM(amount) FROM transactions WHERE type = 'INCOME' AND dateMillis <= :nowMillis), 0)
            - COALESCE((SELECT SUM(amount) FROM transactions WHERE type = 'EXPENSE' AND dateMillis <= :nowMillis), 0)
        FROM wallets
    """)
    fun getTotalCombinedBalanceFlow(nowMillis: Long = System.currentTimeMillis()): Flow<Long?>
}
