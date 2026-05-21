package com.example.qr_prueba_gaby.presentation.ui.MainScreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.qr_prueba_gaby.data.network.service.ApiService
import com.example.qr_prueba_gaby.data.network.service.ControlAccesoParams
import com.example.qr_prueba_gaby.data.network.service.GateOpenParams
import com.example.qr_prueba_gaby.data.network.service.OdooRequest
import com.example.qr_prueba_gaby.data.model.ThemeMode
import com.example.qr_prueba_gaby.data.pref.UserDataStore
import com.example.qr_prueba_gaby.presentation.ui.common.GateState
import com.example.qr_prueba_gaby.presentation.ui.common.OdooStatus
import com.example.qr_prueba_gaby.utils.CryptoManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import org.json.JSONException
import org.json.JSONObject
import javax.inject.Inject

/** Token de seguridad que debe traer el QR generado por la app Admin. */
private const val EXPECTED_TOKEN = "ALCARAVAN_2025"

@HiltViewModel
class MainViewModel @Inject constructor(
    private val dataStore: UserDataStore,
    private val apiService: ApiService
) : ViewModel() {

    private val _gateState = MutableStateFlow(GateState.IDLE)
    val gateState: StateFlow<GateState> = _gateState.asStateFlow()


    private val _odooStatus = MutableStateFlow(OdooStatus.VERIFYING)
    val odooStatus: StateFlow<OdooStatus> = _odooStatus.asStateFlow()

    private val _gateMessage = MutableStateFlow<String?>(null)
    val gateMessage: StateFlow<String?> = _gateMessage.asStateFlow()

    private val _decryptedAndroidId = MutableStateFlow<String?>(null)
    val decryptedAndroidId: StateFlow<String?> = _decryptedAndroidId.asStateFlow()

    private val _idVisibilityProgress = MutableStateFlow(0f)
    val idVisibilityProgress: StateFlow<Float> = _idVisibilityProgress.asStateFlow()

    // ── Estado del re-escaneo del QR del Administrador (cambio de IP) ──
    private val _reprovisionSuccess = MutableStateFlow<Boolean?>(null)
    val reprovisionSuccess: StateFlow<Boolean?> = _reprovisionSuccess.asStateFlow()

    private val _reprovisionMessage = MutableStateFlow<String?>(null)
    val reprovisionMessage: StateFlow<String?> = _reprovisionMessage.asStateFlow()

    // ── Estados de Seguridad (PIN y Diálogos) ──
    private val _isPinEntryVisible = MutableStateFlow(false)
    val isPinEntryVisible = _isPinEntryVisible.asStateFlow()

    private val _isPinSetupVisible = MutableStateFlow(false)
    val isPinSetupVisible = _isPinSetupVisible.asStateFlow()

    private val _pinError = MutableStateFlow<String?>(null)
    val pinError = _pinError.asStateFlow()

    private val _authRequestTrigger = MutableSharedFlow<Unit>()
    val authRequestTrigger = _authRequestTrigger.asSharedFlow()

    /** Evento para lanzar la verificación de identidad en la recuperación del PIN. */
    private val _pinRecoveryTrigger = MutableSharedFlow<Unit>()
    val pinRecoveryTrigger = _pinRecoveryTrigger.asSharedFlow()

    /** Visibilidad del modal de ajustes de seguridad. */
    private val _isSettingsVisible = MutableStateFlow(false)
    val isSettingsVisible = _isSettingsVisible.asStateFlow()

    val userDataFlow = dataStore.userDataFlow
    val userPinFlow = dataStore.userPinFlow

    /** Modo de tema persistido (Sistema / Claro / Oscuro). */
    val themeMode: StateFlow<ThemeMode> = dataStore.themeModeFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ThemeMode.SYSTEM)

    private var idVisibilityJob: Job? = null
    private var isOperationInProgress = false

    init {
        checkOdooStatus()
    }

    /**
     * Verifica periódicamente el estado del conductor contra Odoo.
     *
     * Es SOLO un indicador visual: nunca cierra sesión ni borra el registro.
     * El registro del conductor es único y persistente; el único logout es
     * el manual desde el botón de la UI.
     */
    private fun checkOdooStatus() {
        viewModelScope.launch {
            while (true) {
                try {
                    val user = userDataFlow.first()
                    if (user != null) {
                        // Solo mostrar "verificando" si no estamos ya en VALID
                        if (_odooStatus.value != OdooStatus.VALID) {
                            _odooStatus.value = OdooStatus.VERIFYING
                        }

                        val response = apiService.syncVehicular(
                            OdooRequest(ControlAccesoParams(action = "read", cedula = user.c))
                        )

                        _odooStatus.value =
                            if (response.isSuccessful && response.body()?.result?.status == "success") {
                                OdooStatus.VALID
                            } else {
                                OdooStatus.INVALID
                            }
                    }
                } catch (e: Exception) {
                    _odooStatus.value = OdooStatus.OFFLINE
                }
                delay(10000)
            }
        }
    }


    /** 
     * Inicia el flujo de apertura solicitando biometría primero.
     * Es llamado desde el botón "Abrir" de la UI.
     */
    fun startAuthFlow() {
        if (_gateState.value == GateState.REQUESTING || isOperationInProgress) return
        
        viewModelScope.launch {
            // Enviamos un evento para que el MainScreen (Activity) lance BiometricPrompt
            _authRequestTrigger.emit(Unit)
        }
    }

    /** 
     * Se llama cuando la biometría es exitosa. 
     */
    fun onAuthSuccess() {
        openGate()
    }

    /**
     * El dispositivo NO tiene biometría configurada.
     * - Con PIN activado    → se pide el PIN.
     * - Con PIN desactivado → apertura directa (no hay segundo factor).
     */
    fun onBiometricUnavailable() {
        viewModelScope.launch {
            if (userPinFlow.first() != null) {
                _isPinEntryVisible.value = true
            } else {
                openGate()
            }
        }
    }

    /**
     * La biometría existía pero falló o el usuario la canceló.
     * - Con PIN activado    → se pide el PIN como alternativa.
     * - Con PIN desactivado → no se abre (el usuario canceló la huella).
     */
    fun onBiometricFailedOrCancelled() {
        viewModelScope.launch {
            if (userPinFlow.first() != null) {
                _isPinEntryVisible.value = true
            }
        }
    }

    /**
     * Valida el PIN ingresado.
     */
    fun validatePin(enteredPin: String) {
        viewModelScope.launch {
            val storedPin = userPinFlow.first()
            if (enteredPin == storedPin) {
                _isPinEntryVisible.value = false
                _pinError.value = null
                openGate()
            } else {
                _pinError.value = "PIN Incorrecto"
            }
        }
    }

    /**
     * Guarda el PIN definido por el usuario (al activarlo, cambiarlo o
     * recuperarlo). No abre el portón: el PIN ya no es parte forzada del flujo.
     */
    fun setupPin(newPin: String) {
        viewModelScope.launch {
            dataStore.savePin(newPin)
            _isPinSetupVisible.value = false
        }
    }

    fun closePinDialogs() {
        _isPinEntryVisible.value = false
        _isPinSetupVisible.value = false
        _pinError.value = null
    }

    // ── Ajustes de seguridad: PIN opcional ───────────────────────────────────

    fun openSettings()  { _isSettingsVisible.value = true }
    fun closeSettings() { _isSettingsVisible.value = false }

    /** Cambia el modo de tema de la app y lo persiste. */
    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { dataStore.saveThemeMode(mode) }
    }

    /**
     * Activa o desactiva el PIN de seguridad.
     * - Activar    → abre el diálogo para que el usuario cree su PIN.
     * - Desactivar → elimina el PIN guardado.
     */
    fun setPinEnabled(enabled: Boolean) {
        if (enabled) {
            _isPinSetupVisible.value = true
        } else {
            viewModelScope.launch { dataStore.clearPin() }
        }
    }

    /** Abre el diálogo para cambiar el PIN actual. */
    fun requestChangePin() {
        _isPinSetupVisible.value = true
    }

    // ── Recuperación del PIN olvidado ────────────────────────────────────────

    /** Inicia la recuperación del PIN: dispara la verificación de identidad. */
    fun startPinRecovery() {
        viewModelScope.launch { _pinRecoveryTrigger.emit(Unit) }
    }

    /**
     * Se llama cuando la identidad del dueño quedó verificada (huella o
     * credencial del teléfono), o cuando el teléfono no tiene ningún bloqueo
     * configurado. Permite establecer un PIN nuevo.
     */
    fun onPinRecoveryVerified() {
        _isPinEntryVisible.value = false
        _pinError.value = null
        _isPinSetupVisible.value = true
    }

    /**
     * Re-aplica el QR de aprovisionamiento del Administrador.
     *
     * Se usa cuando cambia la IP/endpoint del servidor Odoo: sobrescribe el
     * endpoint guardado SIN tocar el estado de registro ni de activación,
     * de modo que el registro del conductor sigue siendo único y persistente.
     */
    fun reprovision(rawJson: String) {
        viewModelScope.launch {
            _reprovisionSuccess.value = null
            _reprovisionMessage.value = null
            try {
                val json     = JSONObject(rawJson)
                val endpoint = json.getString("endpoint")
                val token    = json.optString("token", "")

                when {
                    token != EXPECTED_TOKEN -> {
                        _reprovisionSuccess.value = false
                        _reprovisionMessage.value = "QR inválido: token de seguridad incorrecto."
                    }
                    endpoint.isBlank() -> {
                        _reprovisionSuccess.value = false
                        _reprovisionMessage.value = "QR inválido: el endpoint está vacío."
                    }
                    else -> {
                        dataStore.saveProvisioningData(endpoint = endpoint)
                        _reprovisionSuccess.value = true
                        _reprovisionMessage.value = "Servidor actualizado correctamente."
                    }
                }
            } catch (e: JSONException) {
                _reprovisionSuccess.value = false
                _reprovisionMessage.value = "El QR no contiene datos válidos."
            } catch (e: Exception) {
                _reprovisionSuccess.value = false
                _reprovisionMessage.value = "Error inesperado: ${e.localizedMessage}"
            }
        }
    }

    /** Reinicia el estado del re-escaneo para permitir un nuevo intento. */
    fun resetReprovisionState() {
        _reprovisionSuccess.value = null
        _reprovisionMessage.value = null
    }

    /**
     * Solicita la apertura remota del portón al servidor Odoo.
     *
     * En V6 la app ya no se comunica con el ESP32: envía la cédula del conductor
     * a Odoo, que valida la autorización y dispara el relé del ESP32.
     */
    private fun openGate() {
        if (_gateState.value == GateState.REQUESTING || isOperationInProgress) return
        _gateState.value = GateState.REQUESTING
        isOperationInProgress = true

        viewModelScope.launch {
            try {
                val user = dataStore.userDataFlow.first() ?: throw Exception("Usuario no encontrado")

                val response = withTimeoutOrNull(10_000) {
                    apiService.requestGateOpen(OdooRequest(GateOpenParams(cedula = user.c)))
                }

                when {
                    response == null -> {
                        _gateState.value = GateState.ERROR
                        _gateMessage.value = "El servidor tardó demasiado, intenta de nuevo"
                    }
                    response.isSuccessful && response.body()?.result?.status == "success" -> {
                        _gateState.value = GateState.OPENED
                        _gateMessage.value = "¡Acceso Concedido!"
                    }
                    else -> {
                        _gateState.value = GateState.ERROR
                        _gateMessage.value = response.body()?.result?.message
                            ?: response.body()?.error?.message
                            ?: "Acceso denegado por el servidor"
                    }
                }
                delay(1500)
            } catch (e: Exception) {
                _gateState.value = GateState.ERROR
                _gateMessage.value = "Error de conexión: ${e.localizedMessage}"
                delay(2000)
            } finally {
                _gateState.value = GateState.IDLE
                isOperationInProgress = false
            }
        }
    }

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

    fun logout(onDone: () -> Unit) {
        viewModelScope.launch {
            dataStore.clearAll()
            onDone()
        }
    }

    fun clearGateMessage() { _gateMessage.value = null }
}