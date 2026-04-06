package com.jgeek00.crowdsecmonitor.data.api

import com.jgeek00.crowdsecmonitor.data.api.statistics.StatisticsApiClient
import com.jgeek00.crowdsecmonitor.data.db.CSServerModel
import com.jgeek00.crowdsecmonitor.data.models.ApiStatusResponse
import com.jgeek00.crowdsecmonitor.data.models.HttpClientException
import com.jgeek00.crowdsecmonitor.data.models.HttpResponse
import kotlinx.serialization.SerializationException
import retrofit2.Response
import retrofit2.http.GET
import java.io.IOException

class CrowdSecApiClient(server: CSServerModel) {

    private val httpClient = HttpClient(server)

    val statistics: StatisticsApiClient by lazy { StatisticsApiClient(httpClient) }
    val alerts: AlertsApiClient by lazy { AlertsApiClient(httpClient) }
    val decisions: DecisionsApiClient by lazy { DecisionsApiClient(httpClient) }
    val allowlists: AllowlistsApiClient by lazy { AllowlistsApiClient(httpClient) }
    val blocklists: BlocklistsApiClient by lazy { BlocklistsApiClient(httpClient) }

    private val statusService: StatusService by lazy { httpClient.retrofit.create(StatusService::class.java) }

    suspend fun checkApiStatus(): HttpResponse<ApiStatusResponse> {
        return try {
            val response = statusService.checkApiStatus()
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
        } catch (_: InvalidConnectionValuesIOException) {
            throw HttpClientException.InvalidConnectionValues()
        } catch (e: IOException) {
            throw HttpClientException.NetworkError(e)
        } catch (e: Exception) {
            throw HttpClientException.NetworkError(e)
        }
    }
}

private interface StatusService {
    @GET("api/v1/status")
    suspend fun checkApiStatus(): Response<ApiStatusResponse>
}

