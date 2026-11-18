package com.illustravibes.arus

import com.illustravibes.arus.data.repository.BackupFormat
import org.junit.Assert.*
import org.junit.Test
import javax.crypto.AEADBadTagException

/**
 * Unit test format backup (JVM murni, tanpa Android).
 * Jalankan: ./gradlew :app:testDebugUnitTest
 */
class BackupFormatTest {

    private val password = "rahasia123"
    private val dbBytes = "isi-database-palsu-untuk-test".toByteArray(Charsets.UTF_8)
    private val passphrase = ByteArray(32) { (it * 7 + 3).toByte() }

    @Test
    fun `v2 round trip mengembalikan db dan passphrase yang sama`() {
        val file = BackupFormat.buildV2(password, dbBytes, passphrase)
        assertEquals(2, BackupFormat.detectVersion(file))
        val parsed = BackupFormat.parseV2(password, file)
        assertArrayEquals(dbBytes, parsed.dbBytes)
        assertArrayEquals(passphrase, parsed.dbPassphrase)
    }

    @Test
    fun `v2 kata sandi salah gagal`() {
        val file = BackupFormat.buildV2(password, dbBytes, passphrase)
        try {
            BackupFormat.parseV2("salah1234", file)
            fail("Seharusnya gagal dengan kata sandi salah")
        } catch (e: AEADBadTagException) {
            // Perilaku yang diharapkan: GCM menolak ciphertext yang tidak cocok.
        }
    }

    @Test
    fun `v2 file yang diutak-atik gagal`() {
        val file = BackupFormat.buildV2(password, dbBytes, passphrase)
        file[file.size - 1] = (file[file.size - 1].toInt() xor 0xFF).toByte()
        try {
            BackupFormat.parseV2(password, file)
            fail("Seharusnya gagal bila file diutak-atik")
        } catch (e: AEADBadTagException) {
            // Perilaku yang diharapkan: tag GCM tidak cocok.
        }
    }

    @Test
    fun `v1 masih dikenali dan bisa diuraikan`() {
        val salt = BackupFormat.randomBytes(BackupFormat.SALT_SIZE)
        val key = BackupFormat.deriveKey(password, salt, BackupFormat.ITERATIONS_V1)
        val iv = BackupFormat.randomBytes(BackupFormat.IV_SIZE)
        val encrypted = BackupFormat.encryptAesGcm(key, iv, dbBytes)
        val file = BackupFormat.HEADER_V1.toByteArray(Charsets.UTF_8) + salt + iv + encrypted
        assertEquals(1, BackupFormat.detectVersion(file))
        assertArrayEquals(dbBytes, BackupFormat.parseV1(password, file))
    }

    @Test
    fun `header tidak dikenal ditolak`() {
        assertNull(BackupFormat.detectVersion("bukan-backup".toByteArray(Charsets.UTF_8)))
        assertNull(BackupFormat.detectVersion(ByteArray(0)))
    }

    @Test
    fun `kata sandi kurang dari 4 karakter ditolak`() {
        try {
            BackupFormat.buildV2("abc", dbBytes, passphrase)
            fail("Seharusnya ditolak")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message!!.contains("4 karakter"))
        }
    }
}
