package com.illustravibes.arus.core.database.dao

import androidx.room.*
import com.illustravibes.arus.core.database.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    @Query("SELECT * FROM transactions ORDER BY dateMillis DESC, createdAt DESC")
    fun getAllTransactionsFlow(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE id = :id LIMIT 1")
    suspend fun getTransactionById(id: Long): TransactionEntity?

    @Query("SELECT * FROM transactions WHERE transferId = :transferId")
    suspend fun getTransactionsByTransferId(transferId: String): List<TransactionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity): Long

    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)

    @Delete
    suspend fun deleteTransaction(transaction: TransactionEntity)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteTransactionById(id: Long)

    @Query("DELETE FROM transactions WHERE transferId = :transferId")
    suspend fun deleteTransactionsByTransferId(transferId: String)

    // Report Summary Query: Total Income in period (excluding transfers & future-dated)
    @Query("""
        SELECT COALESCE(SUM(amount), 0)
        FROM transactions
        WHERE type = 'INCOME'
          AND dateMillis >= :startMillis
          AND dateMillis <= :endMillis
          AND dateMillis <= :nowMillis
          AND (:walletId IS NULL OR walletId = :walletId)
    """)
    fun getTotalIncomeInPeriodFlow(startMillis: Long, endMillis: Long, walletId: Long? = null, nowMillis: Long = System.currentTimeMillis()): Flow<Long?>

    // Report Summary Query: Total Expense in period (excluding transfers & future-dated)
    @Query("""
        SELECT COALESCE(SUM(amount), 0)
        FROM transactions
        WHERE type = 'EXPENSE'
          AND dateMillis >= :startMillis
          AND dateMillis <= :endMillis
          AND dateMillis <= :nowMillis
          AND (:walletId IS NULL OR walletId = :walletId)
    """)
    fun getTotalExpenseInPeriodFlow(startMillis: Long, endMillis: Long, walletId: Long? = null, nowMillis: Long = System.currentTimeMillis()): Flow<Long?>
}
