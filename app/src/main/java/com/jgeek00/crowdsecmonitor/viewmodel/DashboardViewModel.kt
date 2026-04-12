package com.jgeek00.crowdsecmonitor.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jgeek00.crowdsecmonitor.constants.chartColors
import com.jgeek00.crowdsecmonitor.data.models.LoadingResult
import com.jgeek00.crowdsecmonitor.data.models.StatisticsResponse
import com.jgeek00.crowdsecmonitor.data.repository.PreferencesRepository
import com.jgeek00.crowdsecmonitor.utils.DashboardItemData
import com.jgeek00.crowdsecmonitor.utils.DashboardItemDataForView
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.jgeek00.crowdsecmonitor.session.SessionManager

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    var state by mutableStateOf<LoadingResult<StatisticsResponse>>(LoadingResult.Loading)
        private set

    var isRefreshing by mutableStateOf(false)
        private set

    init {
        viewModelScope.launch {
            combine(
                snapshotFlow { sessionManager.apiClient },
                preferencesRepository.topItemsDashboard
            ) { client, _ -> client }
            .collect { client ->
                if (client != null) fetchDashboardData()
                else state = LoadingResult.Loading
            }
        }
    }

    fun reset() {
        state = LoadingResult.Loading
    }

    fun fetchDashboardData() {
        val apiClient = sessionManager.apiClient ?: return
        viewModelScope.launch {
            val amountItems = preferencesRepository.topItemsDashboard.first()
            state = LoadingResult.Loading
            try {
                val result = apiClient.statistics.fetchStatistics(amount = amountItems)
                state = LoadingResult.Success(result.body)
            } catch (e: Exception) {
                state = LoadingResult.Failure(e)
            }
        }
    }

    fun refresh() {
        val apiClient = sessionManager.apiClient ?: return
        viewModelScope.launch {
            val amountItems = preferencesRepository.topItemsDashboard.first()
            isRefreshing = true
            try {
                val result = apiClient.statistics.fetchStatistics(amount = amountItems)
                state = LoadingResult.Success(result.body)
            } catch (e: Exception) {
                state = LoadingResult.Failure(e)
            } finally {
                isRefreshing = false
            }
        }
    }

    fun generateViewData(items: List<DashboardItemData>): List<DashboardItemDataForView> {
        val total = items.sumOf { it.value }
        return items.mapIndexed { index, item ->
            val percentage = if (total > 0) item.value.toDouble() / total else 0.0
            val color = if (index < chartColors.size) chartColors[index] else Color.Gray
            DashboardItemDataForView(
                item = item.item,
                value = item.value,
                percentage = percentage,
                color = color
            )
        }
    }
}
