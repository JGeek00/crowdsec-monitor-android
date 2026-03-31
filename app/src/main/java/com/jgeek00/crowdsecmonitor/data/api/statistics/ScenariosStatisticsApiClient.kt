package com.jgeek00.crowdsecmonitor.data.api.statistics

import com.jgeek00.crowdsecmonitor.data.api.HttpClient
import com.jgeek00.crowdsecmonitor.data.models.HttpClientException
import com.jgeek00.crowdsecmonitor.data.models.HttpResponse
import com.jgeek00.crowdsecmonitor.data.models.TopScenario
import kotlinx.serialization.SerializationException
import retrofit2.Response
import retrofit2.http.GET
import java.io.IOException

class ScenariosStatisticsApiClient internal constructor(private val httpClient: HttpClient) {

    private val service: Service by lazy { httpClient.retrofit.create(Service::class.java) }

    suspend fun fetchScenariosStatistics(): HttpResponse<List<TopScenario>> {
        return try {
            val response = service.fetchScenariosStatistics()
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
        @GET("api/v1/statistics/scenarios")
        suspend fun fetchScenariosStatistics(): Response<List<TopScenario>>
    }
}