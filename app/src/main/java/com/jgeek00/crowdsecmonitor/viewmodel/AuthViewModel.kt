package com.jgeek00.crowdsecmonitor.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jgeek00.crowdsecmonitor.data.api.CrowdSecApiClient
import com.jgeek00.crowdsecmonitor.data.models.CSServer
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

    var currentServer by mutableStateOf<CSServer?>(null)
        private set

    var servers by mutableStateOf<List<CSServer>>(emptyList())
        private set

    var apiClient by mutableStateOf<CrowdSecApiClient?>(null)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    val hasServerConfigured: Boolean
        get() = currentServer != null && apiClient != null

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

    fun deleteServer(server: CSServer) {
        viewModelScope.launch {
            try {
                errorMessage = null
                serverRepository.deleteServer(server)
            } catch (e: Exception) {
                errorMessage = "Error al borrar el servidor: ${e.message}"
            }
        }
    }

    fun changeCurrentServer(server: CSServer) {
        if (server.id == currentServer?.id) return

        currentServer = server
        apiClient = CrowdSecApiClient(server)
        
        // TODO: Resetear otros ViewModels cuando existan
        // ServerStatusViewModel.reset()
    }

    fun setDefaultServer(server: CSServer) {
        viewModelScope.launch {
            try {
                errorMessage = null
                serverRepository.setDefaultServer(server.id)
            } catch (e: Exception) {
                errorMessage = "Error al establecer servidor predeterminado: ${e.message}"
            }
        }
    }

    fun handleUnauthorized() {
        currentServer?.let { deleteServer(it) }
    }

    fun logout() {
        currentServer?.let { deleteServer(it) }
    }
}
