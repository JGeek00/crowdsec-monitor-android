package com.jgeek00.crowdsecmonitor.viewmodel

import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jgeek00.crowdsecmonitor.data.models.ApiStatusResponse
import com.jgeek00.crowdsecmonitor.data.models.LoadingResult
import com.jgeek00.crowdsecmonitor.repository.ServiceStatusRepository
import com.jgeek00.crowdsecmonitor.session.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ServiceStatusViewModel @Inject constructor(
    private val sessionManager: SessionManager,
    private val repository: ServiceStatusRepository
) : ViewModel() {

    val status: StateFlow<LoadingResult<ApiStatusResponse>> = repository.status

    init {
        viewModelScope.launch {
            snapshotFlow { sessionManager.apiClient }.collect { client ->
                if (client != null) repository.start(client) else repository.stop(client)
            }
        }
    }

    fun openWebSocket() {
        val apiClient = sessionManager.apiClient ?: return
        repository.openWebSocketManual(apiClient)
    }

    fun closeWebSocket() {
        repository.stop(sessionManager.apiClient)
    }
}
