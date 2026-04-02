package com.jgeek00.crowdsecmonitor.data.api

import com.jgeek00.crowdsecmonitor.data.models.AddBlocklistRequest
import com.jgeek00.crowdsecmonitor.data.models.BlocklistDataResponse
import com.jgeek00.crowdsecmonitor.data.models.BlocklistIpsResponse
import com.jgeek00.crowdsecmonitor.data.models.BlocklistsCheckDomainRequest
import com.jgeek00.crowdsecmonitor.data.models.BlocklistsCheckDomainResponse
import com.jgeek00.crowdsecmonitor.data.models.BlocklistsCheckIPsRequest
import com.jgeek00.crowdsecmonitor.data.models.BlocklistsCheckIPsResponse
import com.jgeek00.crowdsecmonitor.data.models.BlocklistsListResponse
import com.jgeek00.crowdsecmonitor.data.models.BlocklistsRequest
import com.jgeek00.crowdsecmonitor.data.models.EmptyResponse
import com.jgeek00.crowdsecmonitor.data.models.HttpClientException
import com.jgeek00.crowdsecmonitor.data.models.HttpResponse
import com.jgeek00.crowdsecmonitor.data.models.ToggleBlocklistRequest
import kotlinx.serialization.SerializationException
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import java.io.IOException

class BlocklistsApiClient internal constructor(private val httpClient: HttpClient) {

    private val service: Service by lazy { httpClient.retrofit.create(Service::class.java) }

    suspend fun fetchBlocklists(requestParams: BlocklistsRequest? = null): HttpResponse<BlocklistsListResponse> {
        return try {
            val response = service.fetchBlocklists(
                limit = requestParams?.limit,
                offset = requestParams?.offset
            )
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

    suspend fun fetchBlocklistData(blocklistId: Int): HttpResponse<BlocklistDataResponse> {
        return try {
            val response = service.fetchBlocklistData(blocklistId)
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

    suspend fun fetchBlocklistIps(blocklistId: Int): HttpResponse<BlocklistIpsResponse> {
        return try {
            val response = service.fetchBlocklistIps(blocklistId)
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

    suspend fun addBlocklist(body: AddBlocklistRequest): HttpResponse<EmptyResponse> {
        return try {
            val response = service.addBlocklist(body)
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

    suspend fun toggleBlocklist(blocklistId: Int, body: ToggleBlocklistRequest): HttpResponse<EmptyResponse> {
        return try {
            val response = service.toggleBlocklist(blocklistId, body)
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

    suspend fun deleteBlocklist(blocklistId: Int): HttpResponse<EmptyResponse> {
        return try {
            val response = service.deleteBlocklist(blocklistId)
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

    suspend fun checkIps(body: BlocklistsCheckIPsRequest): HttpResponse<BlocklistsCheckIPsResponse> {
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

    suspend fun checkDomain(body: BlocklistsCheckDomainRequest): HttpResponse<BlocklistsCheckDomainResponse> {
        return try {
            val response = service.checkDomain(body)
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
        @GET("api/v1/blocklists")
        suspend fun fetchBlocklists(
            @Query("limit") limit: Int?,
            @Query("offset") offset: Int?
        ): Response<BlocklistsListResponse>

        @GET("api/v1/blocklists/{id}?include_ips=ip_string")
        suspend fun fetchBlocklistData(
            @Path("id") blocklistId: Int
        ): Response<BlocklistDataResponse>

        @GET("api/v1/blocklists/{id}/ips?unpaged=true&ip_string=true")
        suspend fun fetchBlocklistIps(
            @Path("id") blocklistId: Int
        ): Response<BlocklistIpsResponse>

        @POST("api/v1/blocklists")
        suspend fun addBlocklist(
            @Body body: AddBlocklistRequest
        ): Response<EmptyResponse>

        @POST("api/v1/blocklists/{id}/enabled")
        suspend fun toggleBlocklist(
            @Path("id") blocklistId: Int,
            @Body body: ToggleBlocklistRequest
        ): Response<EmptyResponse>

        @DELETE("api/v1/blocklists/{id}")
        suspend fun deleteBlocklist(
            @Path("id") blocklistId: Int
        ): Response<EmptyResponse>

        @POST("api/v1/blocklists/check")
        suspend fun checkIps(
            @Body body: BlocklistsCheckIPsRequest
        ): Response<BlocklistsCheckIPsResponse>

        @POST("api/v1/blocklists/check-domain")
        suspend fun checkDomain(
            @Body body: BlocklistsCheckDomainRequest
        ): Response<BlocklistsCheckDomainResponse>
    }
}
