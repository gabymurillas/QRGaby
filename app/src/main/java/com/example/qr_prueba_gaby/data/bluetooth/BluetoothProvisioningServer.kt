package com.example.qr_prueba_gaby.data.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothServerSocket
import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.UUID

// ──────────────────────────────────────────────────────────────────────────────
// Resultado sellado que devuelve el servidor tras procesar una conexión
// ──────────────────────────────────────────────────────────────────────────────
sealed class ProvisioningResult {
    /** Paquete válido recibido y token correcto. */
    data class Success(val endpoint: String, val mac: String) : ProvisioningResult()
    /** El JSON llegó correctamente pero el token no coincide. */
    object TokenMismatch : ProvisioningResult()
    /** Cualquier error de conexión, IO o parseo. */
    data class Error(val message: String) : ProvisioningResult()
}

/**
 * Servidor Bluetooth clásico (RFCOMM / SPP) para el provisionamiento de la app.
 *
 * Ciclo de vida:
 *   - Llamar a [startListening] en una corrutina IO → bloquea hasta recibir
 *     una conexión y un paquete JSON válido.
 *   - Llamar a [stop] desde [onCleared] del ViewModel o [DisposableEffect]
 *     del Composable para cerrar el socket y liberar recursos.
 *
 * Protocolo esperado (JSON enviado por la App Admin):
 * ```json
 * {
 *   "endpoint"   : "https://backend.ejemplo.com",
 *   "target_mac" : "E0:5A:1B:31:29:6E",
 *   "token"      : "ALCARAVAN_2025"
 * }
 * ```
 */
class BluetoothProvisioningServer(private val context: Context) {

    companion object {
        private val SERVER_UUID: UUID =
            UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
        private const val SERVICE_NAME  = "AlcaravanProvisioning"
        private const val EXPECTED_TOKEN = "ALCARAVAN_2025"
        private const val TAG = "BTProvServer"
    }

    @Volatile
    private var serverSocket: BluetoothServerSocket? = null

    // ──────────────────────────────────────────────────────────────────────────
    // API pública
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Inicia el servidor RFCOMM y queda bloqueado esperando UNA conexión.
     * Debe llamarse desde [Dispatchers.IO].
     * Una vez recibido el paquete (o ante un error), cierra todos los sockets.
     */
    @SuppressLint("MissingPermission")
    suspend fun startListening(): ProvisioningResult = withContext(Dispatchers.IO) {
        Log.d(TAG, "Iniciando servidor de provisionamiento...")
        try {
            val adapter = (context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager)
                .adapter ?: return@withContext ProvisioningResult.Error("Bluetooth no disponible en este dispositivo")

            if (!adapter.isEnabled)
                return@withContext ProvisioningResult.Error("Bluetooth desactivado")

            // Abrir el servidor RFCOMM
            serverSocket = adapter.listenUsingRfcommWithServiceRecord(SERVICE_NAME, SERVER_UUID)
            Log.d(TAG, "Servidor RFCOMM abierto. Esperando conexión del Administrador...")

            // accept() es bloqueante — se desbloquea cuando el Admin se conecta
            val clientSocket = serverSocket!!.accept()
            Log.d(TAG, "Conexión aceptada desde: ${clientSocket.remoteDevice.address}")

            // Una vez conectado, cerrar el ServerSocket para no aceptar más
            try { serverSocket?.close() } catch (_: Exception) {}
            serverSocket = null

            // Leer el JSON del InputStream usando brace-counting
            clientSocket.use { socket ->
                try {
                    val reader = BufferedReader(InputStreamReader(socket.inputStream))
                    val jsonStr = readFullJson(reader)
                    Log.d(TAG, "JSON recibido: $jsonStr")
                    parseAndValidate(jsonStr)
                } catch (e: Exception) {
                    Log.e(TAG, "Error leyendo datos: ${e.message}")
                    ProvisioningResult.Error("Error al leer el paquete: ${e.message}")
                }
            }

        } catch (e: Exception) {
            // Excepción normal si stop() cierra el serverSocket mientras se espera
            if (serverSocket == null) {
                Log.d(TAG, "Servidor detenido externamente.")
                ProvisioningResult.Error("Servidor detenido")
            } else {
                Log.e(TAG, "Error en el servidor: ${e.message}")
                ProvisioningResult.Error(e.message ?: "Error desconocido de Bluetooth")
            }
        } finally {
            try { serverSocket?.close() } catch (_: Exception) {}
            serverSocket = null
            Log.d(TAG, "Recursos del servidor liberados.")
        }
    }

    /**
     * Detiene el servidor cerrando el [BluetoothServerSocket].
     * Si [startListening] estaba bloqueado en `accept()`, se desbloquea con excepción.
     */
    fun stop() {
        Log.d(TAG, "stop() llamado — cerrando servidor.")
        try { serverSocket?.close() } catch (_: Exception) {}
        serverSocket = null
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Helpers privados
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Lee el stream carácter a carácter y retorna el primer objeto JSON completo.
     * Usa conteo de llaves `{}` para determinar el fin del objeto sin depender
     * de caracteres de cierre externos (e.g. `\n`).
     */
    private fun readFullJson(reader: BufferedReader): String {
        val sb = StringBuilder()
        var braceDepth = 0
        var jsonStarted = false

        while (true) {
            val ch = reader.read()
            if (ch == -1) break              // stream cerrado
            val c = ch.toChar()
            sb.append(c)

            when (c) {
                '{'  -> { braceDepth++; jsonStarted = true }
                '}'  -> braceDepth--
            }

            if (jsonStarted && braceDepth == 0) break  // objeto completo
        }
        return sb.toString().trim()
    }

    /**
     * Parsea el JSON y valida el token.
     * Retorna [ProvisioningResult.Success], [ProvisioningResult.TokenMismatch]
     * o [ProvisioningResult.Error] si el JSON está malformado.
     */
    private fun parseAndValidate(jsonStr: String): ProvisioningResult {
        return try {
            val json     = JSONObject(jsonStr)
            val endpoint = json.getString("endpoint")
            val mac      = json.getString("target_mac")
            val token    = json.getString("token")

            if (token != EXPECTED_TOKEN) {
                Log.w(TAG, "Token inválido recibido: $token")
                return ProvisioningResult.TokenMismatch
            }

            if (endpoint.isBlank() || mac.isBlank()) {
                return ProvisioningResult.Error("El endpoint o la MAC están vacíos")
            }

            Log.d(TAG, "Provisionamiento válido. Endpoint: $endpoint | MAC: $mac")
            ProvisioningResult.Success(endpoint = endpoint, mac = mac)

        } catch (e: JSONException) {
            Log.e(TAG, "JSON malformado: ${e.message}")
            ProvisioningResult.Error("JSON inválido o incompleto: ${e.message}")
        }
    }
}
