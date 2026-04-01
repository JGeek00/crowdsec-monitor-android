package com.jgeek00.crowdsecmonitor.utils

import android.content.Context
import android.location.Geocoder
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.coroutines.resume

suspend fun reverseGeocode(
    context: Context,
    latitude: Double,
    longitude: Double
): String? = withContext(Dispatchers.IO) {
    try {
        if (!Geocoder.isPresent()) return@withContext null
        val geocoder = Geocoder(context, Locale.getDefault())

        val addresses = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            suspendCancellableCoroutine { continuation ->
                geocoder.getFromLocation(latitude, longitude, 1) { list ->
                    continuation.resume(list)
                }
            }
        } else {
            @Suppress("DEPRECATION")
            geocoder.getFromLocation(latitude, longitude, 1) ?: emptyList()
        }

        addresses.firstOrNull()?.let { address ->
            listOfNotNull(
                address.locality,
                address.adminArea,
                address.countryName
            ).joinToString(", ").takeIf { it.isNotEmpty() }
        }
    } catch (_: Exception) {
        null
    }
}