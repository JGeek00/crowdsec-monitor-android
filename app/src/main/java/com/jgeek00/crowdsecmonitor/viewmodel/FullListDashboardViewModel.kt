package com.jgeek00.crowdsecmonitor.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jgeek00.crowdsecmonitor.constants.Enums
import com.jgeek00.crowdsecmonitor.constants.chartColors
import com.jgeek00.crowdsecmonitor.data.models.LoadingResult
import com.jgeek00.crowdsecmonitor.utils.DashboardItemData
import com.jgeek00.crowdsecmonitor.utils.DashboardItemDataForView
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = FullListDashboardViewModel.Factory::class)
class FullListDashboardViewModel @AssistedInject constructor(
    @Assisted val itemType: Enums.DashboardItemType,
    private val sessionManager: SessionManager
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(itemType: Enums.DashboardItemType): FullListDashboardViewModel
    }

    var state by mutableStateOf<LoadingResult<List<DashboardItemDataForView>>>(LoadingResult.Loading)
        private set

    val chartData: List<DashboardItemDataForView>
        get() {
            val data = (state as? LoadingResult.Success)?.value ?: return emptyList()
            if (data.size <= chartColors.size) return data

            val result = data.take(chartColors.size).toMutableList()
            val others = data.drop(chartColors.size)
            result.add(
                DashboardItemDataForView(
                    item = "Others",
                    value = others.sumOf { it.value },
                    percentage = others.sumOf { it.percentage },
                    color = Color.Gray
                )
            )
            return result
        }

    init {
        fetchData()
    }

    fun fetchData() {
        val apiClient = sessionManager.apiClient ?: return
        viewModelScope.launch {
            state = LoadingResult.Loading
            try {
                val items: List<DashboardItemData> = when (itemType) {
                    Enums.DashboardItemType.COUNTRY ->
                        apiClient.statistics.countries.fetchCountriesStatistics().body
                            .map { DashboardItemData(item = it.countryCode, value = it.amount) }

                    Enums.DashboardItemType.IP_OWNER ->
                        apiClient.statistics.ipOwners.fetchIpOwnersStatistics().body
                            .map { DashboardItemData(item = it.ipOwner, value = it.amount) }

                    Enums.DashboardItemType.SCENARIO ->
                        apiClient.statistics.scenarios.fetchScenariosStatistics().body
                            .map { DashboardItemData(item = it.scenario, value = it.amount) }

                    Enums.DashboardItemType.TARGET ->
                        apiClient.statistics.targets.fetchTargetsStatistics().body
                            .map { DashboardItemData(item = it.target, value = it.amount) }
                }
                state = LoadingResult.Success(generateViewData(items))
            } catch (e: Exception) {
                state = LoadingResult.Failure(e)
            }
        }
    }

    private fun generateViewData(items: List<DashboardItemData>): List<DashboardItemDataForView> {
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

