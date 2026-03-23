package com.example.qr_prueba_gaby.ui.viewmodel

import android.annotation.SuppressLint
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.os.ParcelUuid
import android.provider.Settings
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.qr_prueba_gaby.data.CryptoManager
import com.example.qr_prueba_gaby.data.UserDataStore
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
import javax.inject.Inject

const val GATE_SERVICE_UUID = "0000ffe0-0000-1000-8000-00805f9b34fb"
const val RSSI_THRESHOLD = -70

enum class BleState { SEARCHING, IN_RANGE, CONNECTING, SENT, ERROR }

@HiltViewModel
class AppViewModel @Inject constructor(
    private val dataStore: UserDataStore,
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

    // ──── Estado QR ────
    private val _qrBitmap = MutableStateFlow<Bitmap?>(null)
    val qrBitmap: StateFlow<Bitmap?> = _qrBitmap.asStateFlow()

    private val _encryptedAndroidId = MutableStateFlow("")
    val encryptedAndroidId: StateFlow<String> = _encryptedAndroidId.asStateFlow()

    // ──── Estado Activación (con manejo de carga inicial null para evitar parpadeo) ────
    val isRegisteredFlow: StateFlow<Boolean?> = dataStore.isRegisteredFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val userDataFlow = dataStore.userDataFlow

    // ──── Estado BLE ────
    private val _bleState = MutableStateFlow(BleState.SEARCHING)
    val bleState: StateFlow<BleState> = _bleState.asStateFlow()

    private val _gateMessage = MutableStateFlow<String?>(null)
    val gateMessage: StateFlow<String?> = _gateMessage.asStateFlow()

    private var scanJob: Job? = null
    private var scanCallback: ScanCallback? = null

    fun onNombreChange(value: String) { _nombre.value = value }
    fun onCedulaChange(value: String) { _cedula.value = value.filter { it.isDigit() } }

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
        val nombreVal = _nombre.value.trim()
        val cedulaVal = _cedula.value.trim()
        val plates = _plates.value.filter { it.isNotBlank() }

        if (nombreVal.isBlank()) { _registrationError.value = "Ingresa tu nombre"; return }
        if (cedulaVal.isBlank()) { _registrationError.value = "Ingresa tu cédula"; return }
        if (plates.isEmpty()) { _registrationError.value = "Agrega al menos una placa"; return }
        _registrationError.value = null

        viewModelScope.launch {
            _isGenerating.value = true
            try {
                val androidId = Settings.Secure.getString(
                    context.contentResolver, Settings.Secure.ANDROID_ID
                ) ?: "unknown"

                val encrypted = withContext(Dispatchers.Default) {
                    CryptoManager.encrypt(androidId)
                }
                _encryptedAndroidId.value = encrypted

                val userData = UserData(
                    u = nombreVal,
                    c = cedulaVal,
                    p = plates,
                    aid = encrypted
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

    fun activateUser() {
        viewModelScope.launch {
            dataStore.setRegistered(true)
        }
    }

    @SuppressLint("MissingPermission")
    fun startBleScan() {
        if (_bleState.value == BleState.IN_RANGE) return

        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val adapter = bluetoothManager.adapter ?: run {
            _bleState.value = BleState.ERROR
            return
        }
        if (!adapter.isEnabled) {
            _bleState.value = BleState.ERROR
            return
        }

        val scanner = adapter.bluetoothLeScanner ?: return
        _bleState.value = BleState.SEARCHING

        val filter = ScanFilter.Builder()
            .setServiceUuid(ParcelUuid.fromString(GATE_SERVICE_UUID))
            .build()
        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()

        val callback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                if (result.rssi >= RSSI_THRESHOLD) {
                    _bleState.value = BleState.IN_RANGE
                }
            }
            override fun onScanFailed(errorCode: Int) {
                _bleState.value = BleState.ERROR
            }
        }
        scanCallback = callback

        scanJob = viewModelScope.launch {
            scanner.startScan(listOf(filter), settings, callback)
            delay(30_000)
            if (_bleState.value == BleState.SEARCHING) {
                scanner.stopScan(callback)
                delay(2_000)
                startBleScan()
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun stopBleScan() {
        val manager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
        val scanner = manager?.adapter?.bluetoothLeScanner
        scanCallback?.let { scanner?.stopScan(it) }
        scanCallback = null
        scanJob?.cancel()
    }

    fun openGate() {
        if (_bleState.value != BleState.IN_RANGE) return
        _bleState.value = BleState.CONNECTING

        viewModelScope.launch {
            try {
                delay(2_000)
                _bleState.value = BleState.SENT
                _gateMessage.value = "Señal enviada. ¡Portón abriéndose!"
                delay(5_000)
                _gateMessage.value = null
                _bleState.value = BleState.IN_RANGE
            } catch (e: Exception) {
                _bleState.value = BleState.ERROR
                _gateMessage.value = "Error al conectar con el portón"
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
        scanJob?.cancel()
    }
}
