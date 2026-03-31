package com.jgeek00.crowdsecmonitor.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.jgeek00.crowdsecmonitor.data.api.CrowdSecApiClient
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionManager @Inject constructor() {
    var apiClient by mutableStateOf<CrowdSecApiClient?>(null)
        internal set
}

