package com.jgeek00.crowdsecmonitor.viewmodel

import android.net.InetAddresses
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jgeek00.crowdsecmonitor.constants.Enums
import com.jgeek00.crowdsecmonitor.data.models.AllowlistsCheckIPsRequest
import com.jgeek00.crowdsecmonitor.data.models.AllowlistsCheckIPsResponse
import com.jgeek00.crowdsecmonitor.data.models.BlocklistsCheckIPsRequest
import com.jgeek00.crowdsecmonitor.data.models.BlocklistsCheckIPsResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class IPsCheckerViewModel @Inject constructor(
    private val sessionManager: SessionManager
) : ViewModel() {

    data class IPField(val value: String = "", val invalid: Boolean = false)

    var ipsToCheck by mutableStateOf(listOf<IPField>())
        private set

    var selectedListType by mutableStateOf(Enums.ListType.BLOCKLIST)

    var blocklistsLoading by mutableStateOf(false)
        private set
    var blocklistsResult by mutableStateOf<BlocklistsCheckIPsResponse?>(null)
        private set
    var blocklistsError by mutableStateOf(false)
        private set

    var allowlistsLoading by mutableStateOf(false)
        private set
    var allowlistsResult by mutableStateOf<AllowlistsCheckIPsResponse?>(null)
        private set
    var allowlistsError by mutableStateOf(false)
        private set

    private fun isValidIpAddress(ip: String): Boolean {
        return InetAddresses.isNumericAddress(ip)
    }

    fun updateEntry(index: Int, value: String) {
        val isInvalid = value.isNotEmpty() && !isValidIpAddress(value)
        ipsToCheck = ipsToCheck.toMutableList().also {
            it[index] = it[index].copy(value = value, invalid = isInvalid)
        }
    }

    fun addEntry() {
        ipsToCheck = ipsToCheck + IPField()
    }

    fun removeEntry(index: Int) {
        ipsToCheck = ipsToCheck.toMutableList().also { it.removeAt(index) }
    }

    fun checkIps() {
        val apiClient = sessionManager.apiClient ?: return
        when (selectedListType) {
            Enums.ListType.BLOCKLIST -> {
                blocklistsLoading = true
                blocklistsError = false
                blocklistsResult = null
                viewModelScope.launch {
                    try {
                        val ips = ipsToCheck.map { it.value }
                        val result = apiClient.blocklists.checkIps(BlocklistsCheckIPsRequest(ips = ips))
                        blocklistsResult = result.body
                        blocklistsLoading = false
                    } catch (_: Exception) {
                        blocklistsError = true
                        blocklistsLoading = false
                    }
                }
            }
            Enums.ListType.ALLOWLIST -> {
                allowlistsLoading = true
                allowlistsError = false
                allowlistsResult = null
                viewModelScope.launch {
                    try {
                        val ips = ipsToCheck.map { it.value }
                        val result = apiClient.allowlists.checkIps(AllowlistsCheckIPsRequest(ips = ips))
                        allowlistsResult = result.body
                        allowlistsLoading = false
                    } catch (_: Exception) {
                        allowlistsError = true
                        allowlistsLoading = false
                    }
                }
            }
        }
    }

    fun reset() {
        ipsToCheck = listOf()
        selectedListType = Enums.ListType.BLOCKLIST
        blocklistsLoading = false
        blocklistsResult = null
        blocklistsError = false
        allowlistsLoading = false
        allowlistsResult = null
        allowlistsError = false
    }

    fun resetAfterClose(delayMs: Long = 300L) {
        viewModelScope.launch {
            delay(delayMs)
            reset()
        }
    }
}



