package com.example.qr_prueba_gaby.presentation.ui.viewmodels

import com.example.qr_prueba_gaby.data.network.service.ApiService
import com.example.qr_prueba_gaby.data.network.service.GateOpenRequest
import com.example.qr_prueba_gaby.data.network.service.OdooRequest
import com.example.qr_prueba_gaby.data.pref.UserDataStore
import com.example.qr_prueba_gaby.data.repository.GateRepository
import com.example.qr_prueba_gaby.data.repository.GateResult
import com.example.qr_prueba_gaby.utils.CryptoManager
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

    val userDataFlow = dataStore.userDataFlow
    
    private var proximityJob: Job? = null
    private var idVisibilityJob: Job? = null

    init {
        checkOdooStatus()
        startProximitySimulation()
    }

    private fun checkOdooStatus() {
        viewModelScope.launch {
            while (true) {
                try {
                    val user = userDataFlow.first()
                    if (user != null) {
                        _odooStatus.value = OdooStatus.VERIFYING
                        val response = apiService.syncVehicular(user.c)
                        
                        if (response.isSuccessful && response.body()?.result?.status == "success") {
                            _odooStatus.value = OdooStatus.VALID
                        } else {
                            _odooStatus.value = OdooStatus.INVALID
                            // Si no es válido o no se encuentra, desloguear automáticamente
                            logout { 
                                viewModelScope.launch { _unauthorizedEvent.emit(Unit) }
                            }
                            return@launch // Detener el ciclo si el usuario es inválido
                        }
                    }
                } catch (e: Exception) {
                    _odooStatus.value = OdooStatus.OFFLINE
                }
                delay(10000) // Refresco cada 10 segundos
            }
        }
    }

    private fun startProximitySimulation() {
        proximityJob?.cancel()
        proximityJob = viewModelScope.launch {
            while (true) {
                val mockRssi = (-80..-45).random()
                _rssi.value = mockRssi
                delay(1500)
            }
        }
    }

    fun openGate() {
        if (_bleState.value == BleState.CONNECTING) return
        _bleState.value = BleState.CONNECTING

        viewModelScope.launch {
            try {
                val user = dataStore.userDataFlow.stateIn(this).value ?: throw Exception("Usuario no encontrado")
                val idToSend = CryptoManager.decrypt(user.aid) ?: throw Exception("Error al desencriptar ID")
                
                // Llamada al repositorio (ya corre en IO interiormente)
                val result = gateRepository.openGate(idToSend)

                if (result is GateResult.Success) {
                    _bleState.value = BleState.SENT
                    _gateMessage.value = "¡Acceso Concedido!"
                    
                    // Registro de apertura en el servidor (Fire & Forget)
                    viewModelScope.launch {
                        try {
                            val timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                            val request = GateOpenRequest(fecha_hora = timestamp)
                            apiService.logGateOpen(OdooRequest(request))
                        } catch (apiError: Exception) {
                            android.util.Log.e("MainViewModel", "Error al registrar apertura: ${apiError.message}")
                        }
                    }
                } else if (result is GateResult.Error) {
                    throw Exception(result.message)
                }

                delay(2000)
                _bleState.value = BleState.DISCONNECTED
            } catch (e: Exception) {
                _bleState.value = BleState.ERROR
                _gateMessage.value = e.message
                delay(3000)
                _bleState.value = BleState.DISCONNECTED
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
