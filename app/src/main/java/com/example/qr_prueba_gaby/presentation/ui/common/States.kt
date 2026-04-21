package com.example.qr_prueba_gaby.presentation.ui.common

enum class BleState { DISCONNECTED, CONNECTING, CONNECTED, SENT, ERROR }
enum class OdooStatus { VERIFYING, VALID, INVALID, OFFLINE }

const val TARGET_MAC = "E0:5A:1B:31:29:6E"
val SPP_UUID: java.util.UUID = java.util.UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
