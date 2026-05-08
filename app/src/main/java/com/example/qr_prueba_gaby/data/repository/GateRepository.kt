package com.example.qr_prueba_gaby.data.repository

import android.annotation.SuppressLint
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.util.Log
import com.example.qr_prueba_gaby.presentation.ui.common.SPP_UUID
import com.example.qr_prueba_gaby.presentation.ui.common.TARGET_MAC
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.io.IOException

sealed class GateResult {
    object Success : GateResult()
    data class Error(val message: String) : GateResult()
}

/**
 * Repositorio encargado de la comunicación Bluetooth SPP con el ESP32.
 *
 * Protocolo acordado:
 *   Android → envía Android ID\n       → ESP32
 *   ESP32   → "PEDIR_HUELLA"       → Android
 *   Android → "HUELLA_OK\n"            → ESP32
 *   ESP32   → "ACCESO_OK"       → Android ✅
 */
class GateRepository(
    private val context: Context
) {

    @SuppressLint("MissingPermission")
    suspend fun openGate(androidId: String): GateResult = withContext(Dispatchers.IO) {
        var socket: BluetoothSocket? = null
        try {
            Log.d("GateRepository", "Conectando al ESP32: $TARGET_MAC")
            val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
            val adapter = bluetoothManager.adapter
                ?: return@withContext GateResult.Error("Bluetooth no disponible")

            if (!adapter.isEnabled)
                return@withContext GateResult.Error("Bluetooth desactivado")

            val device = adapter.getRemoteDevice(TARGET_MAC)
            socket = device.createRfcommSocketToServiceRecord(SPP_UUID)

            withTimeoutOrNull(10_000) { socket.connect() }
                ?: throw Exception("Sin respuesta del portón (timeout de conexión)")

            Log.d("GateRepository", "Conectado. Enviando Android ID: $androidId")
            val writer = java.io.BufferedWriter(java.io.OutputStreamWriter(socket.outputStream))
            val reader = java.io.BufferedReader(java.io.InputStreamReader(socket.inputStream))

            // 1. Enviar el Android ID — el ESP32 lo busca en su base de datos
            writer.write("$androidId\n")
            writer.flush()

            // 2. Leer respuestas del ESP32 con timeout de 20 s
            var lastResponse: String? = null
            val result = withTimeoutOrNull(20_000) {
                var finalResult: GateResult? = null
                while (finalResult == null) {
                    val line = try {
                        reader.readLine()?.trim() ?: break
                    } catch (e: IOException) { break }

                    if (line.isEmpty()) continue
                    Log.d("GateRepository", "ESP32 → $line")
                    lastResponse = line

                    when {
                        line.contains("PEDIR_HUELLA") -> {
                            Log.d("GateRepository", "Enviando HUELLA_OK...")
                            writer.write("HUELLA_OK\n")
                            writer.flush()
                        }
                        line.contains("ACCESO_OK") ->
                            finalResult = GateResult.Success

                        line.contains("ACCESO_DENEGADO") ->
                            finalResult = GateResult.Error("Acceso denegado por el hardware")

                        line.contains("MAC_NO_REGISTRADA") || line.contains("ID_NO_REGISTRADO") ->
                            finalResult = GateResult.Error(
                                "Dispositivo no registrado en el ESP32.\nAndroid ID: $androidId"
                            )
                        line.contains("TIMEOUT") ->
                            finalResult = GateResult.Error("Timeout interno del ESP32")
                    }
                }
                finalResult
            }

            result ?: GateResult.Error(lastResponse ?: "Sin respuesta del ESP32 (timeout)")

        } catch (e: Exception) {
            Log.e("GateRepository", "Error: ${e.message}")
            GateResult.Error(e.message ?: "Error de conexión Bluetooth")
        } finally {
            try { socket?.close() } catch (_: Exception) {}
            Log.d("GateRepository", "Socket cerrado")
        }
    }

    @SuppressLint("MissingPermission")
    suspend fun checkAvailability(): Boolean = withContext(Dispatchers.IO) {
        val adapter = (context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager)
            .adapter ?: return@withContext false
        if (!adapter.isEnabled) return@withContext false

        var socket: BluetoothSocket? = null
        return@withContext try {
            socket = adapter.getRemoteDevice(TARGET_MAC)
                .createRfcommSocketToServiceRecord(SPP_UUID)
            withTimeoutOrNull(2500) { socket.connect() } != null
        } catch (e: Exception) {
            false
        } finally {
            try { socket?.close() } catch (_: Exception) {}
        }
    }
}
