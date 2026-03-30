package com.jgeek00.crowdsecmonitor.utils

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class InputFieldState(
    initialValue: String = "",
    initialError: String? = null,
    initialEnabled: Boolean = true
) {
    var value by mutableStateOf(initialValue)
    var error by mutableStateOf(initialError)
    var enabled by mutableStateOf(initialEnabled)

    fun reset() {
        value = ""
        error = null
        enabled = true
    }
}
