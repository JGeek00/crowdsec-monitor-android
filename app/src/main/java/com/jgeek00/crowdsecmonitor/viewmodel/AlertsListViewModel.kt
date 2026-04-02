package com.jgeek00.crowdsecmonitor.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jgeek00.crowdsecmonitor.constants.Defaults
import com.jgeek00.crowdsecmonitor.data.models.AlertsListResponse
import com.jgeek00.crowdsecmonitor.data.models.AlertsRequest
import com.jgeek00.crowdsecmonitor.data.models.AlertsRequestFilters
import com.jgeek00.crowdsecmonitor.data.models.AlertsRequestPagination
import com.jgeek00.crowdsecmonitor.data.models.LoadingResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

private val defaultRequest = AlertsRequest(
    filters = AlertsRequestFilters(
        countries = emptyList(),
        scenarios = emptyList(),
        ipOwners = emptyList(),
        targets = emptyList()
    ),
    pagination = AlertsRequestPagination(
        offset = 0,
        limit = Defaults.ALERTS_AMOUNT_BATCH
    )
)

@HiltViewModel
class AlertsListViewModel @Inject constructor(
    private val sessionManager: SessionManager
) : ViewModel() {

    init {
        viewModelScope.launch {
            sessionManager.alertsRefreshEvent.collect {
                refreshAlertsInternal()
            }
        }
    }

    var state by mutableStateOf<LoadingResult<AlertsListResponse>>(LoadingResult.Loading)
        private set

    var isRefreshing by mutableStateOf(false)
        private set

    var isLoadingMore by mutableStateOf(false)
        private set

    var deletingAlertProcess by mutableStateOf(false)
        private set

    var selectedAlert by mutableStateOf<Int?>(null)
        private set

    var requestParams by mutableStateOf(defaultRequest)
        private set

    var filters by mutableStateOf(defaultRequest.filters)
        private set

    fun reset() {
        state = LoadingResult.Loading
        requestParams = defaultRequest
        filters = defaultRequest.filters
        selectedAlert = null
        deletingAlertProcess = false
        isRefreshing = false
        isLoadingMore = false
    }

    private suspend fun fetchAlerts(showLoading: Boolean = false, params: AlertsRequest? = null) {
        val apiClient = sessionManager.apiClient ?: return

        if (showLoading) {
            state = LoadingResult.Loading
        }

        try {
            val result = apiClient.alerts.fetchAlerts(params ?: requestParams)
            state = LoadingResult.Success(result.body)
        } catch (e: Exception) {
            state = LoadingResult.Failure(e)
        }
    }

    fun initialFetchAlerts() {
        if (state.data != null) return
        viewModelScope.launch {
            fetchAlerts(showLoading = true)
        }
    }

    fun refreshAlerts() {
        viewModelScope.launch {
            isRefreshing = true
            val req = requestParams.copy(pagination = defaultRequest.pagination)
            requestParams = req
            fetchAlerts(params = req)
            isRefreshing = false
        }
    }

    fun applyFilters() {
        val req = requestParams.copy(
            pagination = defaultRequest.pagination,
            filters = filters
        )
        requestParams = req
        viewModelScope.launch {
            fetchAlerts(showLoading = true, params = req)
        }
    }

    fun fetchMore() {
        val apiClient = sessionManager.apiClient ?: return
        val data = state.data ?: return

        if ((data.pagination.page * Defaults.ALERTS_AMOUNT_BATCH) >= data.pagination.total) return

        val previousItems = data.items
        val newOffset = data.pagination.page * Defaults.ALERTS_AMOUNT_BATCH
        requestParams = requestParams.copy(
            pagination = requestParams.pagination.copy(offset = newOffset)
        )

        viewModelScope.launch {
            try {
                isLoadingMore = true
                val result = apiClient.alerts.fetchAlerts(requestParams)
                val existingIds = previousItems.map { it.id }.toHashSet()
                val uniqueNewItems = result.body.items.filter { it.id !in existingIds }
                val mergedItems = previousItems + uniqueNewItems
                val newResponse = AlertsListResponse(
                    filtering = result.body.filtering,
                    items = mergedItems,
                    pagination = result.body.pagination
                )
                state = LoadingResult.Success(newResponse)
            } catch (e: Exception) {
                state = LoadingResult.Failure(e)
            } finally {
                isLoadingMore = false
            }
        }
    }

    fun updateFilters(newFilters: AlertsRequestFilters) {
        filters = newFilters
    }

    fun resetFilters() {
        filters = defaultRequest.filters
        requestParams = requestParams.copy(filters = defaultRequest.filters)
        viewModelScope.launch {
            fetchAlerts(showLoading = true, params = defaultRequest)
        }
    }

    fun resetFiltersPanelToAppliedOnes() {
        filters = requestParams.filters
    }

    fun selectAlert(alertId: Int?) {
        selectedAlert = alertId
    }

    fun deleteAlert(alertId: Int, onResult: (Boolean) -> Unit) {
        val apiClient = sessionManager.apiClient ?: run { onResult(false); return }
        viewModelScope.launch {
            deletingAlertProcess = true
            try {
                apiClient.alerts.deleteAlert(alertId)
                if (selectedAlert == alertId) {
                    selectedAlert = null
                }

                // Refresh alerts and decisions in parallel
                refreshAlertsInternal()
                sessionManager.triggerDecisionsRefresh()

                deletingAlertProcess = false
                onResult(true)
            } catch (_: Exception) {
                deletingAlertProcess = false
                onResult(false)
            }
        }
    }

    private suspend fun refreshAlertsInternal() {
        val req = requestParams.copy(pagination = defaultRequest.pagination)
        requestParams = req
        fetchAlerts(params = req)
    }
}


