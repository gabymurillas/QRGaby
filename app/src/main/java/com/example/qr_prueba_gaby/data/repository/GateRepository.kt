package com.example.qr_prueba_gaby.data.repository

import android.annotation.SuppressLint
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context
import com.example.qr_prueba_gaby.presentation.ui.viewmodels.SPP_UUID
import com.example.qr_prueba_gaby.presentation.ui.viewmodels.TARGET_MAC
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

sealed class GateResult {
    object Success : GateResult()
    data class Error(val message: String) : GateResult()
}

/**
 * Repositorio encargado de la comunicación Bluetooth con el hardware del portón (ESP32).
 */
class GateRepository(
    private val context: Context
) {

    @SuppressLint("MissingPermission")
    suspend fun openGate(androidId: String): GateResult = withContext(Dispatchers.IO) {
        var socket: BluetoothSocket? = null
        try {
            val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
            val adapter = bluetoothManager.adapter ?: return@withContext GateResult.Error("Bluetooth no disponible")
            
            val device = adapter.getRemoteDevice(TARGET_MAC)
            socket = device.createRfcommSocketToServiceRecord(SPP_UUID)
            
            withTimeoutOrNull(5000) { socket.connect() } ?: throw Exception("Sin respuesta del portón")
            
            val writer = java.io.BufferedWriter(java.io.OutputStreamWriter(socket.outputStream))
            val reader = java.io.BufferedReader(java.io.InputStreamReader(socket.inputStream))

            // Enviar ID
            writer.write("$androidId\n")
            writer.flush()

            // Esperar solicitud de huella si aplica
            val response1 = reader.readLine()
            if (response1?.contains("SOLICITUD_HUELLA") == true) {
                writer.write("HUELLA_OK\n")
                writer.flush()
                
                // Confirmación final
                val response2 = reader.readLine()
                if (response2?.contains("ACCESO_CONCEDIDO") == true) {
                    return@withContext GateResult.Success
                } else {
                    return@withContext GateResult.Error(response2 ?: "Acceso Denegado")
                }
            } else if (response1?.contains("ACCESO_CONCEDIDO") == true) {
                return@withContext GateResult.Success
            }

            GateResult.Error(response1 ?: "Respuesta inesperada del hardware")

        } catch (e: Exception) {
            GateResult.Error(e.message ?: "Error de conexión")
        } finally {
            try { socket?.close() } catch (_: Exception) {}
        }
    }
}
