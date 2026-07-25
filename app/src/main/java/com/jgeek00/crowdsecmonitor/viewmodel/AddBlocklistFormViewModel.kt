package com.jgeek00.crowdsecmonitor.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jgeek00.crowdsecmonitor.data.models.AddBlocklistRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.jgeek00.crowdsecmonitor.session.SessionManager

@HiltViewModel
class AddBlocklistFormViewModel @Inject constructor(
    private val sessionManager: SessionManager
) : ViewModel() {

    var name by mutableStateOf("")
    var url by mutableStateOf("")

    var isSaving by mutableStateOf(false)
        private set

    var requiredFieldsError by mutableStateOf(false)
    var invalidUrlError by mutableStateOf(false)
    var savingError by mutableStateOf(false)

    fun save(onSuccess: () -> Unit) {
        if (name.isBlank() || url.isBlank()) {
            requiredFieldsError = true
            return
        }

        // ponytail: plain Java URL.parse instead of android.util.Patterns.WEB_URL
        // so tests don't need Robolectric. URL will throw on malformed input.
        if (!isValidUrl(url.trim())) {
            invalidUrlError = true
            return
        }

        val apiClient = sessionManager.apiClient ?: return

        viewModelScope.launch {
            isSaving = true
            try {
                val body = AddBlocklistRequest(name = name.trim(), url = url.trim())
                apiClient.blocklists.addBlocklist(body)
                isSaving = false
                onSuccess()
            } catch (_: Exception) {
                savingError = true
                isSaving = false
            }
        }
    }

    private fun isValidUrl(url: String): Boolean = try {
        java.net.URI(url).toURL()
        true
    } catch (_: Exception) {
        false
    }

    fun reset() {
        name = ""
        url = ""
        isSaving = false
        requiredFieldsError = false
        invalidUrlError = false
        savingError = false
    }
}

