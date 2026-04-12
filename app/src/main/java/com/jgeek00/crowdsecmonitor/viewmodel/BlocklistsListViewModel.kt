package com.jgeek00.crowdsecmonitor.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jgeek00.crowdsecmonitor.constants.Defaults
import com.jgeek00.crowdsecmonitor.data.models.BlocklistsListResponse
import com.jgeek00.crowdsecmonitor.data.models.BlocklistsRequest
import com.jgeek00.crowdsecmonitor.data.models.LoadingResult
import com.jgeek00.crowdsecmonitor.data.models.ToggleBlocklistRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.jgeek00.crowdsecmonitor.session.SessionManager

private val defaultRequest = BlocklistsRequest(
    offset = 0,
    limit = Defaults.BLOCKLISTS_AMOUNT_BATCH
)

@HiltViewModel
class BlocklistsListViewModel @Inject constructor(
    private val sessionManager: SessionManager
) : ViewModel() {

    var state by mutableStateOf<LoadingResult<BlocklistsListResponse>>(LoadingResult.Loading)
        private set

    var requestParams by mutableStateOf(defaultRequest)
        private set

    var selectedListName by mutableStateOf<String?>(null)
        private set

    var isRefreshing by mutableStateOf(false)
        private set

    var isLoadingMore by mutableStateOf(false)
        private set

    var processingModal by mutableStateOf(false)
        private set

    var errorDisableBlocklist by mutableStateOf(false)
        private set

    var errorEnableBlocklist by mutableStateOf(false)
        private set

    var errorDeleteBlocklist by mutableStateOf(false)
        private set

    var blocklistDeletedSuccessfully by mutableStateOf(false)
        private set

    fun reset() {
        state = LoadingResult.Loading
        requestParams = defaultRequest
        selectedListName = null
        isRefreshing = false
        isLoadingMore = false
        processingModal = false
        errorDisableBlocklist = false
        errorEnableBlocklist = false
        errorDeleteBlocklist = false
        blocklistDeletedSuccessfully = false
    }

    fun selectListName(name: String?) { selectedListName = name }
    fun clearErrorDisableBlocklist() { errorDisableBlocklist = false }
    fun clearErrorEnableBlocklist() { errorEnableBlocklist = false }
    fun clearErrorDeleteBlocklist() { errorDeleteBlocklist = false }
    fun clearBlocklistDeletedSuccessfully() { blocklistDeletedSuccessfully = false }

    private suspend fun fetchData(showLoading: Boolean = false, params: BlocklistsRequest? = null) {
        val apiClient = sessionManager.apiClient ?: return
        if (showLoading) {
            state = LoadingResult.Loading
        }
        try {
            val result = apiClient.blocklists.fetchBlocklists(params ?: requestParams)
            state = LoadingResult.Success(result.body)
        } catch (e: Exception) {
            state = LoadingResult.Failure(e)
        }
    }

    fun initialFetch() {
        if (state.data != null) return
        viewModelScope.launch {
            fetchData(showLoading = true)
        }
    }

    fun refresh() {
        viewModelScope.launch {
            isRefreshing = true
            refreshInternal()
            isRefreshing = false
        }
    }

    fun fetchMore() {
        val apiClient = sessionManager.apiClient ?: return
        val data = state.data ?: return

        if ((data.pagination.page * Defaults.BLOCKLISTS_AMOUNT_BATCH) >= data.pagination.total) return

        val previousItems = data.items
        val newOffset = data.pagination.page * Defaults.BLOCKLISTS_AMOUNT_BATCH
        val newParams = requestParams.copy(offset = newOffset)
        requestParams = newParams

        viewModelScope.launch {
            try {
                isLoadingMore = true
                val result = apiClient.blocklists.fetchBlocklists(requestParams)
                val existingIds = previousItems.map { it.id }.toHashSet()
                val uniqueNewItems = result.body.items.filter { it.id !in existingIds }
                val mergedItems = previousItems + uniqueNewItems
                state = LoadingResult.Success(
                    BlocklistsListResponse(items = mergedItems, pagination = result.body.pagination)
                )
            } catch (e: Exception) {
                state = LoadingResult.Failure(e)
            } finally {
                isLoadingMore = false
            }
        }
    }

    fun enableDisableBlocklist(blocklistId: String, newStatus: Boolean) {
        val apiClient = sessionManager.apiClient ?: return
        viewModelScope.launch {
            processingModal = true
            try {
                apiClient.blocklists.toggleBlocklist(blocklistId, ToggleBlocklistRequest(enabled = newStatus))
                processingModal = false
                refreshInternal()
            } catch (_: Exception) {
                processingModal = false
                if (newStatus) errorEnableBlocklist = true else errorDisableBlocklist = true
            }
        }
    }

    fun deleteBlocklist(blocklistId: String) {
        val apiClient = sessionManager.apiClient ?: return
        viewModelScope.launch {
            processingModal = true
            try {
                apiClient.blocklists.deleteBlocklist(blocklistId)
                processingModal = false
                refreshInternal()
                blocklistDeletedSuccessfully = true
            } catch (_: Exception) {
                processingModal = false
                errorDeleteBlocklist = true
            }
        }
    }

    private suspend fun refreshInternal() {
        val req = defaultRequest
        requestParams = req
        fetchData(params = req)
    }
}

