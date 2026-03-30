package com.jgeek00.crowdsecmonitor.viewmodel

import android.util.Patterns
import java.util.regex.Pattern
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.jgeek00.crowdsecmonitor.data.api.CrowdSecApiClient
import com.jgeek00.crowdsecmonitor.data.models.CSServer
import com.jgeek00.crowdsecmonitor.data.models.Enums
import com.jgeek00.crowdsecmonitor.data.repository.ServerRepository
import com.jgeek00.crowdsecmonitor.utils.InputFieldState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import okhttp3.Request
import javax.inject.Inject

@Serializable
data class ApiStatusResponse(
    val status: String? = null
)

@HiltViewModel
class ConnectionFormViewModel @Inject constructor(
    private val serverRepository: ServerRepository
) : ViewModel() {
    val name = InputFieldState()
    val ipDomain = InputFieldState()
    val port = InputFieldState()
    val path = InputFieldState()
    val basicUser = InputFieldState()
    val basicPassword = InputFieldState()
    val bearerToken = InputFieldState()

    var connectionMethod by mutableStateOf(Enums.ConnectionMethod.HTTP)
    var authMethod by mutableStateOf(Enums.AuthMethod.NONE)

    var connecting by mutableStateOf(false)
        private set

    var connectionErrorAlert by mutableStateOf(false)
    var connectionErrorMessage by mutableStateOf("")

    private fun updateEnabledStates(enabled: Boolean) {
        name.enabled = enabled
        ipDomain.enabled = enabled
        port.enabled = enabled
        path.enabled = enabled
        basicUser.enabled = enabled
        basicPassword.enabled = enabled
        bearerToken.enabled = enabled
    }

    fun validateName(value: String) {
        name.value = value
        name.error = if (value.isBlank()) "Name field is required" else null
    }

    fun validateIpDomain(value: String) {
        ipDomain.value = value
        val ipPattern = Pattern.compile(
            "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$"
        )
        val isIp = ipPattern.matcher(value).matches()
        val isDomain = Patterns.DOMAIN_NAME.matcher(value).matches()
        ipDomain.error = when {
            value.isBlank() -> "IP/Domain field is required"
            !isIp && !isDomain -> "IP/Domain value is not valid"
            else -> null
        }
    }

    fun validatePort(value: String) {
        port.value = value
        if (value.isBlank()) {
            port.error = null
            return
        }
        val portNumber = value.toIntOrNull()
        port.error = when (portNumber) {
            null -> "Port must be a valid number"
            !in 1..65535 -> "Port must be between 1 and 65535"
            else -> null
        }
    }

    fun validatePath(value: String) {
        path.value = value
        path.error = null // No specific validation for path currently
    }

    fun validateBasicUser(value: String) {
        basicUser.value = value
        basicUser.error = if (value.isBlank() && authMethod == Enums.AuthMethod.BASIC) {
            "Username is required"
        } else null
    }

    fun validateBasicPassword(value: String) {
        basicPassword.value = value
        basicPassword.error = if (value.isBlank() && authMethod == Enums.AuthMethod.BASIC) {
            "Password is required"
        } else null
    }

    fun validateBearerToken(value: String) {
        bearerToken.value = value
        bearerToken.error = if (value.isBlank() && authMethod == Enums.AuthMethod.BEARER) {
            "Token is required"
        } else null
    }

    fun validateAll(): Boolean {
        validateName(name.value)
        validateIpDomain(ipDomain.value)
        validatePort(port.value)
        if (authMethod == Enums.AuthMethod.BASIC) {
            validateBasicUser(basicUser.value)
            validateBasicPassword(basicPassword.value)
        } else if (authMethod == Enums.AuthMethod.BEARER) {
            validateBearerToken(bearerToken.value)
        }
        
        return name.error == null && ipDomain.error == null && port.error == null &&
               basicUser.error == null && basicPassword.error == null && bearerToken.error == null
    }

    suspend fun connect(): Boolean {
        if (!validateAll()) return false

        connecting = true
        updateEnabledStates(false)
        connectionErrorAlert = false
        connectionErrorMessage = ""

        return withContext(Dispatchers.IO) {
            try {
                val portValue = port.value.ifBlank { null }?.toIntOrNull()
                val pathValue = path.value.ifBlank { null }
                
                val tempServer = CSServer(
                    name = name.value,
                    http = connectionMethod.value,
                    domain = ipDomain.value,
                    port = portValue,
                    path = pathValue,
                    authMethod = authMethod.value,
                    basicUser = basicUser.value.ifBlank { null },
                    basicPassword = basicPassword.value.ifBlank { null },
                    bearerToken = bearerToken.value.ifBlank { null }
                )

                val apiClient = CrowdSecApiClient(tempServer)
                val request = Request.Builder()
                    .url(apiClient.retrofit.baseUrl().newBuilder().addPathSegments("api/v1/status").build())
                    .build()

                val response = apiClient.retrofit.callFactory().newCall(request).execute()

                if (!response.isSuccessful) {
                    withContext(Dispatchers.Main) {
                        connectionErrorAlert = true
                        connectionErrorMessage = when (response.code) {
                            401 -> "Invalid credentials. Please verify your username, password, or token."
                            else -> "Server error: code ${response.code}"
                        }
                        connecting = false
                        updateEnabledStates(true)
                    }
                    return@withContext false
                }

                serverRepository.createServer(
                    name = name.value,
                    connectionMethod = connectionMethod.value,
                    ipDomain = ipDomain.value,
                    port = portValue,
                    path = pathValue,
                    authMethod = authMethod.value,
                    basicUser = basicUser.value.ifBlank { null },
                    basicPassword = basicPassword.value.ifBlank { null },
                    bearerToken = bearerToken.value.ifBlank { null }
                )

                withContext(Dispatchers.Main) {
                    connecting = false
                    updateEnabledStates(true)
                }
                true
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    connectionErrorAlert = true
                    connectionErrorMessage = "Could not connect to server: ${e.localizedMessage}"
                    connecting = false
                    updateEnabledStates(true)
                }
                false
            }
        }
    }

    fun reset() {
        name.reset()
        ipDomain.reset()
        port.reset()
        path.reset()
        basicUser.reset()
        basicPassword.reset()
        bearerToken.reset()

        connectionMethod = Enums.ConnectionMethod.HTTP
        authMethod = Enums.AuthMethod.NONE

        connecting = false
        updateEnabledStates(true)
        connectionErrorAlert = false
    }
}
