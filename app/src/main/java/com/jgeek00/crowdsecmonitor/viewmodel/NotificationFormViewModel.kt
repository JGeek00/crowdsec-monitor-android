package com.jgeek00.crowdsecmonitor.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jgeek00.crowdsecmonitor.constants.NotificationDefaults
import com.jgeek00.crowdsecmonitor.data.models.AlertsRequest
import com.jgeek00.crowdsecmonitor.data.models.AlertsRequestFilters
import com.jgeek00.crowdsecmonitor.data.models.AlertsRequestPagination
import com.jgeek00.crowdsecmonitor.data.models.CreateNotificationRequest
import com.jgeek00.crowdsecmonitor.data.models.NotificationConditionNode
import com.jgeek00.crowdsecmonitor.data.models.NotificationThreshold
import com.jgeek00.crowdsecmonitor.data.models.UpdateNotificationRequest
import com.jgeek00.crowdsecmonitor.data.models.UserNotification
import com.jgeek00.crowdsecmonitor.data.models.UserNotificationChannel
import com.jgeek00.crowdsecmonitor.session.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicLong
import javax.inject.Inject

enum class RulesValidationIssue {
    EMPTY,
    INCOMPLETE,
    ADVANCED,
    INVALID_WINDOW
}

data class NotificationFilterOptions(
    val scenarios: List<String> = emptyList(),
    val countries: List<String> = emptyList(),
    val targets: List<String> = emptyList(),
    val ipOwners: List<String> = emptyList()
)

/**
 * Presentation model for the rule editor dialog. A single nullable state drives
 * presentation atomically: one state, one attempt. Hosted by the wizard and
 * detail screen roots (never by form rows), so host/lifecycle recycling during
 * keyboard transitions cannot re-trigger presentation. Mirrors the iOS
 * RuleSheetModel (.sheet(item:) equivalent).
 */
data class RuleSheetModel(
    val draft: EditableLeaf,
    val index: Int? = null
) {
    val id: Long = idCounter.incrementAndGet()

    companion object {
        private val idCounter = AtomicLong(0)
    }
}

@HiltViewModel
class NotificationFormViewModel @Inject constructor(
    private val sessionManager: SessionManager
) : ViewModel() {

    private var editing: UserNotification? = null

    val isEditing: Boolean
        get() = editing != null

    var selectedStep by mutableStateOf(0)

    var name by mutableStateOf("")

    var notificationDescription by mutableStateOf("")

    var noCondition by mutableStateOf(false)

    var rules by mutableStateOf<List<EditableLeaf>>(emptyList())

    var advancedCondition by mutableStateOf<NotificationConditionNode?>(null)

    var count by mutableStateOf(NotificationDefaults.DEFAULT_COUNT)

    var windowSecondsText by mutableStateOf(NotificationDefaults.DEFAULT_WINDOW_SECONDS_TEXT)

    var cooldownSeconds by mutableStateOf(NotificationDefaults.DEFAULT_COOLDOWN_SECONDS)

    var message by mutableStateOf("")

    var includeAlertInfo by mutableStateOf(false)

    var channels by mutableStateOf<List<UserNotificationChannel>>(emptyList())

    var selectedChannelIds by mutableStateOf<Set<Int>>(emptySet())

    var filterOptions by mutableStateOf(NotificationFilterOptions())

    var isSaving by mutableStateOf(false)

    var saveError by mutableStateOf(false)

    var loadError by mutableStateOf(false)

    val windowValid: Boolean
        get() {
            val seconds = windowSecondsText.trim().toIntOrNull() ?: return false
            return seconds in NotificationDefaults.WINDOW_MIN_SECONDS..NotificationDefaults.WINDOW_MAX_SECONDS
        }

    val parsedWindowSeconds: Int
        get() = windowSecondsText.trim().toIntOrNull() ?: NotificationDefaults.WINDOW_MIN_SECONDS

    // MARK: - Rule editor dialog state
    //
    // `null` = dismissed. Opening the rule editor while the keyboard is visible
    // collapses the whole dialog stack, so when the keyboard is visible the
    // dialog is staged and only presented after [keyboardDidHide] plus a short
    // settle delay; otherwise it is presented synchronously. Mirrors the iOS
    // keyboard-staging behavior (6d5f4ef).

    var ruleSheet by mutableStateOf<RuleSheetModel?>(null)
        private set

    var isKeyboardVisible by mutableStateOf(false)
        private set

    private var pendingSheet: PendingSheet? = null
    private var pendingSheetJob: Job? = null

    private data class PendingSheet(
        val draft: EditableLeaf,
        val index: Int?
    )

    fun openRuleSheet(draft: EditableLeaf, index: Int?) {
        pendingSheetJob?.cancel()
        pendingSheetJob = null
        if (isKeyboardVisible) {
            pendingSheet = PendingSheet(draft = draft, index = index)
        } else {
            ruleSheet = RuleSheetModel(draft = draft, index = index)
        }
    }

    fun commitRuleSheet() {
        val sheet = ruleSheet ?: return
        val index = sheet.index
        if (index != null && rules.indices.contains(index)) {
            rules = rules.toMutableList().also { it[index] = sheet.draft }
        } else {
            rules = rules + sheet.draft
        }
        ruleSheet = null
    }

    fun dismissRuleSheet() {
        ruleSheet = null
    }

    fun keyboardWillShow() {
        isKeyboardVisible = true
    }

    fun keyboardDidHide() {
        isKeyboardVisible = false
        val pending = pendingSheet
        if (pending == null || ruleSheet != null) return
        pendingSheet = null
        pendingSheetJob?.cancel()
        pendingSheetJob = viewModelScope.launch {
            delay(350)
            ruleSheet = RuleSheetModel(draft = pending.draft, index = pending.index)
        }
    }

    // MARK: - Load / prefill

    fun load(notification: UserNotification) {
        editing = notification
        name = notification.name
        notificationDescription = notification.description ?: ""
        val condition = notification.condition
        if (condition is NotificationConditionNode.And && condition.children.isEmpty()) {
            noCondition = true
            rules = emptyList()
            advancedCondition = null
        } else {
            val flat = rulesFromNode(condition)
            if (flat != null) {
                noCondition = false
                rules = flat
                advancedCondition = null
            } else {
                noCondition = false
                rules = emptyList()
                advancedCondition = condition
            }
        }
        notification.threshold?.let { threshold ->
            count = threshold.count
            windowSecondsText = threshold.windowSeconds.toString()
            cooldownSeconds = threshold.cooldownSeconds ?: NotificationDefaults.DEFAULT_COOLDOWN_SECONDS
        }
        message = notification.message
        includeAlertInfo = notification.includeAlertInfo
        selectedChannelIds = notification.channelIds.toSet()
    }

    fun loadOptions() {
        val apiClient = sessionManager.apiClient ?: return
        viewModelScope.launch {
            try {
                val alerts = apiClient.alerts.fetchAlerts(
                    AlertsRequest(
                        filters = AlertsRequestFilters(),
                        pagination = AlertsRequestPagination(offset = 0, limit = 1)
                    )
                )
                val channelsResult = apiClient.notificationChannels.fetchChannels()
                filterOptions = NotificationFilterOptions(
                    scenarios = alerts.body.filtering.scenarios.sorted(),
                    countries = alerts.body.filtering.countries.sorted(),
                    targets = alerts.body.filtering.targets.sorted(),
                    ipOwners = alerts.body.filtering.ipOwners.sorted()
                )
                channels = channelsResult.body.data
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                loadError = true
            }
        }
    }

    // MARK: - Condition building

    fun buildCondition(): EditableCondition {
        if (noCondition) {
            return EditableCondition.And(emptyList())
        }
        return EditableCondition.And(rules.map { EditableCondition.Leaf(it) })
    }

    fun replaceWithSimpleRules() {
        advancedCondition = null
        rules = emptyList()
    }

    /**
     * Fresh draft with the first available scenario already selected, so the
     * displayed value and the model agree from the start.
     */
    fun newDraft(): EditableLeaf {
        val first = filterOptions.scenarios.firstOrNull() ?: return EditableLeaf.Empty
        return EditableLeaf(
            field = ConditionField.SCENARIO,
            op = ConditionOperator.EQUALS,
            values = listOf(first)
        )
    }

    // MARK: - Validation

    fun validationIssue(step: Int): RulesValidationIssue? {
        if (step != 1) return null
        if (!noCondition) {
            if (advancedCondition != null) {
                return RulesValidationIssue.ADVANCED
            }
            if (rules.isEmpty()) {
                return RulesValidationIssue.EMPTY
            }
            if (rules.any { !it.isValid }) {
                return RulesValidationIssue.INCOMPLETE
            }
        }
        if (!windowValid) {
            return RulesValidationIssue.INVALID_WINDOW
        }
        return null
    }

    fun options(field: ConditionField): List<String> {
        return when (field) {
            ConditionField.SCENARIO -> filterOptions.scenarios
            ConditionField.COUNTRY -> filterOptions.countries
            ConditionField.TARGET -> filterOptions.targets
            ConditionField.IP_OWNER -> filterOptions.ipOwners
        }
    }

    fun canProceed(step: Int): Boolean {
        return when (step) {
            0 -> name.isNotBlank()
            1 -> validationIssue(1) == null
            2 -> message.isNotBlank()
            3 -> selectedChannelIds.isNotEmpty()
            else -> true
        }
    }

    // MARK: - Save

    suspend fun save(): UserNotification? {
        val apiClient = sessionManager.apiClient ?: return null
        isSaving = true
        try {
            val threshold = NotificationThreshold(
                count = count,
                windowSeconds = parsedWindowSeconds,
                cooldownSeconds = cooldownSeconds
            )
            val condition = buildCondition().toApi()
            val channelIds = selectedChannelIds.toList()
            val editing = this.editing
            return if (editing != null) {
                val body = UpdateNotificationRequest(
                    name = name,
                    description = descriptionOrNil(),
                    enabled = null,
                    condition = condition,
                    threshold = threshold,
                    message = message,
                    includeAlertInfo = includeAlertInfo,
                    channelIds = channelIds
                )
                apiClient.notifications.updateNotification(editing.id, body).body.data
            } else {
                val body = CreateNotificationRequest(
                    name = name,
                    description = descriptionOrNil(),
                    enabled = true,
                    condition = condition,
                    threshold = threshold,
                    message = message,
                    includeAlertInfo = includeAlertInfo,
                    channelIds = channelIds
                )
                apiClient.notifications.createNotification(body).body.data
            }
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            saveError = true
            return null
        } finally {
            isSaving = false
        }
    }

    private fun descriptionOrNil(): String? {
        return notificationDescription.trim().ifEmpty { null }
    }

    companion object {
        /**
         * Converts an API tree into flat rules when possible (AND of leaves, single
         * leaf, or OR of same-field equals/in leaves merged into one rule).
         * Returns null for anything else (nested groups, cross-field OR, NOT,
         * unknown fields/operators).
         */
        fun rulesFromNode(node: NotificationConditionNode): List<EditableLeaf>? {
            return when (node) {
                is NotificationConditionNode.Leaf -> {
                    val leaf = editableLeafFromWire(node) ?: return null
                    listOf(leaf)
                }
                is NotificationConditionNode.And -> {
                    val leaves = mutableListOf<EditableLeaf>()
                    for (child in node.children) {
                        val wireLeaf = child as? NotificationConditionNode.Leaf ?: return null
                        val leaf = editableLeafFromWire(wireLeaf) ?: return null
                        leaves.add(leaf)
                    }
                    leaves
                }
                is NotificationConditionNode.Or -> mergedOrRule(node.children)
                is NotificationConditionNode.Not -> null
            }
        }

        private fun editableLeafFromWire(node: NotificationConditionNode.Leaf): EditableLeaf? {
            val field = ConditionField.entries.firstOrNull { it.wireName == node.field } ?: return null
            val op = ConditionOperator.entries.firstOrNull { it.wireName == node.operator } ?: return null
            val values = node.value.asArray
            if (values.isEmpty()) return null
            return EditableLeaf(field = field, op = op, values = values)
        }

        private fun mergedOrRule(items: List<NotificationConditionNode>): List<EditableLeaf>? {
            var field: ConditionField? = null
            val values = mutableListOf<String>()
            for (item in items) {
                val leaf = item as? NotificationConditionNode.Leaf ?: return null
                if (leaf.operator != ConditionOperator.EQUALS.wireName &&
                    leaf.operator != ConditionOperator.IN.wireName
                ) {
                    return null
                }
                val parsed = ConditionField.entries.firstOrNull { it.wireName == leaf.field } ?: return null
                if (field != null && field != parsed) {
                    return null
                }
                field = parsed
                values.addAll(leaf.value.asArray)
            }
            if (field == null || values.isEmpty()) return null
            // Dedup preserving first-occurrence order.
            return listOf(EditableLeaf(field = field, op = ConditionOperator.IN, values = values.distinct()))
        }
    }
}
