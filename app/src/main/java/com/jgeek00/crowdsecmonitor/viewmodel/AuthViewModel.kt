package com.jgeek00.crowdsecmonitor.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jgeek00.crowdsecmonitor.data.api.CrowdSecApiClient
import com.jgeek00.crowdsecmonitor.data.db.CSServerModel
import com.jgeek00.crowdsecmonitor.data.repository.ServerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val serverRepository: ServerRepository
) : ViewModel() {
    var isLoading by mutableStateOf(true)
        private set

    var currentServer by mutableStateOf<CSServerModel?>(null)
        private set

    var servers by mutableStateOf<List<CSServerModel>>(emptyList())
        private set

    var apiClient by mutableStateOf<CrowdSecApiClient?>(null)
        private set

    var deleteServerError by mutableStateOf(false)
        private set

    var setDefaultServerError by mutableStateOf(false)
        private set

    var newDefaultServerSet by mutableStateOf<String?>(null)
        private set

    val hasServerConfigured: Boolean
        get() = currentServer != null && apiClient != null

    fun clearDeleteServerError() { deleteServerError = false }
    fun clearSetDefaultServerError() { setDefaultServerError = false }
    fun clearNewDefaultServerSet() { newDefaultServerSet = null }

    init {
        initialize()
    }

    private fun initialize() {
        viewModelScope.launch {
            isLoading = true
            serverRepository.getAllServers().collect { list ->
                servers = list
                checkInstance()
                isLoading = false
            }
        }
    }

    private fun checkInstance() {
        val server = servers.find { it.defaultServer == true } ?: servers.firstOrNull()
        
        if (server != null) {
            if (currentServer?.id != server.id || apiClient == null) {
                currentServer = server
                apiClient = CrowdSecApiClient(server)
            }
        } else {
            currentServer = null
            apiClient = null
        }
    }

    fun deleteServer(server: CSServerModel) {
        viewModelScope.launch {
            try {
                deleteServerError = false
                serverRepository.deleteServer(server)
            } catch (_: Exception) {
                deleteServerError = true
            }
        }
    }

    private fun deleteServerSilently(server: CSServerModel) {
        viewModelScope.launch {
            runCatching { serverRepository.deleteServer(server) }
        }
    }

    fun changeCurrentServer(server: CSServerModel) {
        if (server.id == currentServer?.id) return

        currentServer = server
        apiClient = CrowdSecApiClient(server)

        // TODO: Resetear los ViewModels dependientes cuando existan
        //  (ServerStatusViewModel, DashboardViewModel, AlertsListViewModel…)
    }

    fun setDefaultServer(server: CSServerModel) {
        viewModelScope.launch {
            try {
                setDefaultServerError = false
                serverRepository.setDefaultServer(server.id)
                newDefaultServerSet = server.name
            } catch (_: Exception) {
                setDefaultServerError = true
            }
        }
    }

    fun handleUnauthorized() {
        currentServer?.let { deleteServerSilently(it) }
    }

    fun logout() {
        currentServer?.let { deleteServerSilently(it) }
    }
}
