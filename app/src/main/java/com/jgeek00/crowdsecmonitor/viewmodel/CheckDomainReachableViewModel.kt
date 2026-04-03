package com.jgeek00.crowdsecmonitor.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jgeek00.crowdsecmonitor.data.models.BlocklistsCheckDomainRequest
import com.jgeek00.crowdsecmonitor.data.models.BlocklistsCheckDomainResponse
import com.jgeek00.crowdsecmonitor.data.models.HttpClientException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.regex.Pattern
import javax.inject.Inject

@HiltViewModel
class CheckDomainReachableViewModel @Inject constructor(
    private val sessionManager: SessionManager
) : ViewModel() {

    var domain by mutableStateOf("")

    var invalidDomainAlert by mutableStateOf(false)

    var data by mutableStateOf<BlocklistsCheckDomainResponse?>(null)
        private set

    var error by mutableStateOf(false)
        private set

    var domainNotResolvable by mutableStateOf(false)
        private set

    var loading by mutableStateOf(false)
        private set

    private val domainPattern = Pattern.compile(
        "^(?:[a-zA-Z0-9](?:[a-zA-Z0-9\\-]{0,61}[a-zA-Z0-9])?\\.)+[a-zA-Z]{2,}$"
    )

    fun checkDomain() {
        if (!domainPattern.matcher(domain.trim()).matches()) {
            invalidDomainAlert = true
            return
        }
        val apiClient = sessionManager.apiClient ?: return

        viewModelScope.launch {
            loading = true
            try {
                val body = BlocklistsCheckDomainRequest(domain = domain.trim())
                val result = apiClient.blocklists.checkDomain(body)
                data = result.body
                error = false
                domainNotResolvable = false
                loading = false
            } catch (e: HttpClientException.HttpError) {
                if (e.statusCode == 422) {
                    data = null
                    error = false
                    domainNotResolvable = true
                } else {
                    data = null
                    error = true
                    domainNotResolvable = false
                }
                loading = false
            } catch (e: HttpClientException.HttpErrorWithMessage) {
                if (e.statusCode == 422) {
                    data = null
                    error = false
                    domainNotResolvable = true
                } else {
                    data = null
                    error = true
                    domainNotResolvable = false
                }
                loading = false
            } catch (_: Exception) {
                data = null
                error = true
                domainNotResolvable = false
                loading = false
            }
        }
    }
}

