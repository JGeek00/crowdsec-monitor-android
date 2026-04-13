package com.jgeek00.crowdsecmonitor.session

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.jgeek00.crowdsecmonitor.data.api.CrowdSecApiClient
import com.jgeek00.crowdsecmonitor.data.db.CSServerModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionManager @Inject constructor() {
    var apiClient by mutableStateOf<CrowdSecApiClient?>(null)
        internal set

    var currentServer by mutableStateOf<CSServerModel?>(null)
        internal set

    val hasServerConfigured: Boolean
        get() = currentServer != null && apiClient != null

    suspend fun activate(server: CSServerModel) {
        apiClient?.invalidate()
        currentServer = server
        apiClient = CrowdSecApiClient(server)
    }

    suspend fun deactivate() {
        apiClient?.invalidate()
        currentServer = null
        apiClient = null
    }

    private val _decisionsRefreshEvent = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val decisionsRefreshEvent = _decisionsRefreshEvent.asSharedFlow()

    fun triggerDecisionsRefresh() {
        _decisionsRefreshEvent.tryEmit(Unit)
    }

    private val _alertsRefreshEvent = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val alertsRefreshEvent = _alertsRefreshEvent.asSharedFlow()

    fun triggerAlertsRefresh() {
        _alertsRefreshEvent.tryEmit(Unit)
    }
}

