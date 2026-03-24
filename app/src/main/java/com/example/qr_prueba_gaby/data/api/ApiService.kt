package com.example.qr_prueba_gaby.data.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Modelos para seguir el formato Odoo (params wrapper).
 */
data class OdooRequest<T>(val params: T)

data class SyncParams(
    val nombre: String,
    val cedula: String,
    val telefono: String,
    val placas: String,
    val android_id: String // El "aid" que guardamos
)

data class OdooResponse(
    val result: SyncResult? = null,
    val error: OdooError? = null
)

data class SyncResult(
    val status: String, // "success", "pending", "error"
    val message: String? = null
)

data class OdooError(
    val code: Int,
    val message: String,
    val data: Any? = null
)

interface ApiService {
    @POST("api/sync_vehicular")
    suspend fun syncVehicular(@Body request: OdooRequest<SyncParams>): Response<OdooResponse>
}
