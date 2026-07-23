package dev.cadence

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

/**
 * Handle handed to Swift so it can stop observing (call from `deinit`). Cancelling tears down the
 * collecting coroutine and its scope.
 */
class FlowSubscription(private val scope: CoroutineScope) {
    fun cancel() {
        scope.cancel()
    }
}

/**
 * Bridge a Kotlin [Flow] to Swift. Kotlin/Native does not export `Flow` in a form Swift can observe,
 * so this launches a collector on [Dispatchers.Main] and pushes each emission to [onEach], returning
 * a [FlowSubscription] Swift cancels when its view goes away.
 *
 * Design notes (this is the KMP seam, done by hand — what SKIE / KMP-NativeCoroutines automate):
 * - **Own scope**, not the ViewModel's `viewModelScope`: the *Swift* view owns this observation's
 *   lifetime, so it must be independently cancellable.
 * - **`Dispatchers.Main`** so the callback runs on the main thread — SwiftUI `@Published` writes and
 *   any UI state must happen there.
 * - **`Flow<*>` / `Any?`**: without a bridge plugin, Kotlin generics erase at the Objective-C
 *   boundary, so Swift receives `Any?` and casts once at the call site.
 */
fun subscribe(flow: Flow<*>, onEach: (Any?) -> Unit): FlowSubscription {
    val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    scope.launch {
        flow.collect { onEach(it) }
    }
    return FlowSubscription(scope)
}
