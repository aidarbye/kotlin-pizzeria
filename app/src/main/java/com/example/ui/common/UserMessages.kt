package com.example.ui.common

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow

/** Short feedback messages ("Saved", "Deleted") that ViewModels send and the app shows in a snackbar. */
class UserMessages {
    private val channel = Channel<String>(Channel.BUFFERED)

    val messages: Flow<String> = channel.receiveAsFlow()

    fun show(text: String) {
        channel.trySend(text)
    }
}
