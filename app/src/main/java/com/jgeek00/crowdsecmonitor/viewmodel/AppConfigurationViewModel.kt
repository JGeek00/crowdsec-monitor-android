package com.jgeek00.crowdsecmonitor.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jgeek00.crowdsecmonitor.constants.Defaults
import com.jgeek00.crowdsecmonitor.data.repository.PreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppConfigurationViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    var topItemsDashboard by mutableIntStateOf(Defaults.TOP_ITEMS_DASHBOARD)
        private set

    var showDefaultActiveDecisions by mutableStateOf(Defaults.SHOW_DEFAULT_ACTIVE_DECISIONS)
        private set

    var disableDecisionTimerAnimation by mutableStateOf(Defaults.DISABLE_DECISION_TIMER_ANIMATION)
        private set

    init {
        viewModelScope.launch {
            topItemsDashboard = preferencesRepository.topItemsDashboard.first()
            showDefaultActiveDecisions = preferencesRepository.showDefaultActiveDecisions.first()
            disableDecisionTimerAnimation = preferencesRepository.disableDecisionTimerAnimation.first()
        }
    }

    fun updateTopItemsDashboard(value: Int) {
        val clamped = value.coerceIn(Defaults.TOP_ITEMS_DASHBOARD_MIN, Defaults.TOP_ITEMS_DASHBOARD_MAX)
        topItemsDashboard = clamped
        viewModelScope.launch {
            preferencesRepository.setTopItemsDashboard(clamped)
        }
    }

    fun updateShowDefaultActiveDecisions(value: Boolean) {
        showDefaultActiveDecisions = value
        viewModelScope.launch {
            preferencesRepository.setShowDefaultActiveDecisions(value)
        }
    }

    fun updateDisableDecisionTimerAnimation(value: Boolean) {
        disableDecisionTimerAnimation = value
        viewModelScope.launch {
            preferencesRepository.setDisableDecisionTimerAnimation(value)
        }
    }
}
