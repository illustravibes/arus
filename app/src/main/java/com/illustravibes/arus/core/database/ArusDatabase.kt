package com.illustravibes.arus.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.illustravibes.arus.core.database.dao.CategoryDao
import com.illustravibes.arus.core.database.dao.TransactionDao
import com.illustravibes.arus.core.database.dao.WalletDao
import com.illustravibes.arus.core.database.entity.CategoryEntity
import com.illustravibes.arus.core.database.entity.TransactionEntity
import com.illustravibes.arus.core.database.entity.WalletEntity
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory

@Database(
    entities = [
        WalletEntity::class,
        CategoryEntity::class,
        TransactionEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class ArusDatabase : RoomDatabase() {

    abstract fun walletDao(): WalletDao
    abstract fun categoryDao(): CategoryDao
    abstract fun transactionDao(): TransactionDao

    companion object {
        @Volatile
        private var INSTANCE: ArusDatabase? = null

        fun getInstance(context: Context): ArusDatabase {
            return INSTANCE ?: synchronized(this) {
                val passphrase = DatabaseKeyManager.getOrCreateDatabasePassphrase(context.applicationContext)
                val factory = SupportOpenHelperFactory(passphrase)

                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ArusDatabase::class.java,
                    "arus_encrypted.db"
                )
                    .openHelperFactory(factory)
                    .build()

                INSTANCE = instance
                instance
            }
        }

        /**
         * Tutup instance aktif dan buang referensinya, supaya getInstance()
         * berikutnya membangun database baru. Dipakai setelah restore:
         * file database sudah diganti dan/atau passphrase sudah berubah.
         * Perhatian: referensi database lama yang masih dipegang UI menjadi
         * tidak bisa dipakai — aplikasi wajib dimulai ulang sesudah ini.
         */
        fun resetInstance() {
            synchronized(this) {
                INSTANCE?.close()
                INSTANCE = null
            }
        }
    }
}
