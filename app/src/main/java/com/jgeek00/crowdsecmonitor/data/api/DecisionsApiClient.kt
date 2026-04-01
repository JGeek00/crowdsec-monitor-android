package com.jgeek00.crowdsecmonitor.data.api

import com.jgeek00.crowdsecmonitor.data.models.CreateDecisionRequest
import com.jgeek00.crowdsecmonitor.data.models.DecisionItemResponse
import com.jgeek00.crowdsecmonitor.data.models.DecisionsListResponse
import com.jgeek00.crowdsecmonitor.data.models.DecisionsRequest
import com.jgeek00.crowdsecmonitor.data.models.EmptyResponse
import com.jgeek00.crowdsecmonitor.data.models.HttpClientException
import com.jgeek00.crowdsecmonitor.data.models.HttpResponse
import kotlinx.serialization.SerializationException
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import java.io.IOException

class DecisionsApiClient internal constructor(private val httpClient: HttpClient) {

    private val service: Service by lazy { httpClient.retrofit.create(Service::class.java) }

    suspend fun fetchDecisions(requestParams: DecisionsRequest): HttpResponse<DecisionsListResponse> {
        return try {
            val response = service.fetchDecisions(
                onlyActive = requestParams.filters.onlyActive,
                offset = requestParams.pagination.offset,
                limit = requestParams.pagination.limit
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

    suspend fun fetchDecisionDetails(decisionId: Int): HttpResponse<DecisionItemResponse> {
        return try {
            val response = service.fetchDecisionDetails(decisionId)
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

    suspend fun createDecision(body: CreateDecisionRequest): HttpResponse<EmptyResponse> {
        return try {
            val response = service.createDecision(body)
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

    suspend fun deleteDecision(decisionId: Int): HttpResponse<EmptyResponse> {
        return try {
            val response = service.deleteDecision(decisionId)
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

    private interface Service {
        @GET("api/v1/decisions")
        suspend fun fetchDecisions(
            @Query("only_active") onlyActive: Boolean?,
            @Query("offset") offset: Int,
            @Query("limit") limit: Int
        ): Response<DecisionsListResponse>

        @GET("api/v1/decisions/{id}")
        suspend fun fetchDecisionDetails(
            @Path("id") decisionId: Int
        ): Response<DecisionItemResponse>

        @POST("api/v1/decisions")
        suspend fun createDecision(
            @Body body: CreateDecisionRequest
        ): Response<EmptyResponse>

        @DELETE("api/v1/decisions/{id}")
        suspend fun deleteDecision(
            @Path("id") decisionId: Int
        ): Response<EmptyResponse>
    }
}
