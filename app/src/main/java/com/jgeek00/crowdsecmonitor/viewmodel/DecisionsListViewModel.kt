package com.jgeek00.crowdsecmonitor.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jgeek00.crowdsecmonitor.constants.Defaults
import com.jgeek00.crowdsecmonitor.data.models.DecisionsListResponse
import com.jgeek00.crowdsecmonitor.data.models.DecisionsRequest
import com.jgeek00.crowdsecmonitor.data.models.DecisionsRequestFilters
import com.jgeek00.crowdsecmonitor.data.models.DecisionsRequestPagination
import com.jgeek00.crowdsecmonitor.data.models.LoadingResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

private val defaultRequest = DecisionsRequest(
    filters = DecisionsRequestFilters(
        onlyActive = Defaults.SHOW_DEFAULT_ACTIVE_DECISIONS
    ),
    pagination = DecisionsRequestPagination(
        offset = 0,
        limit = Defaults.DECISIONS_AMOUNT_BATCH
    )
)

@HiltViewModel
class DecisionsListViewModel @Inject constructor(
    private val sessionManager: SessionManager
) : ViewModel() {

    init {
        viewModelScope.launch {
            sessionManager.decisionsRefreshEvent.collect {
                refreshDecisionsInternal()
            }
        }
    }

    var state by mutableStateOf<LoadingResult<DecisionsListResponse>>(LoadingResult.Loading)
        private set

    var isRefreshing by mutableStateOf(false)
        private set

    var isLoadingMore by mutableStateOf(false)
        private set

    var expiringDecisionProcess by mutableStateOf(false)
        private set

    var requestParams by mutableStateOf(defaultRequest)
        private set

    var filters by mutableStateOf(defaultRequest.filters)
        private set

    fun reset() {
        state = LoadingResult.Loading
        requestParams = defaultRequest
        filters = defaultRequest.filters
        expiringDecisionProcess = false
        isRefreshing = false
        isLoadingMore = false
    }

    private suspend fun fetchDecisions(showLoading: Boolean = false, params: DecisionsRequest? = null) {
        val apiClient = sessionManager.apiClient ?: return

        if (showLoading) {
            state = LoadingResult.Loading
        }

        try {
            val result = apiClient.decisions.fetchDecisions(params ?: requestParams)
            state = LoadingResult.Success(result.body)
        } catch (e: Exception) {
            state = LoadingResult.Failure(e)
        }
    }

    fun initialFetchDecisions() {
        if (state.data != null) return
        viewModelScope.launch {
            fetchDecisions(showLoading = true)
        }
    }

    fun refreshDecisions() {
        viewModelScope.launch {
            isRefreshing = true
            val req = requestParams.copy(pagination = defaultRequest.pagination)
            requestParams = req
            fetchDecisions(params = req)
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
            fetchDecisions(showLoading = true, params = req)
        }
    }

    fun fetchMore() {
        val apiClient = sessionManager.apiClient ?: return
        val data = state.data ?: return

        if ((data.pagination.page * Defaults.DECISIONS_AMOUNT_BATCH) >= data.pagination.total) return

        val previousItems = data.items
        val newOffset = data.pagination.page * Defaults.DECISIONS_AMOUNT_BATCH
        requestParams = requestParams.copy(
            pagination = requestParams.pagination.copy(offset = newOffset)
        )

        viewModelScope.launch {
            try {
                isLoadingMore = true
                val result = apiClient.decisions.fetchDecisions(requestParams)
                val existingIds = previousItems.map { it.id }.toHashSet()
                val uniqueNewItems = result.body.items.filter { it.id !in existingIds }
                val mergedItems = previousItems + uniqueNewItems
                val newResponse = DecisionsListResponse(
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

    fun updateFilters(newFilters: DecisionsRequestFilters) {
        filters = newFilters
    }

    fun resetFilters() {
        filters = defaultRequest.filters
        requestParams = requestParams.copy(filters = defaultRequest.filters)
        viewModelScope.launch {
            fetchDecisions(showLoading = true, params = defaultRequest)
        }
    }

    fun resetFiltersPanelToAppliedOnes() {
        filters = requestParams.filters
    }

    fun expireDecision(decisionId: Int, onResult: (Boolean) -> Unit) {
        val apiClient = sessionManager.apiClient ?: run { onResult(false); return }
        viewModelScope.launch {
            expiringDecisionProcess = true
            try {
                apiClient.decisions.deleteDecision(decisionId)
                refreshDecisionsInternal()
                expiringDecisionProcess = false
                onResult(true)
            } catch (_: Exception) {
                expiringDecisionProcess = false
                onResult(false)
            }
        }
    }

    private suspend fun refreshDecisionsInternal() {
        val req = requestParams.copy(pagination = defaultRequest.pagination)
        requestParams = req
        fetchDecisions(params = req)
    }
}


