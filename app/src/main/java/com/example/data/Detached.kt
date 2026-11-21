package com.example.data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async

/**
 * Runs a write in the application scope and waits for it. If the screen that started the write is closed
 * and its viewModelScope is cancelled, the write still completes: only the waiting is cancelled.
 */
suspend fun <T> CoroutineScope.runDetached(block: suspend CoroutineScope.() -> T): T = async(block = block).await()
