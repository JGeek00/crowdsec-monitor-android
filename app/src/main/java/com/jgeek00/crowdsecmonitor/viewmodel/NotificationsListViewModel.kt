package com.jgeek00.crowdsecmonitor.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jgeek00.crowdsecmonitor.data.models.LoadingResult
import com.jgeek00.crowdsecmonitor.data.models.ToggleNotificationRequest
import com.jgeek00.crowdsecmonitor.data.models.UserNotification
import com.jgeek00.crowdsecmonitor.session.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationsListViewModel @Inject constructor(
    private val sessionManager: SessionManager
) : ViewModel() {

    var state by mutableStateOf<LoadingResult<List<UserNotification>>>(LoadingResult.Loading)
        private set

    var isRefreshing by mutableStateOf(false)
        private set

    var errorToggle by mutableStateOf(false)
        private set

    var errorDelete by mutableStateOf(false)
        private set

    var didDelete by mutableStateOf(false)
        private set

    init {
        viewModelScope.launch {
            snapshotFlow { sessionManager.apiClient }.collect { client ->
                reset()
                if (client != null) fetchData(showLoading = true)
            }
        }
    }

    fun reset() {
        state = LoadingResult.Loading
        isRefreshing = false
        errorToggle = false
        errorDelete = false
        didDelete = false
    }

    fun clearErrorToggle() {
        errorToggle = false
    }

    fun clearErrorDelete() {
        errorDelete = false
    }

    fun clearDidDelete() {
        didDelete = false
    }

    private suspend fun fetchData(showLoading: Boolean = false) {
        val apiClient = sessionManager.apiClient ?: return
        if (showLoading) {
            state = LoadingResult.Loading
        }
        try {
            val result = apiClient.notifications.fetchNotifications()
            state = LoadingResult.Success(result.body.data)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            state = LoadingResult.Failure(e)
        }
    }

    fun initialFetch() {
        if (state.data != null || state.isLoading) return
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

    fun toggle(notification: UserNotification) {
        val apiClient = sessionManager.apiClient ?: return
        viewModelScope.launch {
            try {
                val result = apiClient.notifications.toggleNotification(
                    notification.id,
                    ToggleNotificationRequest(enabled = !notification.enabled)
                )
                val items = state.data ?: return@launch
                state = LoadingResult.Success(
                    items.map { if (it.id == notification.id) result.body.data else it }
                )
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                errorToggle = true
            }
        }
    }

    fun delete(notificationId: Int) {
        val apiClient = sessionManager.apiClient ?: return
        viewModelScope.launch {
            try {
                apiClient.notifications.deleteNotification(notificationId)
                val items = state.data
                if (items != null) {
                    state = LoadingResult.Success(items.filter { it.id != notificationId })
                }
                didDelete = true
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                errorDelete = true
            }
        }
    }
}
