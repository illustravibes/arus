package com.illustravibes.arus.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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
    version = 2,
    exportSchema = true
)
abstract class ArusDatabase : RoomDatabase() {

    abstract fun walletDao(): WalletDao
    abstract fun categoryDao(): CategoryDao
    abstract fun transactionDao(): TransactionDao

    companion object {
        @Volatile
        private var INSTANCE: ArusDatabase? = null

        // Migrasi 1 -> 2: kolom jenis dompet + slug logo. Tabel di-rebuild
        // (bukan ALTER + DEFAULT) supaya skema hasil sama persis dengan
        // yang Room harapkan untuk versi 2. Data lama dipetakan dari nama:
        // "Bank BCA" -> BANK/bca, "GOPAY" -> EWALLET/gopay, sisanya CASH.
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `wallets_new` (" +
                        "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "`name` TEXT NOT NULL, `icon` TEXT NOT NULL, " +
                        "`colorHex` TEXT NOT NULL, " +
                        "`initialBalance` INTEGER NOT NULL, " +
                        "`createdAt` INTEGER NOT NULL, " +
                        "`type` TEXT NOT NULL, `logoSlug` TEXT)"
                )
                db.execSQL(
                    "INSERT INTO `wallets_new` " +
                        "(`id`, `name`, `icon`, `colorHex`, `initialBalance`, `createdAt`, `type`, `logoSlug`) " +
                        "SELECT `id`, `name`, `icon`, `colorHex`, `initialBalance`, `createdAt`, " +
                        "CASE WHEN `name` LIKE '%GOPAY%' THEN 'EWALLET' " +
                        "WHEN `name` LIKE '%BCA%' THEN 'BANK' ELSE 'CASH' END, " +
                        "CASE WHEN `name` LIKE '%BCA%' THEN 'bca' " +
                        "WHEN `name` LIKE '%GOPAY%' THEN 'gopay' ELSE NULL END " +
                        "FROM `wallets`"
                )
                db.execSQL("DROP TABLE IF EXISTS `wallets`")
                db.execSQL("ALTER TABLE `wallets_new` RENAME TO `wallets`")
            }
        }

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
                    .addMigrations(MIGRATION_1_2)
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
