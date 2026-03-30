package com.jgeek00.crowdsecmonitor.viewmodel

import android.util.Patterns
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.jgeek00.crowdsecmonitor.data.api.CrowdSecApiClient
import com.jgeek00.crowdsecmonitor.data.models.CSServer
import com.jgeek00.crowdsecmonitor.data.models.Enums
import com.jgeek00.crowdsecmonitor.data.repository.ServerRepository
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
    var name by mutableStateOf("")
    var connectionMethod by mutableStateOf(Enums.ConnectionMethod.HTTP)
    var ipDomain by mutableStateOf("")
    var port by mutableStateOf("")
    var path by mutableStateOf("")
    var authMethod by mutableStateOf(Enums.AuthMethod.NONE)
    var basicUser by mutableStateOf("")
    var basicPassword by mutableStateOf("")
    var bearerToken by mutableStateOf("")

    var connecting by mutableStateOf(false)

    var invalidValuesAlert by mutableStateOf(false)
    var invalidValuesMessage by mutableStateOf("")

    var connectionErrorAlert by mutableStateOf(false)
    var connectionErrorMessage by mutableStateOf("")

    val isFormValid: Boolean
        get() = name.isNotBlank() && ipDomain.isNotBlank() && when (authMethod) {
            Enums.AuthMethod.BASIC -> basicUser.isNotBlank() && basicPassword.isNotBlank()
            Enums.AuthMethod.BEARER -> bearerToken.isNotBlank()
            Enums.AuthMethod.NONE -> true
        }

    fun checkValues(): Boolean {
        invalidValuesAlert = false
        invalidValuesMessage = ""

        if (name.isBlank()) {
            invalidValuesAlert = true
            invalidValuesMessage = "Name field is required"
            return false
        }

        if (ipDomain.isBlank()) {
            invalidValuesAlert = true
            invalidValuesMessage = "IP/Domain field is required"
            return false
        }

        val isIp = Patterns.IP_ADDRESS.matcher(ipDomain).matches()
        val isDomain = Patterns.DOMAIN_NAME.matcher(ipDomain).matches()
        if (!isIp && !isDomain) {
            invalidValuesAlert = true
            invalidValuesMessage = "IP/Domain value is not valid"
            return false
        }

        if (port.isNotBlank()) {
            val portNumber = port.toIntOrNull()
            if (portNumber == null) {
                invalidValuesAlert = true
                invalidValuesMessage = "Port must be a valid number"
                return false
            }
            if (portNumber <= 0 || portNumber > 65535) {
                invalidValuesAlert = true
                invalidValuesMessage = "Port must be between 1 and 65535"
                return false
            }
        }

        when (authMethod) {
            Enums.AuthMethod.BASIC -> {
                if (basicUser.isBlank()) {
                    invalidValuesAlert = true
                    invalidValuesMessage = "Username is required for basic authentication"
                    return false
                }
                if (basicPassword.isBlank()) {
                    invalidValuesAlert = true
                    invalidValuesMessage = "Password is required for basic authentication"
                    return false
                }
            }
            Enums.AuthMethod.BEARER -> {
                if (bearerToken.isBlank()) {
                    invalidValuesAlert = true
                    invalidValuesMessage = "Token is required for Bearer authentication"
                    return false
                }
            }
            Enums.AuthMethod.NONE -> {}
        }

        return true
    }

    suspend fun connect(): Boolean {
        if (!checkValues()) return false

        connecting = true
        connectionErrorAlert = false
        connectionErrorMessage = ""

        return withContext(Dispatchers.IO) {
            try {
                val portValue = port.ifBlank { null }?.toIntOrNull()
                val pathValue = path.ifBlank { null }
                
                val tempServer = CSServer(
                    name = name,
                    http = connectionMethod.value,
                    domain = ipDomain,
                    port = portValue,
                    path = pathValue,
                    authMethod = authMethod.value,
                    basicUser = basicUser.ifBlank { null },
                    basicPassword = basicPassword.ifBlank { null },
                    bearerToken = bearerToken.ifBlank { null }
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
                    }
                    return@withContext false
                }

                serverRepository.createServer(
                    name = name,
                    connectionMethod = connectionMethod.value,
                    ipDomain = ipDomain,
                    port = portValue,
                    path = pathValue,
                    authMethod = authMethod.value,
                    basicUser = basicUser.ifBlank { null },
                    basicPassword = basicPassword.ifBlank { null },
                    bearerToken = bearerToken.ifBlank { null }
                )

                withContext(Dispatchers.Main) {
                    connecting = false
                }
                true
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    connectionErrorAlert = true
                    connectionErrorMessage = "Could not connect to server: ${e.localizedMessage}"
                    connecting = false
                }
                false
            }
        }
    }

    fun reset() {
        name = ""
        connectionMethod = Enums.ConnectionMethod.HTTP
        ipDomain = ""
        port = ""
        path = ""
        authMethod = Enums.AuthMethod.NONE
        basicUser = ""
        basicPassword = ""
        bearerToken = ""
        connecting = false
        invalidValuesAlert = false
        connectionErrorAlert = false
    }
}
