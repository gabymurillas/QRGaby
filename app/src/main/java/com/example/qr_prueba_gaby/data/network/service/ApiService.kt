package com.example.qr_prueba_gaby.data.network.service

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

/**
 * Sobre JSON-RPC 2.0 nativo de Odoo 13.
 *
 * Todos los endpoints de Odoo son `type='json'` y solo aceptan POST. Cada
 * petición debe enviarse envuelta así:
 * `{"jsonrpc":"2.0","method":"call","params":{...},"id":1}`.
 */
data class OdooRequest<T>(
    val params: T,
    val jsonrpc: String = "2.0",
    val method: String = "call",
    val id: Int = 1
)

/**
 * Parámetros del CRUD unificado `/api/control_acceso`.
 * La acción se define con el campo `action`: "read" | "create" | "update" | "delete".
 * La app de usuario solo usa "read" (consulta de estado, read-only).
 */
data class ControlAccesoParams(
    val action: String,
    val cedula: String
)

/** Parámetros de la apertura remota `/api/open_gate`. */
data class GateOpenParams(
    val cedula: String
)

data class OdooResponse(
    val result: SyncResult? = null,
    val error: OdooError? = null
)

data class SyncResult(
    val status: String, // "success", "pending", "not_found", "error"
    val message: String? = null
)

data class OdooError(
    val code: Int,
    val message: String,
    val data: Any? = null
)

interface ApiService {
    /**
     * Consulta de estado del conductor (read-only). NO abre el portón.
     * Usa la acción "read" del CRUD unificado de Odoo.
     */
    @Headers("Content-Type: application/json")
    @POST("api/control_acceso")
    suspend fun syncVehicular(@Body request: OdooRequest<ControlAccesoParams>): Response<OdooResponse>

    /**
     * Solicitud de apertura remota del portón.
     * Odoo valida al conductor y, de estar autorizado, dispara el relé del ESP32.
     */
    @Headers("Content-Type: application/json")
    @POST("api/open_gate")
    suspend fun requestGateOpen(@Body request: OdooRequest<GateOpenParams>): Response<OdooResponse>
}
