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
import com.jgeek00.crowdsecmonitor.data.repository.PreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

private fun buildDefaultRequest(showOnlyActive: Boolean) = DecisionsRequest(
    filters = DecisionsRequestFilters(onlyActive = showOnlyActive),
    pagination = DecisionsRequestPagination(offset = 0, limit = Defaults.DECISIONS_AMOUNT_BATCH)
)

@HiltViewModel
class DecisionsListViewModel @Inject constructor(
    private val sessionManager: SessionManager,
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    private var defaultRequest = buildDefaultRequest(Defaults.SHOW_DEFAULT_ACTIVE_DECISIONS)

    init {
        // Observe preference changes so that reset/resetFilters always use the current default
        viewModelScope.launch {
            preferencesRepository.showDefaultActiveDecisions.collect { showOnlyActive ->
                defaultRequest = buildDefaultRequest(showOnlyActive)
            }
        }
        viewModelScope.launch {
            preferencesRepository.disableDecisionTimerAnimation.collect { value ->
                disableDecisionTimerAnimation = value
            }
        }
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

    var disableDecisionTimerAnimation by mutableStateOf(Defaults.DISABLE_DECISION_TIMER_ANIMATION)
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
            // Use first() so we wait for the preference before fetching (eliminates the race condition)
            val showOnlyActive = preferencesRepository.showDefaultActiveDecisions.first()
            val req = buildDefaultRequest(showOnlyActive)
            defaultRequest = req
            requestParams = req
            filters = req.filters
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
