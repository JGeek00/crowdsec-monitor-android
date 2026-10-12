package com.jgeek00.crowdsecmonitor.ui.screens.settings.notifications

import android.content.Context
import android.content.res.Resources
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

/**
 * Resolution of backend-provided localization keys (`labelKey`, `placeholderKey`,
 * `descriptionKey`, section/option `labelKey`s) to localized strings.
 *
 * The backend serves provider definitions with open string keys (for example
 * `provider_ntfy`, `section_topic`, `field_password`). This app localizes with
 * static Android resources (the values, values-es and values-de string
 * catalogs), so every backend key that
 * can reach the UI MUST have a matching `<string name="...">` entry in the
 * default catalog. The lookup uses [Resources.getIdentifier] because the key is
 * only known at runtime; if a key is missing (unknown provider, older app
 * talking to a newer backend), we fall back to the raw key instead of crashing
 * or rendering a blank label.
 *
 * Note: `getIdentifier` is cached by the framework and this is only hit for a
 * handful of labels per provider form, so the reflection-style lookup cost is
 * negligible.
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
