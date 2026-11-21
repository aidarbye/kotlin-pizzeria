package com.example.ui.common

import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
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

/** One snackbar state for the whole app. Each screen shows it in its own Scaffold, above its FAB and bottom bar. */
val LocalSnackbarHostState = staticCompositionLocalOf { SnackbarHostState() }

@Composable
fun AppSnackbarHost() {
    SnackbarHost(LocalSnackbarHostState.current)
}
