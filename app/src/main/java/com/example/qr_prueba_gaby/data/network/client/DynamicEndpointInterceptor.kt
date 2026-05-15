package com.example.qr_prueba_gaby.data.network.client

import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Reescribe la URL de cada request usando el endpoint dinámico que entrega
 * [EndpointProvider] (proveniente del QR del administrador).
 *
 * - El `baseUrl` con el que se construye Retrofit es un placeholder técnico
 *   (`http://placeholder.invalid/`); jamás se envía a la red porque este
 *   interceptor lo reemplaza antes de salir.
 * - Si el usuario todavía no escaneó el QR del admin, se aborta con
 *   [IOException] para no enviar tráfico a un host inválido.
 */
@Singleton
class DynamicEndpointInterceptor @Inject constructor(
    private val endpointProvider: EndpointProvider
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val raw = endpointProvider.current()
            ?: throw IOException("Endpoint no provisionado. Escanee el QR del administrador.")

        val base = raw.toHttpUrlOrNull()
            ?: (if (raw.startsWith("http")) null else "http://$raw".toHttpUrlOrNull())
            ?: throw IOException("Endpoint inválido recibido del QR: [$raw]")

        val rewritten = original.url.newBuilder()
            .scheme(base.scheme)
            .host(base.host)
            .port(base.port)
            .build()

        return chain.proceed(original.newBuilder().url(rewritten).build())
    }
}
