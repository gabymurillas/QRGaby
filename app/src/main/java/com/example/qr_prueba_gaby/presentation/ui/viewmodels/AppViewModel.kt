package com.example.qr_prueba_gaby.presentation.ui.viewmodels

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.provider.Settings
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.qr_prueba_gaby.utils.CryptoManager
import com.example.qr_prueba_gaby.data.pref.UserDataStore
import com.example.qr_prueba_gaby.data.network.service.ApiService
import com.example.qr_prueba_gaby.data.network.service.SyncParams
import com.example.qr_prueba_gaby.data.network.service.OdooRequest
import com.example.qr_prueba_gaby.data.model.UserData
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
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
import kotlinx.coroutines.withTimeoutOrNull
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID
import javax.inject.Inject

const val TARGET_MAC = "E0:5A:1B:30:FD:42"
val SPP_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

enum class BleState { DISCONNECTED, CONNECTING, CONNECTED, SENT, ERROR }

@HiltViewModel
class AppViewModel @Inject constructor(
    private val dataStore: UserDataStore,
    private val apiService: ApiService,
    @ApplicationContext private val context: Context
) : ViewModel() {

    // ──── Estado de Registro ────
    private val _nombre = MutableStateFlow("")
    val nombre: StateFlow<String> = _nombre.asStateFlow()

    private val _cedula = MutableStateFlow("")
    val cedula: StateFlow<String> = _cedula.asStateFlow()

    private val _plates = MutableStateFlow(listOf(""))
    val plates: StateFlow<List<String>> = _plates.asStateFlow()

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _registrationError = MutableStateFlow<String?>(null)
    val registrationError: StateFlow<String?> = _registrationError.asStateFlow()

    private val _isValidating = MutableStateFlow(false)
    val isValidating: StateFlow<Boolean> = _isValidating.asStateFlow()

    // ──── Estado QR ────
    private val _qrBitmap = MutableStateFlow<Bitmap?>(null)
    val qrBitmap: StateFlow<Bitmap?> = _qrBitmap.asStateFlow()

    private val _encryptedAndroidId = MutableStateFlow("")
    val encryptedAndroidId: StateFlow<String> = _encryptedAndroidId.asStateFlow()

    private val _decryptedAndroidId = MutableStateFlow<String?>(null)
    val decryptedAndroidId: StateFlow<String?> = _decryptedAndroidId.asStateFlow()

    // ──── Estado Activación (con manejo de carga inicial null para evitar parpadeo) ────
    val isRegisteredFlow: StateFlow<Boolean?> = dataStore.isRegisteredFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val isActivatedFlow: StateFlow<Boolean?> = dataStore.isActivatedFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val userDataFlow = dataStore.userDataFlow

    // ──── Estado BLE ────
    private val _bleState = MutableStateFlow(BleState.DISCONNECTED)
    val bleState: StateFlow<BleState> = _bleState.asStateFlow()

    private val _gateMessage = MutableStateFlow<String?>(null)
    val gateMessage: StateFlow<String?> = _gateMessage.asStateFlow()

    fun onNombreChange(value: String) { _nombre.value = value }
    fun onCedulaChange(newCedula: String) {
        _cedula.value = newCedula.filter { it.isDigit() }
    }

    fun onPlateChange(index: Int, value: String) {
        val updated = _plates.value.toMutableList()
        if (index < updated.size) { updated[index] = value.uppercase() }
        _plates.value = updated
    }

    fun addPlate() {
        if (_plates.value.size < 5) {
            _plates.value = _plates.value + ""
        }
    }

    fun removePlate(index: Int) {
        if (_plates.value.size > 1) {
            _plates.value = _plates.value.toMutableList().also { it.removeAt(index) }
        }
    }

    @SuppressLint("HardwareIds")
    fun generateKey(onSuccess: () -> Unit) {
        if (_nombre.value.isBlank() || _cedula.value.isBlank()) {
            _registrationError.value = "Por favor, completa todos los campos personales."
            return
        }
        if (_plates.value.all { it.isBlank() }) {
            _registrationError.value = "Debes registrar al menos un vehículo."
            return
        }

        viewModelScope.launch {
            _isGenerating.value = true
            _registrationError.value = null
            try {
                val androidId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
                val aidEncrypted = CryptoManager.encrypt(androidId)
                _encryptedAndroidId.value = aidEncrypted

                val userData = UserData(
                    u = _nombre.value,
                    c = _cedula.value,
                    p = _plates.value.filter { it.isNotBlank() },
                    aid = aidEncrypted
                )

                val qr = withContext(Dispatchers.Default) {
                    generateQrBitmap(userData.toJson(), 512)
                }
                _qrBitmap.value = qr

                dataStore.saveUser(userData)
                onSuccess()
            } catch (e: Exception) {
                _registrationError.value = "Error al generar la llave: ${e.localizedMessage}"
            } finally {
                _isGenerating.value = false
            }
        }
    }

    fun showDecryptedId() {
        viewModelScope.launch {
            val encrypted = _encryptedAndroidId.value.ifBlank {
                dataStore.userDataFlow.stateIn(viewModelScope).value?.aid ?: ""
            }
            if (encrypted.isNotBlank()) {
                val decrypted = CryptoManager.decrypt(encrypted)
                _decryptedAndroidId.value = decrypted
                delay(60_000) // 60 segundos
                _decryptedAndroidId.value = null
            }
        }
    }

    fun resetRegistration(onDone: () -> Unit = {}) {
        viewModelScope.launch {
            dataStore.clearAll()
            // Limpiamos estados locales
            _nombre.value = ""
            _cedula.value = ""
            _plates.value = listOf("")
            _qrBitmap.value = null
            _encryptedAndroidId.value = ""
            onDone()
        }
    }

    fun validateOnEndpoint(onSuccess: () -> Unit, onError: (String) -> Unit) {
        if (_isValidating.value) return
        
        viewModelScope.launch {
            _isValidating.value = true
            try {
                val userData = dataStore.userDataFlow.stateIn(viewModelScope).value ?: return@launch
                
                val response = apiService.syncVehicular(_cedula.value)
                
                if (response.isSuccessful && response.body()?.result?.status == "success") {
                    activateUser()
                    onSuccess()
                } else {
                    val result = response.body()?.result
                    val errorMsg = when {
                        response.code() == 401 -> "Error de autenticación con el servidor Odoo."
                        response.code() == 404 -> "Endpoint de sincronización no encontrado."
                        result?.status == "error" -> result.message ?: "Acceso denegado por el sistema."
                        else -> "El servidor no autorizó el acceso. Verifica tus datos."
                    }
                    onError(errorMsg)
                }
            } catch (e: java.net.ConnectException) {
                onError("No se pudo conectar al servidor local 172.17.2.178. ¿Estás en la misma red?")
            } catch (e: java.net.SocketTimeoutException) {
                onError("El tiempo de espera con el servidor ha expirado.")
            } catch (e: Exception) {
                onError("Error de conexión: ${e.localizedMessage}")
            } finally {
                _isValidating.value = false
            }
        }
    }

    fun activateUser() {
        viewModelScope.launch {
            dataStore.setActivated(true)
            dataStore.setRegistered(true)
        }
    }

    // Ya no hacemos escaneo automático, es a demanda
    fun startBleScan() {
        _bleState.value = BleState.DISCONNECTED
    }

    fun stopBleScan() {
        _bleState.value = BleState.DISCONNECTED
    }

    @SuppressLint("MissingPermission")
    fun openGate() {
        if (_bleState.value == BleState.CONNECTING) return
        _bleState.value = BleState.CONNECTING

        viewModelScope.launch(Dispatchers.IO) {
            var socket: BluetoothSocket? = null
            try {
                val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
                val adapter = bluetoothManager.adapter
                if (adapter == null || !adapter.isEnabled) {
                    _bleState.value = BleState.ERROR
                    _gateMessage.value = "Bluetooth apagado o no disponible"
                    return@launch
                }

                val device = adapter.getRemoteDevice(TARGET_MAC)
                // Usar SPP estándar
                socket = device.createRfcommSocketToServiceRecord(SPP_UUID)
                
                // Intentar conectar con un timeout razonable
                withTimeoutOrNull(5000) {
                    socket.connect()
                } ?: throw Exception("Timeout al conectar con el portón")

                _bleState.value = BleState.CONNECTED
                
                val writer = java.io.BufferedWriter(java.io.OutputStreamWriter(socket.outputStream))
                val reader = java.io.BufferedReader(java.io.InputStreamReader(socket.inputStream))

                // 1. Obtener ID cifrado y desencriptarlo para enviarlo a la placa
                val encryptedId = _encryptedAndroidId.value.ifBlank {
                    dataStore.userDataFlow.stateIn(this).value?.aid ?: ""
                }
                if (encryptedId.isBlank()) {
                    throw Exception("ID de dispositivo no encontrado")
                }
                val idToSend = CryptoManager.decrypt(encryptedId)
                
                writer.write("$idToSend\n")
                writer.flush()

                // 2. Leer respuesta ESP32 (Esperamos "SOLICITUD_HUELLA")
                val response1 = reader.readLine() ?: ""

                if (response1.contains("SOLICITUD_HUELLA")) {
                    // 3. Enviar HUELLA_OK de inmediato (por ahora sin biometría real)
                    writer.write("HUELLA_OK\n")
                    writer.flush()

                    // 4. Leer resultado final
                    val response2 = reader.readLine() ?: ""

                    if (response2.contains("ACCESO_CONCEDIDO")) {
                        _bleState.value = BleState.SENT
                        _gateMessage.value = "¡Acceso Concedido! Portón abriéndose."
                    } else if (response2.contains("ACCESO_DENEGADO")) {
                        throw Exception("Acceso Denegado por el portón")
                    } else if (response2.contains("TIMEOUT")) {
                        throw Exception("El portón no recibió respuesta")
                    } else {
                        throw Exception("Respuesta inesperada: $response2")
                    }
                } else if (response1.contains("MAC_NO_REGISTRADA")) {
                    throw Exception("Tu dispositivo no está registrado en el portón")
                } else {
                    throw Exception("Respuesta inesperada del portón: $response1")
                }

                delay(3000)
                _gateMessage.value = null
                _bleState.value = BleState.DISCONNECTED

            } catch (e: Exception) {
                android.util.Log.e("BT_SPP", "Error SPP: ${e.message}")
                _bleState.value = BleState.ERROR
                _gateMessage.value = e.message ?: "Error al conectar con el portón"
                delay(4000) // Mostrar error por un momento
                _bleState.value = BleState.DISCONNECTED
            } finally {
                try {
                    socket?.close()
                } catch (e: Exception) {
                    // Ignorar
                }
            }
        }
    }

    fun clearGateMessage() { _gateMessage.value = null }

    private fun generateQrBitmap(text: String, size: Int): Bitmap {
        val bitMatrix = MultiFormatWriter().encode(text, BarcodeFormat.QR_CODE, size, size)
        val width = bitMatrix.width
        val height = bitMatrix.height
        val pixels = IntArray(width * height)
        for (y in 0 until height) {
            val offset = y * width
            for (x in 0 until width) {
                pixels[offset + x] = if (bitMatrix.get(x, y)) Color.BLACK else Color.WHITE
            }
        }
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
        return bitmap
    }

    override fun onCleared() {
        super.onCleared()
    }
}
