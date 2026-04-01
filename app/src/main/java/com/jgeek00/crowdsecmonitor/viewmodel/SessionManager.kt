package com.jgeek00.crowdsecmonitor.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.jgeek00.crowdsecmonitor.data.api.CrowdSecApiClient
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionManager @Inject constructor() {
    var apiClient by mutableStateOf<CrowdSecApiClient?>(null)
        internal set

    private val _decisionsRefreshEvent = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val decisionsRefreshEvent = _decisionsRefreshEvent.asSharedFlow()

    fun triggerDecisionsRefresh() {
        _decisionsRefreshEvent.tryEmit(Unit)
    }
}

