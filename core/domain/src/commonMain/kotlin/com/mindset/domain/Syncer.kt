package com.mindset.domain

/** Outcome of a sync pass, surfaced to the ViewModel for the UI's sync indicator. */
sealed interface SyncOutcome {
    data object Success : SyncOutcome
    data class Failure(val error: Throwable) : SyncOutcome
}

/**
 * Drains local changes to the server and merges server changes back. The implementation (the
 * offline-first outbox/cursor engine) lives in `:core:sync`; features depend only on this interface.
 */
interface Syncer {
    suspend fun sync(): SyncOutcome
}
