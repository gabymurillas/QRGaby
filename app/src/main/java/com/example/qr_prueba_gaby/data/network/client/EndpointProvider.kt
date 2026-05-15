package com.example.qr_prueba_gaby.data.network.client

import com.example.qr_prueba_gaby.data.pref.UserDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Mantiene en memoria el endpoint actual del backend, sincronizado en vivo con
 * [UserDataStore.urlEndpointFlow]. El valor se actualiza automáticamente cuando
 * el usuario escanea el QR del administrador (que invoca
 * [UserDataStore.saveProvisioningData]).
 *
 * El [DynamicEndpointInterceptor] consulta este provider de forma síncrona en
 * cada request HTTP — por eso necesitamos un caché en memoria y no leer
 * DataStore en el hilo de OkHttp.
 */
@Singleton
class EndpointProvider @Inject constructor(
    dataStore: UserDataStore
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _endpoint = MutableStateFlow<String?>(null)
    val endpoint: StateFlow<String?> = _endpoint.asStateFlow()

    init {
        scope.launch {
            dataStore.urlEndpointFlow.collect { value ->
                _endpoint.value = value?.takeIf { it.isNotBlank() }
            }
        }
    }

    fun current(): String? = _endpoint.value
}
