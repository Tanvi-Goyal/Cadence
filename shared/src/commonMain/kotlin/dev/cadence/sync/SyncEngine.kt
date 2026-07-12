package dev.cadence.sync

import androidx.room.immediateTransaction
import androidx.room.useWriterConnection
import dev.cadence.contracts.SessionDto
import dev.cadence.data.local.AppDatabase
import dev.cadence.data.local.OutboxDao
import dev.cadence.data.local.Session
import dev.cadence.data.local.SessionDao
import dev.cadence.data.local.SyncMeta
import dev.cadence.data.local.SyncMetaDao
import dev.cadence.data.local.SyncMetaKeys
import dev.cadence.data.local.SyncStatus
import dev.cadence.data.remote.SyncApi

/** Outcome of a sync pass, surfaced to the ViewModel for the UI's sync indicator. */
sealed interface SyncResult {
    data object Success : SyncResult
    data class Failure(val error: Throwable) : SyncResult
}

/**
 * The offline-first sync engine. It NEVER touches the UI and the UI never touches it — the UI only
 * ever observes Room. The engine's whole job is to (1) drain the outbox to the server and (2) feed
 * server changes back into Room. One `sync()` pass does push-then-pull.
 *
 * Push before pull: land this device's local changes on the server first, so the subsequent pull
 * returns the already-merged view (including the server's LWW verdict on anything that conflicted).
 */
class SyncEngine(
    private val database: AppDatabase,
    private val sessionDao: SessionDao,
    private val outboxDao: OutboxDao,
    private val syncMetaDao: SyncMetaDao,
    private val api: SyncApi,
) {

    suspend fun sync(): SyncResult =
        try {
            push()
            pull()
            SyncResult.Success
        } catch (error: Exception) {
            // Outbox rows and cursor are only advanced on success, so a failure just means the
            // next sync retries from where we left off. No partial/torn state.
            SyncResult.Failure(error)
        }

    /**
     * Drain the outbox. Map each pending session (including soft-deleted ones — a deletion is a
     * change that must propagate) to its DTO, push them, and ONLY on success remove the outbox
     * rows and flip the sessions to SYNCED — both in ONE Room transaction so the local "it's
     * synced" record can't drift from the outbox being cleared.
     */
    private suspend fun push() {
        val pending = outboxDao.getAll()
        if (pending.isEmpty()) return

        val sessionIds = pending.filter { it.entityType == "session" }
            .map { it.entityId }
            .distinct()
        val dtos = sessionIds.mapNotNull { id -> sessionDao.getById(id)?.toDto() }

        api.push(dtos) // throws on transport failure → caught by sync(); outbox stays intact

        database.useWriterConnection { connection ->
            connection.immediateTransaction {
                outboxDao.deleteByIds(pending.map { it.id })
                sessionDao.markStatus(sessionIds, SyncStatus.SYNCED)
            }
        }
    }

    /**
     * Pull everything changed since our cursor and merge it under Last-Write-Wins: a remote row is
     * applied only if we have no local copy or the remote `updatedAt` is strictly newer — so a
     * local edit that hasn't pushed yet is never clobbered by a stale server copy. Pulled writes
     * do NOT enqueue outbox rows (that would create a sync loop). Advance the cursor last.
     */
    private suspend fun pull() {
        val cursor = syncMetaDao.get(SyncMetaKeys.PULL_CURSOR)?.toLongOrNull()
        val response = api.pull(cursor)

        for (dto in response.changes) {
            val local = sessionDao.getById(dto.id)
            if (local == null || dto.updatedAt > local.updatedAt) {
                sessionDao.upsert(dto.toEntity())
            }
        }

        syncMetaDao.set(SyncMeta(SyncMetaKeys.PULL_CURSOR, response.nextCursor.toString()))
    }
}

private fun Session.toDto(): SessionDto =
    SessionDto(
        id = id,
        startedAt = startedAt,
        name = name,
        type = type,
        notes = notes,
        updatedAt = updatedAt,
        deleted = deleted,
    )

/** A pulled row is authoritative-from-server, so it lands already SYNCED (no outbox entry). */
private fun SessionDto.toEntity(): Session =
    Session(
        id = id,
        startedAt = startedAt,
        name = name,
        type = type,
        notes = notes,
        updatedAt = updatedAt,
        deleted = deleted,
        syncStatus = SyncStatus.SYNCED,
    )
