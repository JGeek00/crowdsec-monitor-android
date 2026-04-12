package com.jgeek00.crowdsecmonitor.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.compose.runtime.snapshotFlow
import com.jgeek00.crowdsecmonitor.data.models.ApiStatusResponse
import com.jgeek00.crowdsecmonitor.data.models.LoadingResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ServiceStatusViewModel @Inject constructor(
    private val sessionManager: SessionManager
) : ViewModel() {

    var status by mutableStateOf<LoadingResult<ApiStatusResponse>>(LoadingResult.Loading)
        private set

    private var webSocketJob: Job? = null

    init {
        viewModelScope.launch {
            snapshotFlow { sessionManager.apiClient }.collect { client ->
                closeWebSocket()
                if (client != null) fetchStatus() else status = LoadingResult.Loading
            }
        }
    }

    fun fetchStatus() {
        val apiClient = sessionManager.apiClient ?: return
        viewModelScope.launch {
            status = LoadingResult.Loading
            try {
                val response = apiClient.checkApiStatus()
                status = LoadingResult.Success(response.body)
                openWebSocket()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                status = LoadingResult.Failure(e)
            }
        }
    }

    fun openWebSocket() {
        val apiClient = sessionManager.apiClient ?: return
        if (webSocketJob?.isActive == true) return

        webSocketJob = viewModelScope.launch {
            try {
                apiClient.streamApiStatus().collect { statusUpdate ->
                    status = LoadingResult.Success(statusUpdate)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                status = LoadingResult.Failure(e)
            }
        }
    }

    fun closeWebSocket() {
        webSocketJob?.cancel()
        webSocketJob = null
        sessionManager.apiClient?.disconnectApiStatusStream()
    }

    override fun onCleared() {
        super.onCleared()
        closeWebSocket()
    }
}
