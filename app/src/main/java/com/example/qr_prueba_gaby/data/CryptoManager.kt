package com.example.qr_prueba_gaby.data

import android.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * Gestiona la encriptación y desencriptación del ANDROID_ID usando una Llave Compartida.
 *
 * IMPORTANTE: La constante SHARED_SECRET debe ser EXACTAMENTE la misma en la App del Administrador.
 * Se utiliza AES-256-GCM para garantizar seguridad e integridad de los datos.
 */
object CryptoManager {

    // Llave de 32 caracteres (256 bits). Cámbiala por una propia y mantenla secreta.
    private const val SHARED_SECRET = "GabyQrSecureKey12345678901234567" 
    private const val TRANSFORMATION = "AES/GCM/NoPadding"
    private const val GCM_TAG_LENGTH = 128
    private const val IV_SIZE = 12

    /**
     * Encripta un texto plano usando la llave compartida.
     * @return String Base64 con formato IV_BASE64:CIPHERTEXT_BASE64
     */
    fun encrypt(plainText: String): String {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        val keySpec = SecretKeySpec(SHARED_SECRET.toByteArray(Charsets.UTF_8), "AES")
        
        cipher.init(Cipher.ENCRYPT_MODE, keySpec)
        val iv = cipher.iv
        val encrypted = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
        
        val ivBase64 = Base64.encodeToString(iv, Base64.NO_WRAP)
        val encBase64 = Base64.encodeToString(encrypted, Base64.NO_WRAP)
        
        return "$ivBase64:$encBase64"
    }

    /**
     * Desencripta un texto encriptado con la misma llave compartida.
     * @param encryptedText Formato esperado "IV_BASE64:CIPHERTEXT_BASE64"
     * @return String con el texto original, o null si falla.
     */
    fun decrypt(encryptedText: String): String? {
        return try {
            val parts = encryptedText.split(":")
            if (parts.size != 2) return null
            
            val iv = Base64.decode(parts[0], Base64.NO_WRAP)
            val ciphertext = Base64.decode(parts[1], Base64.NO_WRAP)
            
            val keySpec = SecretKeySpec(SHARED_SECRET.toByteArray(Charsets.UTF_8), "AES")
            val cipher = Cipher.getInstance(TRANSFORMATION)
            val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
            
            cipher.init(Cipher.DECRYPT_MODE, keySpec, spec)
            String(cipher.doFinal(ciphertext), Charsets.UTF_8)
        } catch (e: Exception) {
            null
        }
    }
}
