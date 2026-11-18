package com.illustravibes.arus.data.repository

import android.content.Context
import android.net.Uri
import androidx.room.Room
import com.illustravibes.arus.core.database.ArusDatabase
import com.illustravibes.arus.core.database.DatabaseKeyManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory
import java.io.File

/**
 * Backup & restore database Arus.
 *
 * Format V2 (lihat BackupFormat): file berisi database SQLCipher + passphrase
 * database yang terbungkus kunci turunan kata sandi pengguna, jadi backup bisa
 * dipulihkan di perangkat lain dengan kata sandi yang sama. File V1 lama tetap
 * bisa dipulihkan, tapi hanya di perangkat yang sama.
 *
 * Restore tidak pernah menimpa database aktif sebelum dua hal terbukti:
 * (1) file berhasil didekripsi (kata sandi benar, file tidak rusak), dan
 * (2) hasil dekripsi terbukti database Arus yang valid (ketiga tabel inti ada).
 * Penukaran file memakai file .bak supaya database lama bisa dikembalikan
 * bila pemindahan gagal. Setelah restore berhasil aplikasi WAJIB dimulai
 * ulang (lihat SettingsScreen): instance Room lama sudah ditutup.
 */
class BackupRepository(
    private val context: Context,
    private val db: ArusDatabase
) {

    companion object {
        private const val DATABASE_NAME = "arus_encrypted.db"
    }

    suspend fun exportBackup(outputUri: Uri, password: String): Result<Boolean> = withContext(Dispatchers.IO) {
        runCatching {
            require(password.length >= 4) { "Kata sandi minimal 4 karakter" }

            // Pastikan seluruh WAL sudah masuk ke file utama sebelum dibaca.
            db.openHelper.writableDatabase.query("PRAGMA wal_checkpoint(FULL)").close()

            val dbFile = context.getDatabasePath(DATABASE_NAME)
            require(dbFile.exists()) { "Database tidak ditemukan" }
            val dbBytes = dbFile.readBytes()

            // Bungkus passphrase SQLCipher dengan kunci dari kata sandi (format V2):
            // file backup jadi bisa dipulihkan di perangkat lain.
            val passphrase = DatabaseKeyManager.getOrCreateDatabasePassphrase(context)
            val backupBytes = BackupFormat.buildV2(password, dbBytes, passphrase)

            context.contentResolver.openOutputStream(outputUri)?.use { outputStream ->
                outputStream.write(backupBytes)
                outputStream.flush()
            } ?: throw IllegalStateException("Gagal membuka file tujuan backup")

            true
        }
    }

    suspend fun restoreBackup(inputUri: Uri, password: String): Result<Boolean> = withContext(Dispatchers.IO) {
        runCatching {
            require(password.length >= 4) { "Kata sandi minimal 4 karakter" }

            val fileBytes = context.contentResolver.openInputStream(inputUri)?.use { it.readBytes() }
                ?: throw IllegalStateException("Gagal membaca file backup")

            // 1. Uraikan sesuai versi format. Kata sandi salah atau file
            //    rusak/diutak-atik gagal di sini, SEBELUM database aktif disentuh.
            val (decryptedDbBytes, restoredPassphrase) = when (BackupFormat.detectVersion(fileBytes)) {
                2 -> {
                    val parsed = BackupFormat.parseV2(password, fileBytes)
                    parsed.dbBytes to parsed.dbPassphrase
                }
                1 -> {
                    // File lama: passphrase tidak tersimpan, jadi hanya bisa
                    // dipulihkan di perangkat ini dengan passphrase yang sekarang.
                    BackupFormat.parseV1(password, fileBytes) to
                        DatabaseKeyManager.getOrCreateDatabasePassphrase(context)
                }
                else -> throw IllegalArgumentException("Format file backup tidak dikenali")
            }

            val dbFile = context.getDatabasePath(DATABASE_NAME)
            val dbDir = requireNotNull(dbFile.parentFile) { "Folder database tidak ditemukan" }
            val tmpFile = File(dbDir, "arus_restore_tmp.db")
            val bakFile = File(dbDir, "arus_encrypted.db.bak")

            // 2. Validasi hasil dekripsi di file sementara: buka sebagai database
            //    SQLCipher dan pastikan ketiga tabel inti ada. File yang bukan
            //    database Arus (mis. salah pilih file) ditolak di sini.
            deleteFileAndSidecars(tmpFile)
            tmpFile.writeBytes(decryptedDbBytes)
            require(isValidArusDatabase(tmpFile, restoredPassphrase)) {
                "Isi backup bukan database Arus yang valid"
            }

            // 3. Tukar file database secara aman: tutup instance Room lama,
            //    singkirkan file lama ke .bak, lalu pindahkan file hasil restore.
            //    Kalau pemindahan gagal, file lama dikembalikan.
            ArusDatabase.resetInstance()
            deleteSidecars(dbFile)
            deleteFileAndSidecars(bakFile)
            val oldKept = if (dbFile.exists()) dbFile.renameTo(bakFile) else false
            if (!tmpFile.renameTo(dbFile)) {
                if (oldKept) bakFile.renameTo(dbFile)
                throw IllegalStateException("Gagal mengganti database")
            }

            // 4. Simpan passphrase hasil restore supaya database bisa dibuka
            //    di perangkat ini, lalu buang file cadangan dan sisa file sementara.
            DatabaseKeyManager.storeDatabasePassphrase(context, restoredPassphrase)
            deleteFileAndSidecars(bakFile)
            deleteSidecars(tmpFile)

            true
        }
    }

    /** Buka file sebagai database Arus dan periksa ketiga tabel inti ada. */
    private fun isValidArusDatabase(file: File, passphrase: ByteArray): Boolean {
        var checkDb: ArusDatabase? = null
        return try {
            val factory = SupportOpenHelperFactory(passphrase)
            checkDb = Room.databaseBuilder(context, ArusDatabase::class.java, file.absolutePath)
                .openHelperFactory(factory)
                .build()
            val cursor = checkDb.openHelper.readableDatabase.query(
                "SELECT COUNT(*) FROM sqlite_master WHERE type = 'table' " +
                    "AND name IN ('wallets', 'categories', 'transactions')"
            )
            cursor.use {
                it.moveToFirst() && it.getInt(0) == 3
            }
        } catch (_: Exception) {
            false
        } finally {
            runCatching { checkDb?.close() }
        }
    }

    private fun deleteFileAndSidecars(file: File) {
        file.delete()
        deleteSidecars(file)
    }

    private fun deleteSidecars(file: File) {
        File(file.path + "-wal").delete()
        File(file.path + "-shm").delete()
        File(file.path + "-journal").delete()
    }
}
