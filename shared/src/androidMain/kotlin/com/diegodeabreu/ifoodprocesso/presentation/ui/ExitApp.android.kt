package com.diegodeabreu.ifoodprocesso.presentation.ui

import android.app.Activity
import com.diegodeabreu.ifoodprocesso.AppContextHolder

actual fun exitApp() {
    (AppContextHolder.context as? Activity)?.finishAffinity()
}
