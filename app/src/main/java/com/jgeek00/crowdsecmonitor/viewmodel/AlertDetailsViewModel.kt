package com.jgeek00.crowdsecmonitor.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jgeek00.crowdsecmonitor.data.models.AlertDetailsResponse
import com.jgeek00.crowdsecmonitor.data.models.LoadingResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AlertDetailsViewModel @Inject constructor(
    private val sessionManager: SessionManager
) : ViewModel() {

    var state by mutableStateOf<LoadingResult<AlertDetailsResponse>>(LoadingResult.Loading)
        private set

    var isRefreshing by mutableStateOf(false)
        private set

    private var initializedForId: Int? = null

    fun initialize(alertId: Int) {
        if (initializedForId == alertId) return
        initializedForId = alertId
        fetchData(alertId)
    }

    fun refresh(alertId: Int) {
        viewModelScope.launch {
            isRefreshing = true
            fetchData(alertId)
            isRefreshing = false
        }
    }

    private fun fetchData(alertId: Int) {
        val apiClient = sessionManager.apiClient ?: return
        viewModelScope.launch {
            try {
                val result = apiClient.alerts.fetchAlertDetails(alertId)
                state = LoadingResult.Success(result.body)
            } catch (e: Exception) {
                state = LoadingResult.Failure(e)
            }
        }
    }
}

