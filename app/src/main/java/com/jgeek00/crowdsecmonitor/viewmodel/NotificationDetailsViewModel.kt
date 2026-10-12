package com.jgeek00.crowdsecmonitor.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jgeek00.crowdsecmonitor.data.models.LoadingResult
import com.jgeek00.crowdsecmonitor.data.models.UserNotification
import com.jgeek00.crowdsecmonitor.session.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Loads a single notification (GET /notifications/{id}) for the details screen.
 *
 * Mirrors the list ViewModels' session flow: when the server session is not
 * ready yet (`apiClient == null`) the screen stays in Loading, and every
 * session change while an id is set re-fetches, so a server switch or a late
 * login refreshes the details.
 */
@HiltViewModel
class NotificationDetailsViewModel @Inject constructor(
    private val sessionManager: SessionManager
) : ViewModel() {

    var state by mutableStateOf<LoadingResult<UserNotification>>(LoadingResult.Loading)
        private set

    private var initializedForId: Int? = null

    init {
        viewModelScope.launch {
            snapshotFlow { sessionManager.apiClient }.collect { client ->
                if (client == null) {
                    state = LoadingResult.Loading
                } else {
                    initializedForId?.let { fetch(it) }
                }
            }
        }
    }

    fun initialize(notificationId: Int) {
        if (initializedForId == notificationId) return
        initializedForId = notificationId
        if (sessionManager.apiClient != null) {
            fetch(notificationId)
        } else {
            state = LoadingResult.Loading
        }
    }

    fun refresh() {
        initializedForId?.let { fetch(it) }
    }

    /**
     * In-place replacement after a successful save, so the read summary updates
     * without a round-trip (matches the iOS `currentNotification = saved`).
     */
    fun update(notification: UserNotification) {
        if (initializedForId == notification.id) {
            state = LoadingResult.Success(notification)
        }
    }

    private fun fetch(notificationId: Int, showLoading: Boolean = true) {
        val apiClient = sessionManager.apiClient ?: return
        if (showLoading) {
            state = LoadingResult.Loading
        }
        viewModelScope.launch {
            try {
                val result = apiClient.notifications.fetchNotification(notificationId)
                state = LoadingResult.Success(result.body.data)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                state = LoadingResult.Failure(e)
            }
        }
    }
}
