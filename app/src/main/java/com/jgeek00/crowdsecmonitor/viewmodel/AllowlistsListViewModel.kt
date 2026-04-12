package com.jgeek00.crowdsecmonitor.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jgeek00.crowdsecmonitor.data.models.AllowlistsListResponse
import com.jgeek00.crowdsecmonitor.data.models.LoadingResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.jgeek00.crowdsecmonitor.session.SessionManager

@HiltViewModel
class AllowlistsListViewModel @Inject constructor(
    private val sessionManager: SessionManager
) : ViewModel() {

    var state by mutableStateOf<LoadingResult<AllowlistsListResponse>>(LoadingResult.Loading)
        private set

    var isRefreshing by mutableStateOf(false)
        private set

    fun reset() {
        state = LoadingResult.Loading
        isRefreshing = false
    }

    private suspend fun fetchData(showLoading: Boolean = false) {
        val apiClient = sessionManager.apiClient ?: return
        if (showLoading) {
            state = LoadingResult.Loading
        }
        try {
            val result = apiClient.allowlists.fetchAllowlists()
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
            fetchData()
            isRefreshing = false
        }
    }
}

