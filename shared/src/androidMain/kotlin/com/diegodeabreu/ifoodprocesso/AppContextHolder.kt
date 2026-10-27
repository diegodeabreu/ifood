package com.diegodeabreu.ifoodprocesso

import android.app.Activity
import android.content.Context
import java.lang.ref.WeakReference

object AppContextHolder {
    lateinit var context: Context
        private set

    private var activityRef: WeakReference<Activity>? = null

    fun init(context: Context) {
        this.context = context.applicationContext
    }

    fun setActivity(activity: Activity) {
        activityRef = WeakReference(activity)
    }

    fun finishActivity() {
        activityRef?.get()?.finishAffinity()
    }
}
