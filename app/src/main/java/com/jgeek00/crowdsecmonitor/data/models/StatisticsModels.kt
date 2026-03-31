package com.jgeek00.crowdsecmonitor.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StatisticsResponse(
    @SerialName("alertsLast24Hours") val alertsLast24Hours: Int,
    @SerialName("activeDecisions") val activeDecisions: Int,
    @SerialName("activityHistory") val activityHistory: List<ActivityHistory>,
    @SerialName("topCountries") val topCountries: List<TopCountry>,
    @SerialName("topScenarios") val topScenarios: List<TopScenario>,
    @SerialName("topIpOwners") val topIpOwners: List<TopIpOwner>,
    @SerialName("topTargets") val topTargets: List<TopTarget>
)

@Serializable
data class ActivityHistory(
    @SerialName("date") val date: String,
    @SerialName("amountAlerts") val amountAlerts: Int,
    @SerialName("amountDecisions") val amountDecisions: Int
)

@Serializable
data class TopCountry(
    @SerialName("countryCode") val countryCode: String,
    @SerialName("amount") val amount: Int
)

@Serializable
data class TopIpOwner(
    @SerialName("ipOwner") val ipOwner: String,
    @SerialName("amount") val amount: Int
)

@Serializable
data class TopScenario(
    @SerialName("scenario") val scenario: String,
    @SerialName("amount") val amount: Int
)

@Serializable
data class TopTarget(
    @SerialName("target") val target: String,
    @SerialName("amount") val amount: Int
)

