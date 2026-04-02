package com.jgeek00.crowdsecmonitor.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jgeek00.crowdsecmonitor.constants.Enums
import com.jgeek00.crowdsecmonitor.data.models.CreateDecisionRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.net.Inet6Address
import java.net.InetAddress
import java.util.regex.Pattern
import javax.inject.Inject

@HiltViewModel
class CreateDecisionFormViewModel @Inject constructor(
    private val sessionManager: SessionManager
) : ViewModel() {

    var ipAddress by mutableStateOf("")
    var durationDays by mutableIntStateOf(0)
    var durationHours by mutableIntStateOf(4)
    var durationMinutes by mutableIntStateOf(0)
    var type by mutableStateOf(Enums.DecisionType.BAN)
    var reason by mutableStateOf("")

    var invalidFieldsAlert by mutableStateOf(false)
    var invalidFieldsAlertMessage by mutableStateOf("")
    var errorCreatingDecisionAlert by mutableStateOf(false)
    var creatingDecision by mutableStateOf(false)
        private set

    val durationString: String
        get() {
            val components = mutableListOf<String>()
            if (durationDays > 0) components.add("${durationDays}d")
            if (durationHours > 0) components.add("${durationHours}h")
            if (durationMinutes > 0) components.add("${durationMinutes}m")
            return components.joinToString("")
        }

    private fun isValidIpAddress(ip: String): Boolean {
        val ipv4Pattern = Pattern.compile(
            "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$"
        )
        if (ipv4Pattern.matcher(ip).matches()) return true
        if (!ip.contains(":")) return false
        return try {
            InetAddress.getByName(ip) is Inet6Address
        } catch (_: Exception) {
            false
        }
    }

    fun validateValues(): Boolean {
        if (ipAddress.isBlank()) {
            invalidFieldsAlertMessage = "IP address is required"
            invalidFieldsAlert = true
            return false
        }
        if (!isValidIpAddress(ipAddress.trim())) {
            invalidFieldsAlertMessage = "IP address is invalid"
            invalidFieldsAlert = true
            return false
        }
        if (durationDays == 0 && durationHours == 0 && durationMinutes == 0) {
            invalidFieldsAlertMessage = "Duration must be greater than 0"
            invalidFieldsAlert = true
            return false
        }
        if (reason.isBlank()) {
            invalidFieldsAlertMessage = "Reason is required"
            invalidFieldsAlert = true
            return false
        }
        return true
    }

    fun save(onSuccess: () -> Unit) {
        if (!validateValues()) return
        val apiClient = sessionManager.apiClient ?: return

        viewModelScope.launch {
            creatingDecision = true
            try {
                val body = CreateDecisionRequest(
                    ip = ipAddress.trim(),
                    duration = durationString,
                    type = type,
                    reason = reason
                )
                apiClient.decisions.createDecision(body)

                sessionManager.triggerDecisionsRefresh()
                sessionManager.triggerAlertsRefresh()

                creatingDecision = false
                onSuccess()
            } catch (_: Exception) {
                errorCreatingDecisionAlert = true
                creatingDecision = false
            }
        }
    }

    fun reset() {
        ipAddress = ""
        durationDays = 0
        durationHours = 4
        durationMinutes = 0
        type = Enums.DecisionType.BAN
        reason = ""
        invalidFieldsAlert = false
        invalidFieldsAlertMessage = ""
        errorCreatingDecisionAlert = false
        creatingDecision = false
    }
}





