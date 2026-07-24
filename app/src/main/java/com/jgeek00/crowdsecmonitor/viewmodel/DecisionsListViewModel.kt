package com.jgeek00.crowdsecmonitor.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jgeek00.crowdsecmonitor.constants.Defaults
import com.jgeek00.crowdsecmonitor.data.models.DecisionsByIPResponse
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
import com.jgeek00.crowdsecmonitor.session.SessionManager

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

    var state by mutableStateOf<LoadingResult<DecisionsListResponse>>(LoadingResult.Loading)
        private set

    var stateByIP by mutableStateOf<LoadingResult<DecisionsByIPResponse>>(LoadingResult.Loading)
        private set

    val isGroupedByIP: Boolean get() = requestParams.filters.groupByIP == true

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

    init {
        viewModelScope.launch {
            val showOnlyActive = preferencesRepository.showDefaultActiveDecisions.first()
            val showDefaultGroupedByIP = preferencesRepository.showDefaultDecisionsGroupedByIP.first()
            defaultRequest = buildDefaultRequest(showOnlyActive).copy(
                filters = defaultRequest.filters.copy(groupByIP = showDefaultGroupedByIP)
            )
            filters = defaultRequest.filters
        }
        viewModelScope.launch {
            snapshotFlow { sessionManager.apiClient }.collect { client ->
                resetWithPreference()
                if (client != null) fetchDecisions(showLoading = true)
            }
        }
        viewModelScope.launch {
            preferencesRepository.showDefaultActiveDecisions.collect { showOnlyActive ->
                defaultRequest = buildDefaultRequest(showOnlyActive).copy(
                    filters = defaultRequest.filters.copy(groupByIP = defaultRequest.filters.groupByIP)
                )
                filters = defaultRequest.filters
            }
        }
        viewModelScope.launch {
            preferencesRepository.showDefaultDecisionsGroupedByIP.collect { showDefaultGroupedByIP ->
                defaultRequest = defaultRequest.copy(
                    filters = defaultRequest.filters.copy(groupByIP = showDefaultGroupedByIP)
                )
                filters = defaultRequest.filters
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

    private suspend fun resetWithPreference() {
        val showOnlyActive = preferencesRepository.showDefaultActiveDecisions.first()
        val showDefaultGroupedByIP = preferencesRepository.showDefaultDecisionsGroupedByIP.first()
        defaultRequest = buildDefaultRequest(showOnlyActive).copy(
            filters = defaultRequest.filters.copy(groupByIP = showDefaultGroupedByIP)
        )
        state = LoadingResult.Loading
        stateByIP = LoadingResult.Loading
        requestParams = defaultRequest
        filters = defaultRequest.filters
        expiringDecisionProcess = false
        isRefreshing = false
        isLoadingMore = false
    }

    fun reset() {
        state = LoadingResult.Loading
        stateByIP = LoadingResult.Loading
        requestParams = defaultRequest
        filters = defaultRequest.filters
        expiringDecisionProcess = false
        isRefreshing = false
        isLoadingMore = false
    }

    private suspend fun fetchDecisions(showLoading: Boolean = false, params: DecisionsRequest? = null) {
        val apiClient = sessionManager.apiClient ?: return
        val request = params ?: requestParams

        if (showLoading) {
            if (request.filters.groupByIP == true) {
                stateByIP = LoadingResult.Loading
            } else {
                state = LoadingResult.Loading
            }
        }

        try {
            if (request.filters.groupByIP == true) {
                val result = apiClient.decisions.fetchDecisionsByIP(
                    onlyActive = request.filters.onlyActive,
                    offset = request.pagination.offset,
                    limit = request.pagination.limit
                )
                stateByIP = LoadingResult.Success(result.body)
            } else {
                val result = apiClient.decisions.fetchDecisions(request)
                state = LoadingResult.Success(result.body)
            }
        } catch (e: Exception) {
            if (request.filters.groupByIP == true) {
                stateByIP = LoadingResult.Failure(e)
            } else {
                state = LoadingResult.Failure(e)
            }
        }
    }

    fun initialFetchDecisions() {
        if (isGroupedByIP) {
            if (stateByIP.data != null || stateByIP.isLoading) return
        } else {
            if (state.data != null || state.isLoading) return
        }
        viewModelScope.launch {
            resetWithPreference()
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

        if (isGroupedByIP) {
            val data = stateByIP.data ?: return
            if ((data.pagination.page * Defaults.DECISIONS_AMOUNT_BATCH) >= data.pagination.total) return

            val previousGroups = data.groups
            val newOffset = data.pagination.page * Defaults.DECISIONS_AMOUNT_BATCH
            requestParams = requestParams.copy(
                pagination = requestParams.pagination.copy(offset = newOffset)
            )

            viewModelScope.launch {
                try {
                    isLoadingMore = true
                    val result = apiClient.decisions.fetchDecisionsByIP(
                        onlyActive = requestParams.filters.onlyActive,
                        offset = requestParams.pagination.offset,
                        limit = requestParams.pagination.limit
                    )
                    val existingIps = previousGroups.map { it.ip }.toHashSet()
                    val uniqueNewGroups = result.body.groups.filter { it.ip !in existingIps }
                    val mergedGroups = previousGroups + uniqueNewGroups
                    val newResponse = DecisionsByIPResponse(
                        filtering = result.body.filtering,
                        groups = mergedGroups,
                        pagination = result.body.pagination
                    )
                    stateByIP = LoadingResult.Success(newResponse)
                } catch (e: Exception) {
                    stateByIP = LoadingResult.Failure(e)
                } finally {
                    isLoadingMore = false
                }
            }
        } else {
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
    }

    fun updateFilters(newFilters: DecisionsRequestFilters) {
        filters = newFilters
    }

    fun resetFilters() {
        viewModelScope.launch {
            val showOnlyActive = preferencesRepository.showDefaultActiveDecisions.first()
            val showDefaultGroupedByIP = preferencesRepository.showDefaultDecisionsGroupedByIP.first()
            val req = buildDefaultRequest(showOnlyActive).copy(
                filters = defaultRequest.filters.copy(groupByIP = showDefaultGroupedByIP)
            )
            defaultRequest = req
            filters = defaultRequest.filters
            requestParams = requestParams.copy(filters = defaultRequest.filters)
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
