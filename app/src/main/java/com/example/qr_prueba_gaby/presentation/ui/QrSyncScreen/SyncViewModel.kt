package com.example.qr_prueba_gaby.presentation.ui.QrSyncScreen

import android.graphics.Bitmap
import android.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.qr_prueba_gaby.data.network.service.ApiService
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
import javax.inject.Inject

@HiltViewModel
class SyncViewModel @Inject constructor(
    private val dataStore: UserDataStore,
    private val apiService: ApiService
) : ViewModel() {

    private val _qrBitmap = MutableStateFlow<Bitmap?>(null)
    val qrBitmap: StateFlow<Bitmap?> = _qrBitmap.asStateFlow()

    private val _isValidating = MutableStateFlow(false)
    val isValidating: StateFlow<Boolean> = _isValidating.asStateFlow()

    private val _decryptedAndroidId = MutableStateFlow<String?>(null)
    val decryptedAndroidId: StateFlow<String?> = _decryptedAndroidId.asStateFlow()

    private val _idVisibilityProgress = MutableStateFlow(0f)
    val idVisibilityProgress: StateFlow<Float> = _idVisibilityProgress.asStateFlow()

    private var idVisibilityJob: Job? = null

    val userDataFlow = dataStore.userDataFlow
    val isActivatedFlow: StateFlow<Boolean?> = dataStore.isActivatedFlow
        .stateIn(viewModelScope, SharingStarted.Companion.WhileSubscribed(5000), null)

    init {
        generateQrFromStoredData()
    }

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

    fun validateOnEndpoint(cedula: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        if (_isValidating.value) return
        viewModelScope.launch {
            _isValidating.value = true
            try {
                val response = apiService.syncVehicular(cedula)
                if (response.isSuccessful && response.body()?.result?.status == "success") {
                    dataStore.setActivated(true)
                    dataStore.setRegistered(true)
                    onSuccess()
                } else {
                    onError(response.body()?.result?.message ?: "Error de validación")
                }
            } catch (e: Exception) {
                onError("Error de conexión: ${e.localizedMessage}")
            } finally {
                _isValidating.value = false
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

    private fun generateQrBitmap(text: String, size: Int): Bitmap {
        val bitMatrix = MultiFormatWriter().encode(text, BarcodeFormat.QR_CODE, size, size)
        val pixels = IntArray(size * size)
        for (y in 0 until size) {
            for (x in 0 until size) {
                pixels[y * size + x] = if (bitMatrix.get(x, y)) Color.BLACK else Color.WHITE
            }
        }
        return Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888).apply { setPixels(pixels, 0, size, 0, 0, size, size) }
    }
}