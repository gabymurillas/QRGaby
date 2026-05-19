package com.example.qr_prueba_gaby.utils

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

/**
 * Gestor de Autenticación Biométrica.
 * Proporciona métodos para verificar la disponibilidad de sensores y lanzar el prompt.
 */
object BiometricAuthManager {

    /**
     * Verifica si el dispositivo tiene biometría configurada y disponible.
     * @return Boolean true si está disponible, false si no.
     */
    fun isBiometricAvailable(activity: FragmentActivity): Boolean {
        val biometricManager = BiometricManager.from(activity)
        return when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
            BiometricManager.BIOMETRIC_SUCCESS -> true
            else -> false
        }
    }

    /**
     * Combinación de autenticadores para la recuperación del PIN: huella o la
     * credencial del sistema (PIN/patrón/contraseña del teléfono).
     *
     * Se usa BIOMETRIC_WEAK (en vez de STRONG) porque la combinación
     * STRONG | DEVICE_CREDENTIAL no es soportada en API 28-29; WEAK | DEVICE_CREDENTIAL
     * funciona en todo el rango soportado por la app.
     */
    private const val RECOVERY_AUTHENTICATORS =
        BiometricManager.Authenticators.BIOMETRIC_WEAK or
            BiometricManager.Authenticators.DEVICE_CREDENTIAL

    /**
     * Indica si el dispositivo puede verificar al dueño con huella o con la
     * credencial del sistema (bloqueo de pantalla). Si devuelve false, el
     * teléfono no tiene ningún bloqueo configurado.
     */
    fun canVerifyOwner(activity: FragmentActivity): Boolean {
        return BiometricManager.from(activity)
            .canAuthenticate(RECOVERY_AUTHENTICATORS) == BiometricManager.BIOMETRIC_SUCCESS
    }

    /**
     * Lanza un prompt que acepta huella o la credencial del sistema
     * (PIN/patrón/contraseña del teléfono). Se usa para verificar al dueño
     * durante la recuperación del PIN de seguridad.
     */
    fun showOwnerVerificationPrompt(
        activity: FragmentActivity,
        onSuccess: () -> Unit,
        onFailed: () -> Unit
    ) {
        val executor = ContextCompat.getMainExecutor(activity)
        val biometricPrompt = BiometricPrompt(activity, executor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                onSuccess()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                onFailed()
            }
        })

        // Con DEVICE_CREDENTIAL en los autenticadores NO se debe definir botón negativo.
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Verifica tu identidad")
            .setSubtitle("Usa tu huella o el bloqueo de tu teléfono para recuperar el PIN")
            .setAllowedAuthenticators(RECOVERY_AUTHENTICATORS)
            .build()

        biometricPrompt.authenticate(promptInfo)
    }

    /**
     * Lanza el prompt de autenticación biométrica.
     */
    fun showBiometricPrompt(
        activity: FragmentActivity,
        title: String = "Autenticación Requerida",
        subtitle: String = "Usa tu huella o Face ID para abrir el portón",
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
        onCancel: () -> Unit
    ) {
        val executor = ContextCompat.getMainExecutor(activity)
        val biometricPrompt = BiometricPrompt(activity, executor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                onSuccess()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                if (errorCode == BiometricPrompt.ERROR_USER_CANCELED || errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                    onCancel()
                } else {
                    onError(errString.toString())
                }
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                // El prompt de sistema ya maneja visualmente el fallo, 
                // nosotros solo esperamos el éxito o error definitivo.
            }
        })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setNegativeButtonText("Usar PIN")
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
            .build()

        biometricPrompt.authenticate(promptInfo)
    }
}
