package com.illustravibes.arus.core.database

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

object DatabaseKeyManager {

    private const val KEYSTORE_ALIAS = "arus_db_master_key"
    private const val PREFS_NAME = "arus_sec_prefs"
    private const val KEY_ENCRYPTED_PASSPHRASE = "encrypted_db_passphrase"
    private const val KEY_GCM_IV = "gcm_iv"

    @Synchronized
    fun getOrCreateDatabasePassphrase(context: Context): ByteArray {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val encryptedPassphraseBase64 = prefs.getString(KEY_ENCRYPTED_PASSPHRASE, null)
        val ivBase64 = prefs.getString(KEY_GCM_IV, null)

        if (encryptedPassphraseBase64 != null && ivBase64 != null) {
            val encryptedBytes = android.util.Base64.decode(encryptedPassphraseBase64, android.util.Base64.DEFAULT)
            val iv = android.util.Base64.decode(ivBase64, android.util.Base64.DEFAULT)
            return decryptPassphrase(encryptedBytes, iv)
        }

        // Generate a new 32-byte (256-bit) random passphrase for SQLCipher
        val newPassphrase = ByteArray(32)
        SecureRandom().nextBytes(newPassphrase)

        val (encryptedBytes, iv) = encryptPassphrase(newPassphrase)

        prefs.edit()
            .putString(KEY_ENCRYPTED_PASSPHRASE, android.util.Base64.encodeToString(encryptedBytes, android.util.Base64.DEFAULT))
            .putString(KEY_GCM_IV, android.util.Base64.encodeToString(iv, android.util.Base64.DEFAULT))
            .apply()

        return newPassphrase
    }

    /**
     * Simpan passphrase database yang sudah ada (mis. dari file backup V2)
     * supaya dipakai perangkat ini. Passphrase dienkripsi dengan kunci
     * Keystore perangkat ini, sama seperti saat pertama dibuat.
     */
    @Synchronized
    fun storeDatabasePassphrase(context: Context, passphrase: ByteArray) {
        require(passphrase.size == 32) { "Passphrase database harus 32 byte" }
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val (encryptedBytes, iv) = encryptPassphrase(passphrase)
        prefs.edit()
            .putString(KEY_ENCRYPTED_PASSPHRASE, android.util.Base64.encodeToString(encryptedBytes, android.util.Base64.DEFAULT))
            .putString(KEY_GCM_IV, android.util.Base64.encodeToString(iv, android.util.Base64.DEFAULT))
            .apply()
    }

    private fun getOrCreateMasterKey(): SecretKey {
        val keyStore = KeyStore.getInstance("AndroidKeyStore")
        keyStore.load(null)

        if (keyStore.containsAlias(KEYSTORE_ALIAS)) {
            val entry = keyStore.getEntry(KEYSTORE_ALIAS, null) as KeyStore.SecretKeyEntry
            return entry.secretKey
        }

        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            "AndroidKeyStore"
        )

        val spec = KeyGenParameterSpec.Builder(
            KEYSTORE_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .build()

        keyGenerator.init(spec)
        return keyGenerator.generateKey()
    }

    private fun encryptPassphrase(passphrase: ByteArray): Pair<ByteArray, ByteArray> {
        val secretKey = getOrCreateMasterKey()
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        val encryptedBytes = cipher.doFinal(passphrase)
        val iv = cipher.iv
        return Pair(encryptedBytes, iv)
    }

    private fun decryptPassphrase(encryptedBytes: ByteArray, iv: ByteArray): ByteArray {
        val secretKey = getOrCreateMasterKey()
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val spec = GCMParameterSpec(128, iv)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)
        return cipher.doFinal(encryptedBytes)
    }
}
