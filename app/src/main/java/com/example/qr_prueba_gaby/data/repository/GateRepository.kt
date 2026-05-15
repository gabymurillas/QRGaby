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
import android.bluetooth.BluetoothDevice

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

            try {
                socket.connect()
            } catch (e: IOException) {
                throw Exception("Sin respuesta del portón (timeout de conexión)")
            }

            Log.d("GateRepository", "Conectado. Enviando Android ID: $androidId")
            val writer = java.io.BufferedWriter(java.io.OutputStreamWriter(socket.outputStream))
            val reader = java.io.BufferedReader(java.io.InputStreamReader(socket.inputStream))

            // 1. Enviar el Android ID — el ESP32 lo busca en su base de datos
            writer.write("$androidId\n")
            writer.flush()

            // 2. Leer respuestas del ESP32 con timeout de 8 s
            var lastResponse: String? = null
            val result = withTimeoutOrNull(8_000) {
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
            try {
                socket?.close()
                Log.d("GateRepository", "Socket cerrado")
                
                // Desvincular (quitar emparejamiento) para asegurar que no permanezca conectado/vinculado
                val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
                val adapter = bluetoothManager.adapter
                if (adapter != null) {
                    val device = adapter.getRemoteDevice(TARGET_MAC)
                    if (device.bondState != BluetoothDevice.BOND_NONE) {
                        Log.d("GateRepository", "Desvinculando dispositivo...")
                        val method = device.javaClass.getMethod("removeBond")
                        method.invoke(device)
                    }
                }
            } catch (e: Exception) {
                Log.e("GateRepository", "Error en desconexión: ${e.message}")
            }
        }
    }
}
