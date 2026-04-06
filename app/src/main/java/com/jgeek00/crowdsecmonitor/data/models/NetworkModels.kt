package com.jgeek00.crowdsecmonitor.data.models

import kotlinx.serialization.Serializable

@Serializable
data class HttpResponse<T>(
    val successful: Boolean,
    val statusCode: Int,
    val body: T
)

@Serializable
data class ApiErrorResponse(
    val message: String? = null,
    val errors: List<String>? = null
) {
    val resolvedMessage: String?
        get() = message ?: errors?.firstOrNull()
}

@Serializable
class EmptyResponse

sealed class LoadingResult<out T> {
    data object Loading : LoadingResult<Nothing>()
    data class Success<T>(val value: T) : LoadingResult<T>()
    data class Failure(val throwable: Throwable) : LoadingResult<Nothing>()

    val isLoading: Boolean get() = this is Loading

    val data: T? get() = (this as? Success)?.value

    val error: Throwable? get() = (this as? Failure)?.throwable
}

sealed class HttpClientException(message: String? = null, cause: Throwable? = null) : Exception(message, cause) {
    class InvalidResponse : HttpClientException("Invalid response from server")
    class Unauthorized : HttpClientException("Unauthorized access")
    class InvalidConnectionValues : HttpClientException("One or more connection values contain invalid characters")
    data class HttpError(val statusCode: Int) : HttpClientException("HTTP Error: $statusCode")
    data class HttpErrorWithMessage(val statusCode: Int, override val message: String) : HttpClientException(message)
    data class NetworkError(val throwable: Throwable) : HttpClientException(cause = throwable)
    data class DecodingError(val throwable: Throwable) : HttpClientException(cause = throwable)
}
