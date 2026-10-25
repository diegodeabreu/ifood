package com.diegodeabreu.ifoodprocesso.analytics

interface AnalyticsTracker {
    fun logEvent(
        eventName: String,
        params: Map<String, Any> = emptyMap(),
    )

    fun setUserProperty(
        propertyName: String,
        value: String,
    )

    fun logScreenView(screenName: String)
}
