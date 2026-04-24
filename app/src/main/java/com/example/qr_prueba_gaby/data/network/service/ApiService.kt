package com.example.qr_prueba_gaby.data.network.service

import com.example.qr_prueba_gaby.data.model.UserData
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Query

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

data class GateOpenRequest(
    val cedula: String,
    val fecha_hora: String
)

interface ApiService {
    @GET("api/sync_vehicular")
    suspend fun syncVehicular(@Query("cedula") cedula: String): Response<OdooResponse>

    @Headers("Content-Type: application/json")
    @POST("api/open_gate")
    suspend fun logGateOpen(@Body request: OdooRequest<GateOpenRequest>): Response<OdooResponse>
}
