package com.diegodeabreu.ifoodprocesso.analytics

class ConsoleAnalyticsTracker : AnalyticsTracker {
    override fun logEvent(
        eventName: String,
        params: Map<String, Any>,
    ) {
        println("[Analytics] Event: $eventName | Params: $params")
    }

    override fun setUserProperty(
        propertyName: String,
        value: String,
    ) {
        println("[Analytics] UserProperty: $propertyName = $value")
    }

    override fun logScreenView(screenName: String) {
        println("[Analytics] ScreenView: $screenName")
    }
}
