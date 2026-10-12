package com.jgeek00.crowdsecmonitor.data.api

import com.jgeek00.crowdsecmonitor.data.models.ChannelTestResponse
import com.jgeek00.crowdsecmonitor.data.models.CreateChannelRequest
import com.jgeek00.crowdsecmonitor.data.models.DeleteNotificationChannelResponse
import com.jgeek00.crowdsecmonitor.data.models.HttpClientException
import com.jgeek00.crowdsecmonitor.data.models.HttpResponse
import com.jgeek00.crowdsecmonitor.data.models.NotificationChannelDetailResponse
import com.jgeek00.crowdsecmonitor.data.models.NotificationChannelsListResponse
import com.jgeek00.crowdsecmonitor.data.models.ProvidersListResponse
import com.jgeek00.crowdsecmonitor.data.models.TestChannelRequest
import com.jgeek00.crowdsecmonitor.data.models.TestInlineChannelRequest
import com.jgeek00.crowdsecmonitor.data.models.UpdateChannelRequest
import kotlinx.serialization.SerializationException
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import java.io.IOException

class NotificationChannelsApiClient internal constructor(private val httpClient: HttpClient) {

    private val service: Service by lazy { httpClient.retrofit.create(Service::class.java) }

    suspend fun fetchChannels(): HttpResponse<NotificationChannelsListResponse> {
        return try {
            val response = service.fetchChannels()
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

    suspend fun fetchProviders(): HttpResponse<ProvidersListResponse> {
        return try {
            val response = service.fetchProviders()
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

    suspend fun createChannel(body: CreateChannelRequest): HttpResponse<NotificationChannelDetailResponse> {
        return try {
            val response = service.createChannel(body)
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

    suspend fun fetchChannel(channelId: Int): HttpResponse<NotificationChannelDetailResponse> {
        return try {
            val response = service.fetchChannel(channelId)
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

    suspend fun updateChannel(channelId: Int, body: UpdateChannelRequest): HttpResponse<NotificationChannelDetailResponse> {
        return try {
            val response = service.updateChannel(channelId, body)
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

    suspend fun deleteChannel(channelId: Int): HttpResponse<DeleteNotificationChannelResponse> {
        return try {
            val response = service.deleteChannel(channelId)
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

    suspend fun testChannel(channelId: Int, body: TestChannelRequest): HttpResponse<ChannelTestResponse> {
        return try {
            val response = service.testChannel(channelId, body)
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

    suspend fun testInlineChannel(body: TestInlineChannelRequest): HttpResponse<ChannelTestResponse> {
        return try {
            val response = service.testInlineChannel(body)
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
        @GET("api/v1/notification-channels")
        suspend fun fetchChannels(): Response<NotificationChannelsListResponse>

        @GET("api/v1/notification-channels/providers")
        suspend fun fetchProviders(): Response<ProvidersListResponse>

        @POST("api/v1/notification-channels")
        suspend fun createChannel(
            @Body body: CreateChannelRequest
        ): Response<NotificationChannelDetailResponse>

        @GET("api/v1/notification-channels/{id}")
        suspend fun fetchChannel(
            @Path("id") channelId: Int
        ): Response<NotificationChannelDetailResponse>

        @PUT("api/v1/notification-channels/{id}")
        suspend fun updateChannel(
            @Path("id") channelId: Int,
            @Body body: UpdateChannelRequest
        ): Response<NotificationChannelDetailResponse>

        @DELETE("api/v1/notification-channels/{id}")
        suspend fun deleteChannel(
            @Path("id") channelId: Int
        ): Response<DeleteNotificationChannelResponse>

        @POST("api/v1/notification-channels/{id}/test")
        suspend fun testChannel(
            @Path("id") channelId: Int,
            @Body body: TestChannelRequest
        ): Response<ChannelTestResponse>

        @POST("api/v1/notification-channels/test")
        suspend fun testInlineChannel(
            @Body body: TestInlineChannelRequest
        ): Response<ChannelTestResponse>
    }
}
