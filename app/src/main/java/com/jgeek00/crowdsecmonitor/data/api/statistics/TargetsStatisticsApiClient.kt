package com.jgeek00.crowdsecmonitor.data.api.statistics

import com.jgeek00.crowdsecmonitor.data.api.HttpClient
import com.jgeek00.crowdsecmonitor.data.models.HttpClientException
import com.jgeek00.crowdsecmonitor.data.models.HttpResponse
import com.jgeek00.crowdsecmonitor.data.models.TopTarget
import kotlinx.serialization.SerializationException
import retrofit2.Response
import retrofit2.http.GET
import java.io.IOException

class TargetsStatisticsApiClient internal constructor(private val httpClient: HttpClient) {

    private val service: Service by lazy { httpClient.retrofit.create(Service::class.java) }

    suspend fun fetchTargetsStatistics(): HttpResponse<List<TopTarget>> {
        return try {
            val response = service.fetchTargetsStatistics()
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
        @GET("api/v1/statistics/targets")
        suspend fun fetchTargetsStatistics(): Response<List<TopTarget>>
    }
}