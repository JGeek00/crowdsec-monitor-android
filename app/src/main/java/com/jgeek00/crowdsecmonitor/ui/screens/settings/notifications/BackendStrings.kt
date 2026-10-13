package com.jgeek00.crowdsecmonitor.ui.screens.settings.notifications

import android.content.Context
import android.content.res.Resources
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

/**
 * Resolves backend localization keys (`labelKey`, `placeholderKey`, ...
 * open string keys) to localized strings. Every backend key that can reach the
 * UI must have a `<string name="...">` entry in the default catalog; the
 * lookup uses [Resources.getIdentifier] because the key is only known at
 * runtime. Missing entries fall back to the raw key (unknown provider or an
 * older app talking to a newer backend) instead of crashing or rendering a
 * blank label. The framework caches `getIdentifier` results and this is hit
 * only for a handful of labels per form, so the cost is negligible.
 */
fun backendStringResId(resources: Resources, packageName: String, key: String): Int =
    resources.getIdentifier(key, "string", packageName)

/**
 * Non-composable resolution used by tests and any non-UI context.
 * Unknown keys resolve to the raw key text.
 */
fun resolveBackendString(context: Context, key: String): String {
    val resId = backendStringResId(context.resources, context.packageName, key)
    return if (resId != 0) context.getString(resId) else key
}

/**
 * Composable variant for provider definition labels. Recomputed on every
 * recomposition (cheap lookup) so locale/configuration changes are always
 * reflected; no caching that could go stale after a language switch.
 */
@Composable
fun backendString(key: String): String {
    val context = LocalContext.current
    return resolveBackendString(context, key)
}
