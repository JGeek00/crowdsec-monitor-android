package com.jgeek00.crowdsecmonitor.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jgeek00.crowdsecmonitor.data.db.CSServerModel
import com.jgeek00.crowdsecmonitor.data.repository.ServerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.jgeek00.crowdsecmonitor.session.SessionManager

@HiltViewModel
class ServersManagerViewModel @Inject constructor(
    private val serverRepository: ServerRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    var isLoading by mutableStateOf(true)
        private set

    var servers by mutableStateOf<List<CSServerModel>>(emptyList())
        private set

    var deleteServerError by mutableStateOf(false)
        private set

    var setDefaultServerError by mutableStateOf(false)
        private set

    var newDefaultServerSet by mutableStateOf<String?>(null)
        private set

    val currentServer: CSServerModel?
        get() = sessionManager.currentServer

    val hasServerConfigured: Boolean
        get() = sessionManager.hasServerConfigured

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
                activateAppropriateServer(list)
                isLoading = false
            }
        }
    }

    private suspend fun activateAppropriateServer(list: List<CSServerModel>) {
        val server = list.find { it.defaultServer == true } ?: list.firstOrNull()
        if (server != null) {
            if (sessionManager.currentServer?.id != server.id || sessionManager.apiClient == null) {
                sessionManager.activate(server)
            }
        } else {
            sessionManager.deactivate()
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

    fun changeCurrentServer(server: CSServerModel) {
        if (server.id == sessionManager.currentServer?.id) return
        viewModelScope.launch {
            sessionManager.activate(server)
        }
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
}

