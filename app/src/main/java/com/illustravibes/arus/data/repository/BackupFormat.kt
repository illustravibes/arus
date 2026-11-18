package com.illustravibes.arus.data.repository

import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

/**
 * Format file backup Arus. Pure Kotlin tanpa dependensi Android, jadi bisa
 * di-unit-test di JVM biasa (lihat BackupFormatTest).
 *
 * V1 (lama): header "ARUS_BACKUP_V1" + salt(16) + iv(12) + AES-GCM(db bytes).
 *   Passphrase SQLCipher TIDAK ikut tersimpan, jadi file V1 hanya bisa
 *   dipulihkan di perangkat yang sama dengan passphrase yang sama.
 *
 * V2: header "ARUS_BACKUP_V2" + salt(16) + ivKey(12)
 *   + AES-GCM(passphrase database 32 byte) + ivDb(12) + AES-GCM(db bytes).
 *   Passphrase ikut terbungkus kunci turunan dari kata sandi pengguna, jadi
 *   file V2 bisa dipulihkan di perangkat lain dengan kata sandi yang sama.
 *   GCM tag (16 byte) menempel di ujung tiap ciphertext: kata sandi salah
 *   atau file yang diutak-atik gagal dekripsi sebelum database disentuh.
 */
object BackupFormat {
    const val HEADER_V1 = "ARUS_BACKUP_V1"
    const val HEADER_V2 = "ARUS_BACKUP_V2"
    const val SALT_SIZE = 16
    const val IV_SIZE = 12
    const val PASSPHRASE_SIZE = 32
    const val ITERATIONS_V1 = 10_000
    const val ITERATIONS_V2 = 210_000
    const val KEY_LENGTH_BITS = 256
    private const val GCM_TAG_BITS = 128

    private val HEADER_V1_BYTES = HEADER_V1.toByteArray(Charsets.UTF_8)
    private val HEADER_V2_BYTES = HEADER_V2.toByteArray(Charsets.UTF_8)

    fun detectVersion(bytes: ByteArray): Int? = when {
        bytes.startsWith(HEADER_V1_BYTES) -> 1
        bytes.startsWith(HEADER_V2_BYTES) -> 2
        else -> null
    }

    private fun ByteArray.startsWith(prefix: ByteArray): Boolean {
        if (size < prefix.size) return false
        return prefix.indices.all { this[it] == prefix[it] }
    }

    fun deriveKey(password: String, salt: ByteArray, iterations: Int): SecretKeySpec {
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val spec = PBEKeySpec(password.toCharArray(), salt, iterations, KEY_LENGTH_BITS)
        return SecretKeySpec(factory.generateSecret(spec).encoded, "AES")
    }

    fun randomBytes(size: Int): ByteArray = ByteArray(size).also { SecureRandom().nextBytes(it) }

    fun encryptAesGcm(key: SecretKeySpec, iv: ByteArray, plaintext: ByteArray): ByteArray {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key, GCMParameterSpec(GCM_TAG_BITS, iv))
        return cipher.doFinal(plaintext)
    }

    fun decryptAesGcm(key: SecretKeySpec, iv: ByteArray, ciphertext: ByteArray): ByteArray {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(GCM_TAG_BITS, iv))
        return cipher.doFinal(ciphertext)
    }

    /** Bangun file backup V2 dari byte database + passphrase SQLCipher-nya. */
    fun buildV2(password: String, dbBytes: ByteArray, dbPassphrase: ByteArray): ByteArray {
        require(password.length >= 4) { "Kata sandi minimal 4 karakter" }
        require(dbPassphrase.size == PASSPHRASE_SIZE) { "Passphrase database harus 32 byte" }
        val salt = randomBytes(SALT_SIZE)
        val key = deriveKey(password, salt, ITERATIONS_V2)
        val ivKey = randomBytes(IV_SIZE)
        val encryptedPassphrase = encryptAesGcm(key, ivKey, dbPassphrase)
        val ivDb = randomBytes(IV_SIZE)
        val encryptedDb = encryptAesGcm(key, ivDb, dbBytes)
        return HEADER_V2_BYTES + salt + ivKey + encryptedPassphrase + ivDb + encryptedDb
    }

    data class ParsedV2(val dbBytes: ByteArray, val dbPassphrase: ByteArray)

    /**
     * Uraikan file V2. Gagal (AEADBadTagException / IllegalArgumentException)
     * bila kata sandi salah atau file rusak/diutak-atik.
     */
    fun parseV2(password: String, fileBytes: ByteArray): ParsedV2 {
        require(password.length >= 4) { "Kata sandi minimal 4 karakter" }
        val minSize = HEADER_V2_BYTES.size + SALT_SIZE + IV_SIZE +
            (PASSPHRASE_SIZE + 16) + IV_SIZE + 1
        require(fileBytes.size >= minSize) { "File backup tidak valid atau rusak" }
        require(fileBytes.startsWith(HEADER_V2_BYTES)) { "Format file backup tidak dikenali" }
        var offset = HEADER_V2_BYTES.size
        val salt = fileBytes.copyOfRange(offset, offset + SALT_SIZE)
        offset += SALT_SIZE
        val ivKey = fileBytes.copyOfRange(offset, offset + IV_SIZE)
        offset += IV_SIZE
        val encryptedPassphrase = fileBytes.copyOfRange(offset, offset + PASSPHRASE_SIZE + 16)
        offset += PASSPHRASE_SIZE + 16
        val ivDb = fileBytes.copyOfRange(offset, offset + IV_SIZE)
        offset += IV_SIZE
        val encryptedDb = fileBytes.copyOfRange(offset, fileBytes.size)
        val key = deriveKey(password, salt, ITERATIONS_V2)
        val dbPassphrase = decryptAesGcm(key, ivKey, encryptedPassphrase)
        require(dbPassphrase.size == PASSPHRASE_SIZE) { "File backup tidak valid atau rusak" }
        val dbBytes = decryptAesGcm(key, ivDb, encryptedDb)
        return ParsedV2(dbBytes, dbPassphrase)
    }

    /** Uraikan file V1 (hanya byte database; passphrase harus passphrase perangkat ini). */
    fun parseV1(password: String, fileBytes: ByteArray): ByteArray {
        require(password.length >= 4) { "Kata sandi minimal 4 karakter" }
        val minSize = HEADER_V1_BYTES.size + SALT_SIZE + IV_SIZE + 1
        require(fileBytes.size >= minSize) { "File backup tidak valid atau rusak" }
        require(fileBytes.startsWith(HEADER_V1_BYTES)) { "Format file backup tidak dikenali" }
        var offset = HEADER_V1_BYTES.size
        val salt = fileBytes.copyOfRange(offset, offset + SALT_SIZE)
        offset += SALT_SIZE
        val iv = fileBytes.copyOfRange(offset, offset + IV_SIZE)
        offset += IV_SIZE
        val encryptedDb = fileBytes.copyOfRange(offset, fileBytes.size)
        val key = deriveKey(password, salt, ITERATIONS_V1)
        return decryptAesGcm(key, iv, encryptedDb)
    }
}
