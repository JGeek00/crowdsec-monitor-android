package com.jgeek00.crowdsecmonitor.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jgeek00.crowdsecmonitor.data.models.BlocklistDataResponse
import com.jgeek00.crowdsecmonitor.data.models.LoadingResult
import com.jgeek00.crowdsecmonitor.data.models.ToggleBlocklistRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.jgeek00.crowdsecmonitor.session.SessionManager

@HiltViewModel
class BlocklistDetailsViewModel @Inject constructor(
    private val sessionManager: SessionManager
) : ViewModel() {

    var state by mutableStateOf<LoadingResult<BlocklistDataResponse>>(LoadingResult.Loading)
        private set

    var isRefreshing by mutableStateOf(false)
        private set

    var ipsRound by mutableIntStateOf(1)
        private set

    var searchPresented by mutableStateOf(false)
        private set

    var searchText by mutableStateOf("")
        private set

    var processingModal by mutableStateOf(false)
        private set

    var errorRefreshBlocklist by mutableStateOf(false)
        private set

    var errorToggleBlocklist by mutableStateOf(false)
        private set

    var errorDeleteBlocklist by mutableStateOf(false)
        private set

    var blocklistDeletedSuccessfully by mutableStateOf(false)
        private set

    private var initializedForId: String? = null

    fun initialize(blocklistId: String) {
        if (initializedForId == blocklistId) return
        initializedForId = blocklistId
        ipsRound = 1
        searchPresented = false
        searchText = ""
        resetActionFlags()
        fetchData(blocklistId)
    }

    fun refresh(blocklistId: String) {
        viewModelScope.launch {
            isRefreshing = true
            fetchData(blocklistId)
            isRefreshing = false
        }
    }

    fun updateBlocklistId(newId: String) {
        initializedForId = newId
        ipsRound = 1
        searchText = ""
        resetActionFlags()
        fetchData(newId, showLoading = true)
    }

    fun incrementIpsRound() {
        ipsRound++
    }

    fun updateSearchPresented(value: Boolean) {
        searchPresented = value
        if (!value) searchText = ""
    }

    fun updateSearchText(text: String) {
        searchText = text
    }

    fun clearErrorRefreshBlocklist() { errorRefreshBlocklist = false }
    fun clearErrorToggleBlocklist() { errorToggleBlocklist = false }
    fun clearErrorDeleteBlocklist() { errorDeleteBlocklist = false }
    fun clearBlocklistDeletedSuccessfully() { blocklistDeletedSuccessfully = false }

    fun refreshBlocklist(blocklistId: String) {
        val apiClient = sessionManager.apiClient ?: return
        viewModelScope.launch {
            processingModal = true
            try {
                apiClient.blocklists.refreshBlocklist(blocklistId)
                processingModal = false
            } catch (_: Exception) {
                processingModal = false
                errorRefreshBlocklist = true
            }
        }
    }

    fun toggleBlocklist(blocklistId: String, newStatus: Boolean) {
        val apiClient = sessionManager.apiClient ?: return
        viewModelScope.launch {
            processingModal = true
            try {
                apiClient.blocklists.toggleBlocklist(blocklistId, ToggleBlocklistRequest(enabled = newStatus))
                processingModal = false
                fetchData(blocklistId)
            } catch (_: Exception) {
                processingModal = false
                errorToggleBlocklist = true
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
                blocklistDeletedSuccessfully = true
            } catch (_: Exception) {
                processingModal = false
                errorDeleteBlocklist = true
            }
        }
    }

    private fun resetActionFlags() {
        errorRefreshBlocklist = false
        errorToggleBlocklist = false
        errorDeleteBlocklist = false
        blocklistDeletedSuccessfully = false
    }

    private fun fetchData(blocklistId: String, showLoading: Boolean = false) {
        val apiClient = sessionManager.apiClient ?: return
        viewModelScope.launch {
            if (showLoading) {
                state = LoadingResult.Loading
            }
            try {
                val result = apiClient.blocklists.fetchBlocklistData(blocklistId)
                state = LoadingResult.Success(result.body)
            } catch (e: Exception) {
                state = LoadingResult.Failure(e)
            }
        }
    }
}
