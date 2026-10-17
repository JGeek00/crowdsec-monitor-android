package com.jgeek00.crowdsecmonitor.data.api

import com.jgeek00.crowdsecmonitor.data.models.CreateNotificationRequest
import com.jgeek00.crowdsecmonitor.data.models.DeleteNotificationResponse
import com.jgeek00.crowdsecmonitor.data.models.HttpClientException
import com.jgeek00.crowdsecmonitor.data.models.HttpResponse
import com.jgeek00.crowdsecmonitor.data.models.NotificationDetailResponse
import com.jgeek00.crowdsecmonitor.data.models.NotificationHistoryResponse
import com.jgeek00.crowdsecmonitor.data.models.NotificationsListResponse
import com.jgeek00.crowdsecmonitor.data.models.ToggleNotificationRequest
import com.jgeek00.crowdsecmonitor.data.models.UpdateNotificationRequest
import kotlinx.serialization.SerializationException
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import java.io.IOException

class NotificationsApiClient internal constructor(private val httpClient: HttpClient) {

    private val service: Service by lazy { httpClient.retrofit.create(Service::class.java) }

    suspend fun fetchNotifications(): HttpResponse<NotificationsListResponse> {
        return try {
            val response = service.fetchNotifications()
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

    suspend fun createNotification(body: CreateNotificationRequest): HttpResponse<NotificationDetailResponse> {
        return try {
            val response = service.createNotification(body)
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

    suspend fun fetchNotification(notificationId: Int): HttpResponse<NotificationDetailResponse> {
        return try {
            val response = service.fetchNotification(notificationId)
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

    suspend fun updateNotification(notificationId: Int, body: UpdateNotificationRequest): HttpResponse<NotificationDetailResponse> {
        return try {
            val response = service.updateNotification(notificationId, body)
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

    suspend fun deleteNotification(notificationId: Int): HttpResponse<DeleteNotificationResponse> {
        return try {
            val response = service.deleteNotification(notificationId)
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

    suspend fun toggleNotification(notificationId: Int, body: ToggleNotificationRequest): HttpResponse<NotificationDetailResponse> {
        return try {
            val response = service.toggleNotification(notificationId, body)
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

    suspend fun fetchHistory(): HttpResponse<NotificationHistoryResponse> {
        return try {
            val response = service.fetchHistory()
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
        @GET("api/v1/notifications")
        suspend fun fetchNotifications(): Response<NotificationsListResponse>

        @POST("api/v1/notifications")
        suspend fun createNotification(
            @Body body: CreateNotificationRequest
        ): Response<NotificationDetailResponse>

        @GET("api/v1/notifications/{id}")
        suspend fun fetchNotification(
            @Path("id") notificationId: Int
        ): Response<NotificationDetailResponse>

        @PUT("api/v1/notifications/{id}")
        suspend fun updateNotification(
            @Path("id") notificationId: Int,
            @Body body: UpdateNotificationRequest
        ): Response<NotificationDetailResponse>

        @DELETE("api/v1/notifications/{id}")
        suspend fun deleteNotification(
            @Path("id") notificationId: Int
        ): Response<DeleteNotificationResponse>

        @POST("api/v1/notifications/{id}/enabled")
        suspend fun toggleNotification(
            @Path("id") notificationId: Int,
            @Body body: ToggleNotificationRequest
        ): Response<NotificationDetailResponse>

        @GET("api/v1/notifications/history")
        suspend fun fetchHistory(): Response<NotificationHistoryResponse>
    }
}
