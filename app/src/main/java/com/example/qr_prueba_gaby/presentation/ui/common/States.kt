package com.example.qr_prueba_gaby.presentation.ui.common

/**
 * Estado de la solicitud de apertura remota del portón.
 *
 * En V6 la apertura ya no usa Bluetooth: la app hace una petición HTTP a Odoo
 * y es Odoo quien dispara el relé del ESP32.
 *
 *  - IDLE       → sin operación en curso.
 *  - REQUESTING → petición HTTP enviada, esperando respuesta de Odoo.
 *  - OPENED     → Odoo confirmó la apertura del portón.
 *  - ERROR      → fallo de red o acceso denegado.
 */
enum class GateState { IDLE, REQUESTING, OPENED, ERROR }

/** Estado de la verificación del conductor contra el servidor Odoo. */
enum class OdooStatus { VERIFYING, VALID, INVALID, OFFLINE }
