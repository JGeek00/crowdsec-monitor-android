package com.jgeek00.crowdsecmonitor.repository

import com.jgeek00.crowdsecmonitor.data.api.CrowdSecApiClient
import com.jgeek00.crowdsecmonitor.data.models.ApiStatusResponse
import com.jgeek00.crowdsecmonitor.data.models.LoadingResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ServiceStatusRepository @Inject constructor() {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val _status = MutableStateFlow<LoadingResult<ApiStatusResponse>>(LoadingResult.Loading)
    val status: StateFlow<LoadingResult<ApiStatusResponse>> = _status.asStateFlow()

    private var webSocketJob: Job? = null

    fun start(apiClient: CrowdSecApiClient) {
        webSocketJob?.cancel()
        _status.value = LoadingResult.Loading

        scope.launch {
            try {
                val response = apiClient.checkApiStatus()
                _status.value = LoadingResult.Success(response.body)
                openWebSocket(apiClient)
            } catch (e: Exception) {
                _status.value = LoadingResult.Failure(e)
            }
        }
    }

    private fun openWebSocket(apiClient: CrowdSecApiClient) {
        if (webSocketJob?.isActive == true) return

        webSocketJob = scope.launch {
            try {
                apiClient.streamApiStatus().collect { statusUpdate ->
                    _status.value = LoadingResult.Success(statusUpdate)
                }
            } catch (e: Exception) {
                _status.value = LoadingResult.Failure(e)
            }
        }
    }

    fun stop(apiClient: CrowdSecApiClient?) {
        webSocketJob?.cancel()
        webSocketJob = null
        apiClient?.disconnectApiStatusStream()
    }

    fun openWebSocketManual(apiClient: CrowdSecApiClient) {
        openWebSocket(apiClient)
    }

    fun close() {
        webSocketJob?.cancel()
        webSocketJob = null
        scope.coroutineContext[Job]?.cancel()
    }
}

