package com.jgeek00.crowdsecmonitor.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jgeek00.crowdsecmonitor.data.models.HttpClientException
import com.jgeek00.crowdsecmonitor.data.models.LoadingResult
import com.jgeek00.crowdsecmonitor.data.models.NotificationProvider
import com.jgeek00.crowdsecmonitor.data.models.UserNotificationChannel
import com.jgeek00.crowdsecmonitor.session.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationChannelsListViewModel @Inject constructor(
    private val sessionManager: SessionManager
) : ViewModel() {

    var state by mutableStateOf<LoadingResult<List<UserNotificationChannel>>>(LoadingResult.Loading)
        private set

    var isRefreshing by mutableStateOf(false)
        private set

    var providers by mutableStateOf<List<NotificationProvider>>(emptyList())
        private set

    var errorDelete by mutableStateOf(false)
        private set

    var channelInUse by mutableStateOf(false)
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
        providers = emptyList()
        errorDelete = false
        channelInUse = false
        didDelete = false
    }

    fun clearErrorDelete() {
        errorDelete = false
    }

    fun clearChannelInUse() {
        channelInUse = false
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
            val result = apiClient.notificationChannels.fetchChannels()
            state = LoadingResult.Success(result.body.data)
            try {
                val providersResult = apiClient.notificationChannels.fetchProviders()
                providers = providersResult.body.providers
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                // Providers are auxiliary: the channel list remains usable if they fail.
            }
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

    fun delete(channelId: Int) {
        val apiClient = sessionManager.apiClient ?: return
        viewModelScope.launch {
            try {
                apiClient.notificationChannels.deleteChannel(channelId)
                val items = state.data
                if (items != null) {
                    state = LoadingResult.Success(items.filter { it.id != channelId })
                }
                didDelete = true
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                if (isConflict(e)) {
                    channelInUse = true
                } else {
                    errorDelete = true
                }
            }
        }
    }

    private fun isConflict(error: Exception): Boolean {
        return when (error) {
            is HttpClientException.HttpError -> error.statusCode == 409
            is HttpClientException.HttpErrorWithMessage -> error.statusCode == 409
            else -> false
        }
    }
}
