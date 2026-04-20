package com.jgeek00.crowdsecmonitor.data.api

import android.util.Base64
import com.jgeek00.crowdsecmonitor.data.db.CSServerModel
import com.jgeek00.crowdsecmonitor.data.models.ApiErrorResponse
import com.jgeek00.crowdsecmonitor.data.models.HttpClientException
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.io.IOException
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

internal class InvalidConnectionValuesIOException(cause: Throwable) :
    IOException("Invalid header value in connection settings: ${cause.message}", cause)

class HttpClient(private val server: CSServerModel) {

    val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    private val okHttpClient: OkHttpClient by lazy {
        getUnsafeOkHttpClient()
            .addInterceptor { chain ->
                val requestBuilder = chain.request().newBuilder()

                try {
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

                    server.customHeaders?.forEach { (key, value) ->
                        requestBuilder.addHeader(key, value)
                    }
                } catch (e: IllegalArgumentException) {
                    throw InvalidConnectionValuesIOException(e)
                }

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

    fun <T> handleHttpError(response: Response<T>): Nothing {
        if (response.code() == 401) {
            throw HttpClientException.Unauthorized()
        }

        val errorBody = response.errorBody()?.string().orEmpty()
        if (errorBody.isNotBlank()) {
            try {
                val apiError = json.decodeFromString<ApiErrorResponse>(errorBody)
                apiError.resolvedMessage?.let {
                    throw HttpClientException.HttpErrorWithMessage(response.code(), it)
                }
            } catch (_: Exception) {

            }
        }

        throw HttpClientException.HttpError(response.code())
    }

    private fun buildBaseUrl(): String {
        val portStr = if ((server.port ?: 0) > 0) ":${server.port}" else ""
        val pathStr = server.path?.let {
            if (it.startsWith("/")) it else "/$it"
        } ?: ""

        var url = "${server.http}://${server.domain}$portStr$pathStr"
        if (!url.endsWith("/")) url += "/"
        return url
    }

    // Don't validate SSL certificates
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

