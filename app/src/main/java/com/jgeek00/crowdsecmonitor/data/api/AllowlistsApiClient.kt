package com.jgeek00.crowdsecmonitor.data.api

import com.jgeek00.crowdsecmonitor.data.models.AllowlistsCheckIPsRequest
import com.jgeek00.crowdsecmonitor.data.models.AllowlistsCheckIPsResponse
import com.jgeek00.crowdsecmonitor.data.models.AllowlistsListResponse
import com.jgeek00.crowdsecmonitor.data.models.HttpClientException
import com.jgeek00.crowdsecmonitor.data.models.HttpResponse
import kotlinx.serialization.SerializationException
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import java.io.IOException

class AllowlistsApiClient internal constructor(private val httpClient: HttpClient) {

    private val service: Service by lazy { httpClient.retrofit.create(Service::class.java) }

    suspend fun fetchAllowlists(): HttpResponse<AllowlistsListResponse> {
        return try {
            val response = service.fetchAllowlists()
            val body = response.body()
            if (response.isSuccessful && body != null) {
                HttpResponse(successful = true, statusCode = response.code(), body = body)
            } else {
                httpClient.handleHttpError(response)
            }
        } catch (e: HttpClientException) {
            throw e
        } catch (e: SerializationException) {
            throw HttpClientException.DecodingError(e)
        } catch (e: IOException) {
            throw HttpClientException.NetworkError(e)
        } catch (e: Exception) {
            throw HttpClientException.NetworkError(e)
        }
    }

    suspend fun checkIps(body: AllowlistsCheckIPsRequest): HttpResponse<AllowlistsCheckIPsResponse> {
        return try {
            val response = service.checkIps(body)
            val responseBody = response.body()
            if (response.isSuccessful && responseBody != null) {
                HttpResponse(successful = true, statusCode = response.code(), body = responseBody)
            } else {
                httpClient.handleHttpError(response)
            }
        } catch (e: HttpClientException) {
            throw e
        } catch (e: SerializationException) {
            throw HttpClientException.DecodingError(e)
        } catch (e: IOException) {
            throw HttpClientException.NetworkError(e)
        } catch (e: Exception) {
            throw HttpClientException.NetworkError(e)
        }
    }

    private interface Service {
        @GET("api/v1/allowlists")
        suspend fun fetchAllowlists(): Response<AllowlistsListResponse>

        @POST("api/v1/allowlists/check")
        suspend fun checkIps(
            @Body body: AllowlistsCheckIPsRequest
        ): Response<AllowlistsCheckIPsResponse>
    }
}
