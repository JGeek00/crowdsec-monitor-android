package com.jgeek00.crowdsecmonitor.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jgeek00.crowdsecmonitor.data.models.DecisionItemResponse
import com.jgeek00.crowdsecmonitor.data.models.LoadingResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DecisionDetailsViewModel @Inject constructor(
    private val sessionManager: SessionManager
) : ViewModel() {

    var state by mutableStateOf<LoadingResult<DecisionItemResponse>>(LoadingResult.Loading)
        private set

    var isRefreshing by mutableStateOf(false)
        private set

    var expiringDecisionProcess by mutableStateOf(false)
        private set

    private var initializedForId: Int? = null

    fun initialize(decisionId: Int) {
        if (initializedForId == decisionId) return
        initializedForId = decisionId
        fetchData(decisionId)
    }

    fun refresh(decisionId: Int) {
        viewModelScope.launch {
            isRefreshing = true
            fetchData(decisionId)
            isRefreshing = false
        }
    }

    private fun fetchData(decisionId: Int) {
        val apiClient = sessionManager.apiClient ?: return
        viewModelScope.launch {
            try {
                val result = apiClient.decisions.fetchDecisionDetails(decisionId)
                state = LoadingResult.Success(result.body)
            } catch (e: Exception) {
                state = LoadingResult.Failure(e)
            }
        }
    }

    fun expireDecision(decisionId: Int, onResult: (Boolean) -> Unit) {
        val apiClient = sessionManager.apiClient ?: run { onResult(false); return }
        viewModelScope.launch {
            expiringDecisionProcess = true
            try {
                apiClient.decisions.deleteDecision(decisionId)
                sessionManager.triggerDecisionsRefresh()
                fetchData(decisionId)
                expiringDecisionProcess = false
                onResult(true)
            } catch (_: Exception) {
                expiringDecisionProcess = false
                onResult(false)
            }
        }
    }
}

