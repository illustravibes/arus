package com.illustravibes.arus.data.repository

import androidx.room.withTransaction
import com.illustravibes.arus.core.database.ArusDatabase
import com.illustravibes.arus.core.database.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

class TransactionRepository(private val db: ArusDatabase) {

    val allTransactionsFlow: Flow<List<TransactionEntity>> = db.transactionDao().getAllTransactionsFlow()

    suspend fun getTransactionById(id: Long): TransactionEntity? {
        return db.transactionDao().getTransactionById(id)
    }

    suspend fun updateTransaction(transaction: TransactionEntity) {
        db.transactionDao().updateTransaction(transaction)
    }

    suspend fun addIncome(
        amount: Long,
        dateMillis: Long,
        walletId: Long,
        categoryId: Long,
        note: String? = null
    ): Long {
        require(amount > 0L) { "Nominal transaksi harus lebih dari Rp0" }
        val transaction = TransactionEntity(
            type = "INCOME",
            amount = amount,
            dateMillis = dateMillis,
            walletId = walletId,
            categoryId = categoryId,
            note = note?.trim()
        )
        return db.transactionDao().insertTransaction(transaction)
    }

    suspend fun addExpense(
        amount: Long,
        dateMillis: Long,
        walletId: Long,
        categoryId: Long,
        note: String? = null
    ): Long {
        require(amount > 0L) { "Nominal transaksi harus lebih dari Rp0" }
        val transaction = TransactionEntity(
            type = "EXPENSE",
            amount = amount,
            dateMillis = dateMillis,
            walletId = walletId,
            categoryId = categoryId,
            note = note?.trim()
        )
        return db.transactionDao().insertTransaction(transaction)
    }

    // Atomic Transfer Mutation: creates linked transfer record
    suspend fun addTransfer(
        amount: Long,
        dateMillis: Long,
        sourceWalletId: Long,
        targetWalletId: Long,
        note: String? = null
    ): String {
        require(amount > 0L) { "Nominal transfer harus lebih dari Rp0" }
        require(sourceWalletId != targetWalletId) { "Dompet asal dan tujuan tidak boleh sama" }

        val transferId = UUID.randomUUID().toString()

        db.withTransaction {
            val transferRecord = TransactionEntity(
                type = "TRANSFER",
                amount = amount,
                dateMillis = dateMillis,
                walletId = sourceWalletId,
                targetWalletId = targetWalletId,
                note = note?.trim(),
                transferId = transferId
            )
            db.transactionDao().insertTransaction(transferRecord)
        }

        return transferId
    }

    // Atomic Deletion: handles both standard transactions and linked transfer records
    suspend fun deleteTransaction(transaction: TransactionEntity) {
        db.withTransaction {
            if (transaction.transferId != null) {
                db.transactionDao().deleteTransactionsByTransferId(transaction.transferId)
            } else {
                db.transactionDao().deleteTransaction(transaction)
            }
        }
    }

    fun getIncomeReportFlow(startMillis: Long, endMillis: Long, walletId: Long? = null): Flow<Long> {
        return db.transactionDao().getTotalIncomeInPeriodFlow(startMillis, endMillis, walletId).map { it ?: 0L }
    }

    fun getExpenseReportFlow(startMillis: Long, endMillis: Long, walletId: Long? = null): Flow<Long> {
        return db.transactionDao().getTotalExpenseInPeriodFlow(startMillis, endMillis, walletId).map { it ?: 0L }
    }
}
