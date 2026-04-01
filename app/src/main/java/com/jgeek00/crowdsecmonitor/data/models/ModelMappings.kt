package com.jgeek00.crowdsecmonitor.data.models

fun AlertDetailsDecision.toDecisionsListResponseItem(): DecisionsListResponseItem =
    DecisionsListResponseItem(
        id = id,
        alertId = alertId,
        origin = origin,
        type = type,
        scope = scope,
        value = value,
        expiration = expiration,
        scenario = scenario,
        simulated = simulated,
        source = DecisionSource(
            asName = source.asName,
            asNumber = source.asNumber,
            cn = source.cn,
            ip = source.ip,
            latitude = source.latitude,
            longitude = source.longitude,
            range = source.range,
            scope = source.scope,
            value = source.value
        ),
        crowdsecCreatedAt = crowdsecCreatedAt
    )

fun DecisionItemAlert.toAlertsListResponseAlert(): AlertsListResponseAlert =
    AlertsListResponseAlert(
        id = id,
        uuid = uuid,
        scenario = scenario,
        scenarioVersion = scenarioVersion,
        scenarioHash = scenarioHash,
        message = message,
        capacity = capacity,
        leakspeed = leakspeed,
        simulated = simulated,
        remediation = remediation,
        eventsCount = eventsCount,
        machineId = machineId,
        source = AlertSource(
            asName = source.asName,
            asNumber = source.asNumber,
            cn = source.cn,
            ip = source.ip,
            latitude = source.latitude,
            longitude = source.longitude,
            range = source.range,
            scope = source.scope,
            value = source.value
        ),
        meta = emptyList(),
        events = emptyList(),
        crowdsecCreatedAt = crowdsecCreatedAt,
        startAt = startAt,
        stopAt = stopAt
    )

