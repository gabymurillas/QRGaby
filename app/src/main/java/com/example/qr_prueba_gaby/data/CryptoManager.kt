package com.example.qr_prueba_gaby.data

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Gestiona la encriptación y desencriptación del ANDROID_ID usando Android Keystore.
 *
 * - La llave AES-256-GCM vive EXCLUSIVAMENTE dentro del Android Keystore.
 * - Nunca se expone, serializa ni puede ser extraída del dispositivo.
 * - Usa GCM para autenticación del ciphertext (integridad garantizada).
 */
object CryptoManager {

    private const val KEY_ALIAS = "QRGabyKey"
    private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    private const val TRANSFORMATION = "AES/GCM/NoPadding"
    private const val GCM_TAG_LENGTH = 128

    /**
     * Encripta un texto plano.
     * @return String Base64 con formato IV_BASE64:CIPHERTEXT_BASE64
     */
    fun encrypt(plainText: String): String {
        val key = getOrCreateKey()
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, key)
        val iv = cipher.iv
        val encrypted = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
        val ivBase64 = Base64.encodeToString(iv, Base64.NO_WRAP)
        val encBase64 = Base64.encodeToString(encrypted, Base64.NO_WRAP)
        return "$ivBase64:$encBase64"
    }

    /**
     * Desencripta un texto encriptado previamente con [encrypt].
     * @return String con el texto original, o null si falla.
     */
    fun decrypt(encryptedText: String): String? {
        return try {
            val parts = encryptedText.split(":")
            if (parts.size != 2) return null
            val iv = Base64.decode(parts[0], Base64.NO_WRAP)
            val ciphertext = Base64.decode(parts[1], Base64.NO_WRAP)
            val key = getOrCreateKey()
            val cipher = Cipher.getInstance(TRANSFORMATION)
            val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
            cipher.init(Cipher.DECRYPT_MODE, key, spec)
            String(cipher.doFinal(ciphertext), Charsets.UTF_8)
        } catch (e: Exception) { null }
    }

    private fun getOrCreateKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }

        // Si la llave ya existe, la retorna directamente
        keyStore.getKey(KEY_ALIAS, null)?.let { return it as SecretKey }

        // Si no existe, la genera dentro del Keystore (nunca sale del hardware)
        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            ANDROID_KEYSTORE
        )
        val spec = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .build()

        keyGenerator.init(spec)
        return keyGenerator.generateKey()
    }
}
