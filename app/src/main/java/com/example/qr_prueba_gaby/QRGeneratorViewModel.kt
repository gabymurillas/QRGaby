package com.example.qr_prueba_gaby

import android.graphics.Bitmap
import android.graphics.Color
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.security.MessageDigest

private const val SECRET_KEY = "TuClaveSecretaAqui"

class QRGeneratorViewModel : ViewModel() {

    private val _qrBitmap = MutableStateFlow<Bitmap?>(null)
    val qrBitmap: StateFlow<Bitmap?> = _qrBitmap

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    fun generateQR(androidId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val bitmap = withContext(Dispatchers.IO) {
                    val timestamp = System.currentTimeMillis() / 1000
                    val secureData = generateSecureToken(androidId, timestamp)
                    generateQRCode(secureData, 512)
                }
                _qrBitmap.value = bitmap
            } catch (e: WriterException) {
                _errorMessage.value = "Error al generar el formato QR: ${e.message}"
            } catch (e: Exception) {
                _errorMessage.value = "Error inesperado: ${e.localizedMessage ?: "Desconocido"}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun generateSecureToken(id: String, timestamp: Long): String {
        val dataToHash = "$id$timestamp$SECRET_KEY"
        val hash = sha256(dataToHash)
        return "ID: $id\nVERIF: ${hash.take(12)}\nFECHA: $timestamp"
    }

    private fun sha256(input: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    @Throws(WriterException::class)
    private fun generateQRCode(text: String, size: Int): Bitmap {
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
}
