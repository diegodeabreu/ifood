package com.diegodeabreu.ifoodprocesso.analytics

object Analytics {
    private var tracker: AnalyticsTracker = ConsoleAnalyticsTracker()

    fun init(tracker: AnalyticsTracker) {
        this.tracker = tracker
    }

    fun track(
        eventName: String,
        params: Map<String, Any> = emptyMap(),
    ) {
        tracker.logEvent(eventName, params)
    }

    fun userProperty(
        propertyName: String,
        value: String,
    ) {
        tracker.setUserProperty(propertyName, value)
    }

    fun screen(screenName: String) {
        tracker.logScreenView(screenName)
    }
}
