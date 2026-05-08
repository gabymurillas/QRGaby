package com.example.qr_prueba_gaby.presentation.ui.common

enum class BleState { DISCONNECTED, CONNECTING, CONNECTED, SENT, ERROR }
enum class OdooStatus { VERIFYING, VALID, INVALID, OFFLINE }

/** Estados del servidor Bluetooth clásico (RFCOMM) de provisionamiento. */
enum class BtServerState {
    IDLE,       // Servidor no iniciado
    LISTENING,  // Esperando conexión del Administrador
    RECEIVED,   // Paquete JSON recibido y guardado con éxito
    TOKEN_ERROR,// Token recibido no coincide con el esperado
    ERROR       // Error de conexión o JSON inválido
}

const val TARGET_MAC = "E0:5A:1B:31:29:6E"
val SPP_UUID: java.util.UUID = java.util.UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
