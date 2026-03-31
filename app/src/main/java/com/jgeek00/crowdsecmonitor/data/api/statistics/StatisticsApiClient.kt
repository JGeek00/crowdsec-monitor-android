package com.jgeek00.crowdsecmonitor.data.api.statistics

import com.jgeek00.crowdsecmonitor.data.api.HttpClient
import com.jgeek00.crowdsecmonitor.data.api.statistics.TargetsStatisticsApiClient
import com.jgeek00.crowdsecmonitor.data.models.HttpClientException
import com.jgeek00.crowdsecmonitor.data.models.HttpResponse
import com.jgeek00.crowdsecmonitor.data.models.StatisticsResponse
import kotlinx.serialization.SerializationException
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query
import java.io.IOException

class StatisticsApiClient internal constructor(private val httpClient: HttpClient) {

    val countries: CountriesStatisticsApiClient by lazy { CountriesStatisticsApiClient(httpClient) }
    val ipOwners: IpOwnersStatisticsApiClient by lazy { IpOwnersStatisticsApiClient(httpClient) }
    val scenarios: ScenariosStatisticsApiClient by lazy { ScenariosStatisticsApiClient(httpClient) }
    val targets: TargetsStatisticsApiClient by lazy { TargetsStatisticsApiClient(httpClient) }

    private val service: Service by lazy { httpClient.retrofit.create(Service::class.java) }

    suspend fun fetchStatistics(amount: Int? = null, since: String? = null): HttpResponse<StatisticsResponse> {
        return try {
            val response = service.fetchStatistics(amount, since)
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
        @GET("api/v1/statistics")
        suspend fun fetchStatistics(
            @Query("amount") amount: Int?,
            @Query("since") since: String?
        ): Response<StatisticsResponse>
    }
}