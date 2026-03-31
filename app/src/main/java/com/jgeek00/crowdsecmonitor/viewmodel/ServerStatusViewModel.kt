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
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ServerStatusViewModel @Inject constructor(
    private val sessionManager: SessionManager
) : ViewModel() {

    var status by mutableStateOf<LoadingResult<ApiStatusResponse>>(LoadingResult.Loading)
        private set

    init {
        viewModelScope.launch {
            snapshotFlow { sessionManager.apiClient }.collect { client ->
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
            } catch (e: Exception) {
                status = LoadingResult.Failure(e)
            }
        }
    }
}
