package com.jgeek00.crowdsecmonitor.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.jgeek00.crowdsecmonitor.constants.Defaults
import com.jgeek00.crowdsecmonitor.constants.StorageKeys
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferencesRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    private val topItemsDashboardKey = intPreferencesKey(StorageKeys.TOP_ITEMS_DASHBOARD)
    private val themeKey = stringPreferencesKey(StorageKeys.THEME)
    private val showDefaultActiveDecisionsKey = booleanPreferencesKey(StorageKeys.SHOW_DEFAULT_ACTIVE_DECISIONS)
    private val disableDecisionTimerAnimationKey = booleanPreferencesKey(StorageKeys.DISABLE_DECISION_TIMER_ANIMATION)

    val topItemsDashboard: Flow<Int> = dataStore.data.map { prefs ->
        prefs[topItemsDashboardKey] ?: Defaults.TOP_ITEMS_DASHBOARD
    }

    val theme: Flow<String?> = dataStore.data.map { prefs ->
        prefs[themeKey]
    }

    val showDefaultActiveDecisions: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[showDefaultActiveDecisionsKey] ?: Defaults.SHOW_DEFAULT_ACTIVE_DECISIONS
    }

    val disableDecisionTimerAnimation: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[disableDecisionTimerAnimationKey] ?: Defaults.DISABLE_DECISION_TIMER_ANIMATION
    }

    suspend fun setTopItemsDashboard(value: Int) {
        dataStore.edit { prefs -> prefs[topItemsDashboardKey] = value }
    }

    suspend fun setTheme(value: String) {
        dataStore.edit { prefs -> prefs[themeKey] = value }
    }

    suspend fun setShowDefaultActiveDecisions(value: Boolean) {
        dataStore.edit { prefs -> prefs[showDefaultActiveDecisionsKey] = value }
    }

    suspend fun setDisableDecisionTimerAnimation(value: Boolean) {
        dataStore.edit { prefs -> prefs[disableDecisionTimerAnimationKey] = value }
    }
}

