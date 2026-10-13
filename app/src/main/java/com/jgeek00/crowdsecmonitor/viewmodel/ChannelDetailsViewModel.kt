package com.jgeek00.crowdsecmonitor.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jgeek00.crowdsecmonitor.data.models.LoadingResult
import com.jgeek00.crowdsecmonitor.data.models.UserNotificationChannel
import com.jgeek00.crowdsecmonitor.session.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Loads a single notification channel (GET /notification-channels/{id}). Stays
 * in Loading until the server session exists; every session change while an id
 * is set re-fetches, so a server switch or a late login refreshes the details.
 */
@HiltViewModel
class ChannelDetailsViewModel @Inject constructor(
    private val sessionManager: SessionManager
) : ViewModel() {

    var state by mutableStateOf<LoadingResult<UserNotificationChannel>>(LoadingResult.Loading)
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

    fun initialize(channelId: Int) {
        if (initializedForId == channelId) return
        initializedForId = channelId
        if (sessionManager.apiClient != null) {
            fetch(channelId)
        } else {
            state = LoadingResult.Loading
        }
    }

    fun refresh() {
        initializedForId?.let { fetch(it) }
    }

    /**
     * In-place replacement after a successful save, so the read summary updates
     * without a round-trip.
     */
    fun update(channel: UserNotificationChannel) {
        if (initializedForId == channel.id) {
            state = LoadingResult.Success(channel)
        }
    }

    private fun fetch(channelId: Int, showLoading: Boolean = true) {
        val apiClient = sessionManager.apiClient ?: return
        if (showLoading) {
            state = LoadingResult.Loading
        }
        viewModelScope.launch {
            try {
                val result = apiClient.notificationChannels.fetchChannel(channelId)
                state = LoadingResult.Success(result.body.data)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                state = LoadingResult.Failure(e)
            }
        }
    }
}
