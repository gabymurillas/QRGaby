package com.example.qr_prueba_gaby.presentation.ui.QrSyncScreen

import android.graphics.Bitmap
import android.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.qr_prueba_gaby.data.pref.UserDataStore
import com.example.qr_prueba_gaby.utils.CryptoManager
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject
import javax.inject.Inject

/** Token de seguridad que debe traer el QR generado por la app Admin. */
private const val EXPECTED_TOKEN = "ALCARAVAN_2025"

@HiltViewModel
class SyncViewModel @Inject constructor(
    private val dataStore: UserDataStore
) : ViewModel() {

    // ── Estado del QR del usuario ─────────────────────────────────────────────
    private val _qrBitmap = MutableStateFlow<Bitmap?>(null)
    val qrBitmap: StateFlow<Bitmap?> = _qrBitmap.asStateFlow()

    // ── ID técnico visible ───────────────────────────────────────────────────
    private val _decryptedAndroidId = MutableStateFlow<String?>(null)
    val decryptedAndroidId: StateFlow<String?> = _decryptedAndroidId.asStateFlow()

    private val _idVisibilityProgress = MutableStateFlow(0f)
    val idVisibilityProgress: StateFlow<Float> = _idVisibilityProgress.asStateFlow()

    private var idVisibilityJob: Job? = null

    // ── Estado del escaneo del QR del Administrador ──────────────────────────
    /**
     * null  → sin resultado todavía
     * true  → QR válido procesado correctamente
     * false → QR inválido (token incorrecto, campos vacíos o JSON malformado)
     */
    private val _provisioningSuccess = MutableStateFlow<Boolean?>(null)
    val provisioningSuccess: StateFlow<Boolean?> = _provisioningSuccess.asStateFlow()

    /** Mensaje descriptivo del resultado del escaneo. */
    private val _provisioningMessage = MutableStateFlow<String?>(null)
    val provisioningMessage: StateFlow<String?> = _provisioningMessage.asStateFlow()

    // ── DataStore flows ──────────────────────────────────────────────────────
    val userDataFlow = dataStore.userDataFlow
    val isActivatedFlow: StateFlow<Boolean?> = dataStore.isActivatedFlow
        .stateIn(viewModelScope, SharingStarted.Companion.WhileSubscribed(5000), null)

    init {
        generateQrFromStoredData()
    }

    // ────────────────────────────────────────────────────────────────────────
    // Provisionamiento vía QR del Administrador
    // ────────────────────────────────────────────────────────────────────────

    /**
     * Procesa el contenido en bruto del QR escaneado desde la app Admin.
     *
     * Protocolo esperado (JSON):
     * ```json
     * {
     *   "endpoint" : "http://172.17.12.119:8059",
     *   "token"    : "ALCARAVAN_2025"
     * }
     * ```
     * Si el JSON es válido y el token correcto, guarda el endpoint en el
     * DataStore y activa la llave digital del usuario.
     */
    fun processProvisioningQr(rawJson: String) {
        viewModelScope.launch {
            _provisioningMessage.value = null
            _provisioningSuccess.value = null

            try {
                val json     = JSONObject(rawJson)
                val endpoint = json.getString("endpoint")
                val token    = json.getString("token")

                if (token != EXPECTED_TOKEN) {
                    _provisioningSuccess.value  = false
                    _provisioningMessage.value  = "QR inválido: token de seguridad incorrecto."
                    return@launch
                }

                if (endpoint.isBlank()) {
                    _provisioningSuccess.value  = false
                    _provisioningMessage.value  = "QR inválido: el endpoint está vacío."
                    return@launch
                }

                // Guardar el endpoint y activar la llave digital
                dataStore.saveProvisioningData(endpoint = endpoint)
                dataStore.setActivated(true)
                dataStore.setRegistered(true)

                _provisioningSuccess.value  = true
                _provisioningMessage.value  = "¡Configuración recibida! Acceso activado."

            } catch (e: JSONException) {
                _provisioningSuccess.value  = false
                _provisioningMessage.value  = "El QR no contiene datos válidos."
            } catch (e: Exception) {
                _provisioningSuccess.value  = false
                _provisioningMessage.value  = "Error inesperado: ${e.localizedMessage}"
            }
        }
    }

    /** Reinicia el estado del escaneo para permitir un nuevo intento. */
    fun resetProvisioningState() {
        _provisioningSuccess.value = null
        _provisioningMessage.value = null
    }

    // ────────────────────────────────────────────────────────────────────────
    // Generación del QR de identificación del conductor
    // ────────────────────────────────────────────────────────────────────────

    private fun generateQrFromStoredData() {
        viewModelScope.launch {
            userDataFlow.collect { user ->
                user?.let {
                    val bitmap = withContext(Dispatchers.Default) {
                        generateQrBitmap(it.toJson(), 512)
                    }
                    _qrBitmap.value = bitmap
                }
            }
        }
    }

    // ────────────────────────────────────────────────────────────────────────
    // ID técnico visible (sin cambios)
    // ────────────────────────────────────────────────────────────────────────

    fun showDecryptedId(encryptedId: String) {
        if (_decryptedAndroidId.value != null) {
            idVisibilityJob?.cancel()
            _decryptedAndroidId.value = null
            _idVisibilityProgress.value = 0f
            return
        }

        idVisibilityJob = viewModelScope.launch {
            val decrypted = CryptoManager.decrypt(encryptedId)
            _decryptedAndroidId.value = decrypted

            val durationMs = 30_000L
            val intervalMs = 100L
            val steps = (durationMs / intervalMs).toInt()

            for (i in steps downTo 0) {
                _idVisibilityProgress.value = i.toFloat() / steps
                delay(intervalMs)
            }
            _decryptedAndroidId.value = null
            _idVisibilityProgress.value = 0f
        }
    }

    // ────────────────────────────────────────────────────────────────────────
    // Helpers
    // ────────────────────────────────────────────────────────────────────────

    private fun generateQrBitmap(text: String, size: Int): Bitmap {
        val bitMatrix = MultiFormatWriter().encode(text, BarcodeFormat.QR_CODE, size, size)
        val pixels = IntArray(size * size)
        for (y in 0 until size) {
            for (x in 0 until size) {
                pixels[y * size + x] = if (bitMatrix.get(x, y)) Color.BLACK else Color.WHITE
            }
        }
        return Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
            .apply { setPixels(pixels, 0, size, 0, 0, size, size) }
    }
}