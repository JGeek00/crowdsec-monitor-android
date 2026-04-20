package com.jgeek00.crowdsecmonitor.viewmodel

import android.content.Context
import android.util.Patterns
import java.util.regex.Pattern
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.jgeek00.crowdsecmonitor.R
import com.jgeek00.crowdsecmonitor.data.api.CrowdSecApiClient
import com.jgeek00.crowdsecmonitor.data.models.HttpClientException
import com.jgeek00.crowdsecmonitor.data.db.CSServerModel
import com.jgeek00.crowdsecmonitor.constants.Enums
import com.jgeek00.crowdsecmonitor.data.repository.ServerRepository
import com.jgeek00.crowdsecmonitor.utils.InputFieldState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

const val MAX_CUSTOM_HEADERS = 10
data class CustomHeaderState(
    var key: String = "",
    var value: String = "",
    var keyError: String? = null,
    var valueError: String? = null
)

@HiltViewModel
class ConnectionFormViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
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
    val customHeaders = mutableStateListOf<CustomHeaderState>()

    private fun updateEnabledStates(enabled: Boolean) {
        name.enabled = enabled
        ipDomain.enabled = enabled
        port.enabled = enabled
        path.enabled = enabled
        basicUser.enabled = enabled
        basicPassword.enabled = enabled
        bearerToken.enabled = enabled
    }
    fun addCustomHeader() {
        if (customHeaders.size < MAX_CUSTOM_HEADERS) {
            customHeaders.add(CustomHeaderState())
        }
    }

    fun removeCustomHeader(index: Int) {
        if (index in customHeaders.indices) customHeaders.removeAt(index)
    }

    fun updateCustomHeaderKey(index: Int, key: String) {
        if (index !in customHeaders.indices) return
        val current = customHeaders[index]
        customHeaders[index] = current.copy(
            key = key,
            keyError = validateHeaderKey(key)
        )
    }

    fun updateCustomHeaderValue(index: Int, value: String) {
        if (index !in customHeaders.indices) return
        val current = customHeaders[index]
        customHeaders[index] = current.copy(
            value = value,
            valueError = if (value.isBlank()) "Header value is required" else null
        )
    }

    private fun validateHeaderKey(key: String): String? = when {
        key.isBlank() -> "Header name is required"
        !key.matches(Regex("[!#\$%&'*+\\-.0-9A-Z^_`a-z|~]+")) ->
            "Header name contains invalid characters"
        else -> null
    }

    private fun validateAllCustomHeaders(): Boolean {
        var valid = true
        customHeaders.forEachIndexed { i, h ->
            val keyError = validateHeaderKey(h.key)
            val valueError = if (h.value.isBlank()) "Header value is required" else null
            if (keyError != null || valueError != null) {
                customHeaders[i] = h.copy(keyError = keyError, valueError = valueError)
                valid = false
            }
        }
        return valid
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
        val headersValid = validateAllCustomHeaders()

        return name.error == null && ipDomain.error == null && port.error == null &&
                basicUser.error == null && basicPassword.error == null && bearerToken.error == null &&
                headersValid
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

                val headersSnapshot = customHeaders
                    .filter { it.key.isNotBlank() }
                    .map { Pair(it.key.trim(), it.value) }
                    .takeIf { it.isNotEmpty() }

                val tempServer = CSServerModel(
                    name = name.value,
                    http = connectionMethod.value,
                    domain = ipDomain.value,
                    port = portValue,
                    path = pathValue,
                    authMethod = authMethod.value,
                    basicUser = basicUser.value.ifBlank { null },
                    basicPassword = basicPassword.value.ifBlank { null },
                    bearerToken = bearerToken.value.ifBlank { null },
                    customHeaders = headersSnapshot
                )

                val apiClient = CrowdSecApiClient(tempServer)
                apiClient.checkApiStatus()

                serverRepository.createServer(
                    name = name.value,
                    connectionMethod = connectionMethod.value,
                    ipDomain = ipDomain.value,
                    port = portValue,
                    path = pathValue,
                    authMethod = authMethod.value,
                    basicUser = basicUser.value.ifBlank { null },
                    basicPassword = basicPassword.value.ifBlank { null },
                    bearerToken = bearerToken.value.ifBlank { null },
                    customHeaders = headersSnapshot
                )

                withContext(Dispatchers.Main) {
                    connecting = false
                    updateEnabledStates(true)
                }
                true
            } catch (e: HttpClientException) {
                withContext(Dispatchers.Main) {
                    connectionErrorAlert = true
                    connectionErrorMessage = when (e) {
                        is HttpClientException.Unauthorized -> context.getString(R.string.connection_error_invalid_credentials)
                        is HttpClientException.InvalidConnectionValues -> context.getString(R.string.connection_error_invalid_values)
                        is HttpClientException.HttpErrorWithMessage -> e.message
                        is HttpClientException.HttpError -> context.getString(R.string.connection_error_server, e.statusCode)
                        else -> context.getString(R.string.connection_error_no_response)
                    }
                    connecting = false
                    updateEnabledStates(true)
                }
                false
            } catch (_: Exception) {
                withContext(Dispatchers.Main) {
                    connectionErrorAlert = true
                    connectionErrorMessage = context.getString(R.string.connection_error_no_response)
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

        customHeaders.clear()

        connecting = false
        updateEnabledStates(true)
        connectionErrorAlert = false
    }
}
