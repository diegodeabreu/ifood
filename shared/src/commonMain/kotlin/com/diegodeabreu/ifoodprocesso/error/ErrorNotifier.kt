package com.diegodeabreu.ifoodprocesso.error

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow

object ErrorNotifier {
    private val errorChannel = Channel<String>(Channel.BUFFERED)
    val errors = errorChannel.receiveAsFlow()

    fun notifyError(message: String) {
        errorChannel.trySend(message)
    }
}
