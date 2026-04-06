package com.jgeek00.crowdsecmonitor.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jgeek00.crowdsecmonitor.data.models.BlocklistDataResponse
import com.jgeek00.crowdsecmonitor.data.models.LoadingResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

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

    private var initializedForId: String? = null

    fun initialize(blocklistId: String) {
        if (initializedForId == blocklistId) return
        initializedForId = blocklistId
        ipsRound = 1
        searchPresented = false
        searchText = ""
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

