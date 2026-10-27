package com.diegodeabreu.ifoodprocesso.presentation.ui

import com.diegodeabreu.ifoodprocesso.AppContextHolder

actual fun exitApp() {
    AppContextHolder.finishActivity()
}
