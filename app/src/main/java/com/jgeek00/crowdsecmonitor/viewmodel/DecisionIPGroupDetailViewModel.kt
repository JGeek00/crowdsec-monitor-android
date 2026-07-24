package com.jgeek00.crowdsecmonitor.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jgeek00.crowdsecmonitor.data.models.DecisionsByIPDetailResponse
import com.jgeek00.crowdsecmonitor.data.models.LoadingResult
import com.jgeek00.crowdsecmonitor.data.repository.PreferencesRepository
import com.jgeek00.crowdsecmonitor.session.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DecisionIPGroupDetailViewModel @Inject constructor(
    private val sessionManager: SessionManager,
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    var state by mutableStateOf<LoadingResult<DecisionsByIPDetailResponse>>(LoadingResult.Loading)
        private set

    var isRefreshing by mutableStateOf(false)
        private set

    var expiringDecisionProcess by mutableStateOf(false)
        private set

    var disableDecisionTimerAnimation by mutableStateOf(false)
        private set

    private var currentIp: String? = null
    private var currentOnlyActive: Boolean? = null

    init {
        viewModelScope.launch {
            preferencesRepository.disableDecisionTimerAnimation.collect { value ->
                disableDecisionTimerAnimation = value
            }
        }
    }

    fun initialize(ip: String, onlyActive: Boolean? = null) {
        if (ip == currentIp && onlyActive == currentOnlyActive) return
        currentIp = ip
        currentOnlyActive = onlyActive
        fetchData()
    }

    fun refresh() {
        viewModelScope.launch {
            isRefreshing = true
            fetchDataInternal()
            isRefreshing = false
        }
    }

    private fun fetchData() {
        viewModelScope.launch {
            state = LoadingResult.Loading
            fetchDataInternal()
        }
    }

    private suspend fun fetchDataInternal() {
        val apiClient = sessionManager.apiClient ?: return
        val ip = currentIp ?: return
        try {
            val result = apiClient.decisions.fetchDecisionsByIPDetail(ip = ip, onlyActive = currentOnlyActive)
            state = LoadingResult.Success(result.body)
        } catch (e: Exception) {
            state = LoadingResult.Failure(e)
        }
    }

    fun expireDecision(decisionId: Int, onResult: (Boolean) -> Unit) {
        val apiClient = sessionManager.apiClient ?: run { onResult(false); return }
        viewModelScope.launch {
            expiringDecisionProcess = true
            try {
                apiClient.decisions.deleteDecision(decisionId)
                sessionManager.triggerDecisionsRefresh()
                fetchDataInternal()
                expiringDecisionProcess = false
                onResult(true)
            } catch (_: Exception) {
                expiringDecisionProcess = false
                onResult(false)
            }
        }
    }
}
