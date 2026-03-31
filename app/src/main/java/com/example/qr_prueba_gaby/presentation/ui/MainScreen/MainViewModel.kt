package com.example.qr_prueba_gaby.presentation.ui.MainScreen

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.qr_prueba_gaby.data.model.UserData
import com.example.qr_prueba_gaby.data.network.service.ApiService
import com.example.qr_prueba_gaby.data.network.service.GateOpenRequest
import com.example.qr_prueba_gaby.data.network.service.OdooRequest
import com.example.qr_prueba_gaby.data.pref.UserDataStore
import com.example.qr_prueba_gaby.data.repository.GateRepository
import com.example.qr_prueba_gaby.data.repository.GateResult
import com.example.qr_prueba_gaby.presentation.ui.common.BleState
import com.example.qr_prueba_gaby.presentation.ui.common.OdooStatus
import com.example.qr_prueba_gaby.utils.CryptoManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val dataStore: UserDataStore,
    private val apiService: ApiService,
    private val gateRepository: GateRepository
) : ViewModel() {

    private val _bleState = MutableStateFlow(BleState.DISCONNECTED)
    val bleState: StateFlow<BleState> = _bleState.asStateFlow()

    private val _rssi = MutableStateFlow(-100)
    val rssi: StateFlow<Int> = _rssi.asStateFlow()

    private val _odooStatus = MutableStateFlow(OdooStatus.VERIFYING)
    val odooStatus: StateFlow<OdooStatus> = _odooStatus.asStateFlow()

    private val _gateMessage = MutableStateFlow<String?>(null)
    val gateMessage: StateFlow<String?> = _gateMessage.asStateFlow()

    private val _decryptedAndroidId = MutableStateFlow<String?>(null)
    val decryptedAndroidId: StateFlow<String?> = _decryptedAndroidId.asStateFlow()

    private val _idVisibilityProgress = MutableStateFlow(0f)
    val idVisibilityProgress: StateFlow<Float> = _idVisibilityProgress.asStateFlow()

    private val _unauthorizedEvent = MutableSharedFlow<Unit>()
    val unauthorizedEvent = _unauthorizedEvent.asSharedFlow()

    // ── Estados de Seguridad (PIN y Diálogos) ──
    private val _isPinEntryVisible = MutableStateFlow(false)
    val isPinEntryVisible = _isPinEntryVisible.asStateFlow()

    private val _isPinSetupVisible = MutableStateFlow(false)
    val isPinSetupVisible = _isPinSetupVisible.asStateFlow()

    private val _pinError = MutableStateFlow<String?>(null)
    val pinError = _pinError.asStateFlow()

    private val _authRequestTrigger = MutableSharedFlow<Unit>()
    val authRequestTrigger = _authRequestTrigger.asSharedFlow()

    val userDataFlow = dataStore.userDataFlow
    val userPinFlow = dataStore.userPinFlow

    private var proximityJob: Job? = null
    private var idVisibilityJob: Job? = null
    private var isOperationInProgress = false

    init {
        checkOdooStatus()
        startDeviceDetection()
    }

    private fun checkOdooStatus() {
        viewModelScope.launch {
            while (true) {
                try {
                    val user = userDataFlow.first()
                    if (user != null) {
                        // RECOMENDACIÓN: Solo mostrar amarillo si no estamos ya en VALID
                        if (_odooStatus.value != OdooStatus.VALID) {
                            _odooStatus.value = OdooStatus.VERIFYING
                        }
                        
                        val response = apiService.syncVehicular(user.c)

                        if (response.isSuccessful && response.body()?.result?.status == "success") {
                            _odooStatus.value = OdooStatus.VALID
                        } else {
                            _odooStatus.value = OdooStatus.INVALID
                            logout {
                                viewModelScope.launch { _unauthorizedEvent.emit(Unit) }
                            }
                            return@launch
                        }
                    }
                } catch (e: Exception) {
                    _odooStatus.value = OdooStatus.OFFLINE
                }
                delay(10000)
            }
        }
    }

    private fun startDeviceDetection() {
        proximityJob?.cancel()
        proximityJob = viewModelScope.launch {
            while (true) {
                if (!isOperationInProgress) {
                    val isAvailable = gateRepository.checkAvailability()
                    _rssi.value = if (isAvailable) -50 else -100
                }
                delay(2000)
            }
        }
    }

    /** 
     * Inicia el flujo de apertura solicitando biometría primero.
     * Es llamado desde el botón "Abrir" de la UI.
     */
    fun startAuthFlow() {
        if (_bleState.value == BleState.CONNECTING || isOperationInProgress) return
        
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
     * Se llama si la biometría falla o se elige PIN.
     */
    fun onBiometricFailureOrPIN() {
        viewModelScope.launch {
            val hasPin = userPinFlow.first() != null
            if (hasPin) {
                _isPinEntryVisible.value = true
            } else {
                _isPinSetupVisible.value = true
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
     * Guarda el PIN por primera vez.
     */
    fun setupPin(newPin: String) {
        viewModelScope.launch {
            dataStore.savePin(newPin)
            _isPinSetupVisible.value = false
            openGate()
        }
    }

    fun closePinDialogs() {
        _isPinEntryVisible.value = false
        _isPinSetupVisible.value = false
        _pinError.value = null
    }

    private fun openGate() {
        if (_bleState.value == BleState.CONNECTING || isOperationInProgress) return
        _bleState.value = BleState.CONNECTING
        isOperationInProgress = true

        viewModelScope.launch {
            try {
                val user = dataStore.userDataFlow.stateIn(this).value ?: throw Exception("Usuario no encontrado")
                val idToSend = CryptoManager.decrypt(user.aid) ?: throw Exception("Error al desencriptar ID")

                val result = gateRepository.openGate(idToSend)

                if (result is GateResult.Success) {
                    _bleState.value = BleState.SENT
                    _gateMessage.value = "¡Acceso Concedido!"

                    viewModelScope.launch {
                        try {
                            val timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                            val request = GateOpenRequest(fecha_hora = timestamp)
                            apiService.logGateOpen(OdooRequest(request))
                        } catch (apiError: Exception) {
                            Log.e("MainViewModel", "Error al registrar apertura: ${apiError.message}")
                        }
                    }
                    delay(1000)
                } else if (result is GateResult.Error) {
                    throw Exception(result.message)
                }
            } catch (e: Exception) {
                _bleState.value = BleState.ERROR
                _gateMessage.value = e.message
                delay(2000)
            } finally {
                _bleState.value = BleState.DISCONNECTED
                isOperationInProgress = false
                val isAvailable = gateRepository.checkAvailability()
                _rssi.value = if (isAvailable) -50 else -100
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