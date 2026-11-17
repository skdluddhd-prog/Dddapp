package com.example.engine

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Hardware-backed (Android Keystore) AES-256-GCM encryption for stored API keys.
 * - Old plain-text data is read transparently and re-saved encrypted on the next save.
 * - If the Keystore is unavailable, data is stored as before so nothing is ever lost.
 * Uses only the Android framework: no extra library, no extra RAM, a few microseconds per call.
 */
object SecureKeyVault {
    private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    private const val ALIAS = "skrl2_api_key_vault_v1"
    private const val PREFIX = "enc1:"
    private const val TRANSFORMATION = "AES/GCM/NoPadding"
    private const val IV_BYTES = 12
    private const val TAG_BITS = 128

    private fun secretKey(): SecretKey {
        val ks = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        (ks.getKey(ALIAS, null) as? SecretKey)?.let { return it }
        val gen = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
        gen.init(
            KeyGenParameterSpec.Builder(
                ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build()
        )
        return gen.generateKey()
    }

    /** Returns "enc1:<base64(iv+cipher)>", or the original text if encryption is impossible. */
    fun encrypt(plain: String): String {
        return try {
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, secretKey())
            val iv = cipher.iv
            val out = cipher.doFinal(plain.toByteArray(Charsets.UTF_8))
            PREFIX + Base64.encodeToString(iv + out, Base64.NO_WRAP)
        } catch (_: Throwable) {
            plain
        }
    }

    /** Decrypts encrypted text, passes old plain text through, and returns null if unreadable. */
    fun decryptOrPlain(stored: String?): String? {
        if (stored.isNullOrEmpty()) return stored
        if (!stored.startsWith(PREFIX)) return stored
        return try {
            val data = Base64.decode(stored.removePrefix(PREFIX), Base64.NO_WRAP)
            val iv = data.copyOfRange(0, IV_BYTES)
            val body = data.copyOfRange(IV_BYTES, data.size)
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.DECRYPT_MODE, secretKey(), GCMParameterSpec(TAG_BITS, iv))
            String(cipher.doFinal(body), Charsets.UTF_8)
        } catch (_: Throwable) {
            null
        }
    }
}
