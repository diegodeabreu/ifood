package com.diegodeabreu.ifoodprocesso

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform
