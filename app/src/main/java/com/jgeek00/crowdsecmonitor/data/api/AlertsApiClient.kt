package com.jgeek00.crowdsecmonitor.data.api

import com.jgeek00.crowdsecmonitor.data.models.AlertDetailsResponse
import com.jgeek00.crowdsecmonitor.data.models.AlertsListResponse
import com.jgeek00.crowdsecmonitor.data.models.AlertsRequest
import com.jgeek00.crowdsecmonitor.data.models.EmptyResponse
import com.jgeek00.crowdsecmonitor.data.models.HttpClientException
import com.jgeek00.crowdsecmonitor.data.models.HttpResponse
import kotlinx.serialization.SerializationException
import retrofit2.Response
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import java.io.IOException

class AlertsApiClient internal constructor(private val httpClient: HttpClient) {

    private val service: Service by lazy { httpClient.retrofit.create(Service::class.java) }

    suspend fun fetchAlerts(requestParams: AlertsRequest): HttpResponse<AlertsListResponse> {
        return try {
            val countries = requestParams.filters.countries.ifEmpty { null }
            val scenarios = requestParams.filters.scenarios.ifEmpty { null }
            val ipOwners = requestParams.filters.ipOwners.ifEmpty { null }
            val targets = requestParams.filters.targets.ifEmpty { null }

            val response = service.fetchAlerts(
                countries = countries,
                scenarios = scenarios,
                ipOwners = ipOwners,
                targets = targets,
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

    suspend fun fetchAlertDetails(alertId: Int): HttpResponse<AlertDetailsResponse> {
        return try {
            val response = service.fetchAlertDetails(alertId)
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

    suspend fun deleteAlert(alertId: Int): HttpResponse<EmptyResponse> {
        return try {
            val response = service.deleteAlert(alertId)
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
        @GET("api/v1/alerts")
        suspend fun fetchAlerts(
            @Query("country") countries: List<String>?,
            @Query("scenario") scenarios: List<String>?,
            @Query("ipOwner") ipOwners: List<String>?,
            @Query("target") targets: List<String>?,
            @Query("offset") offset: Int,
            @Query("limit") limit: Int
        ): Response<AlertsListResponse>

        @GET("api/v1/alerts/{id}")
        suspend fun fetchAlertDetails(
            @Path("id") alertId: Int
        ): Response<AlertDetailsResponse>

        @DELETE("api/v1/alerts/{id}")
        suspend fun deleteAlert(
            @Path("id") alertId: Int
        ): Response<EmptyResponse>
    }
}
