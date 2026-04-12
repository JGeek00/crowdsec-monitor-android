package com.jgeek00.crowdsecmonitor.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jgeek00.crowdsecmonitor.constants.Defaults
import com.jgeek00.crowdsecmonitor.data.models.DecisionItemResponse
import com.jgeek00.crowdsecmonitor.data.models.LoadingResult
import com.jgeek00.crowdsecmonitor.data.repository.PreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.jgeek00.crowdsecmonitor.session.SessionManager

@HiltViewModel
class DecisionDetailsViewModel @Inject constructor(
    private val sessionManager: SessionManager,
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    var state by mutableStateOf<LoadingResult<DecisionItemResponse>>(LoadingResult.Loading)
        private set

    var isRefreshing by mutableStateOf(false)
        private set

    var expiringDecisionProcess by mutableStateOf(false)
        private set

    var disableDecisionTimerAnimation by mutableStateOf(Defaults.DISABLE_DECISION_TIMER_ANIMATION)
        private set

    private var initializedForId: Int? = null

    init {
        // Collect as a flow so changes from AppConfigurationScreen reflect immediately
        viewModelScope.launch {
            preferencesRepository.disableDecisionTimerAnimation.collect { value ->
                disableDecisionTimerAnimation = value
            }
        }
    }

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

