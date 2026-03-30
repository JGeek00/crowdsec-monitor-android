package com.jgeek00.crowdsecmonitor.data.api

import android.util.Base64
import com.jgeek00.crowdsecmonitor.data.models.CSServer
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

class CrowdSecApiClient(private val server: CSServer) {

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    private val okHttpClient: OkHttpClient by lazy {
        getUnsafeOkHttpClient()
            .addInterceptor { chain ->
                val requestBuilder = chain.request().newBuilder()
                
                // Configurar autenticación según el método
                when (server.authMethod) {
                    "basic" -> {
                        val credentials = "${server.basicUser}:${server.basicPassword}"
                        val base64 = Base64.encodeToString(credentials.toByteArray(), Base64.NO_WRAP)
                        requestBuilder.addHeader("Authorization", "Basic $base64")
                    }
                    "bearer" -> {
                        server.bearerToken?.let {
                            requestBuilder.addHeader("Authorization", "Bearer $it")
                        }
                    }
                }
                
                requestBuilder.addHeader("Content-Type", "application/json")
                chain.proceed(requestBuilder.build())
            }
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .build()
    }

    val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(buildBaseUrl())
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
    }

    private fun buildBaseUrl(): String {
        val portStr = if (server.port != null && server.port!! > 0) ":${server.port}" else ""
        val pathStr = server.path?.let { 
            if (it.startsWith("/")) it else "/$it" 
        } ?: ""
        
        // Asegurar que la URL termina en / para Retrofit
        var url = "${server.http}://${server.domain}$portStr$pathStr"
        if (!url.endsWith("/")) url += "/"
        return url
    }

    /**
     * Configura un cliente OkHttp que ignora la validación de certificados SSL.
     * Equivalente al URLSessionDelegate en Swift para bypass de SSL.
     */
    private fun getUnsafeOkHttpClient(): OkHttpClient.Builder {
        try {
            val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
                override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
                override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
                override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
            })

            val sslContext = SSLContext.getInstance("SSL")
            sslContext.init(null, trustAllCerts, SecureRandom())
            
            val builder = OkHttpClient.Builder()
            builder.sslSocketFactory(sslContext.socketFactory, trustAllCerts[0] as X509TrustManager)
            builder.hostnameVerifier { _, _ -> true }
            
            return builder
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }
}
