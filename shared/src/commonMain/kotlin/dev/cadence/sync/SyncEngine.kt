package dev.cadence.sync

import androidx.room3.immediateTransaction
import androidx.room3.useWriterConnection
import dev.cadence.contracts.LoggedItemDto
import dev.cadence.contracts.SessionDto
import dev.cadence.contracts.SetDto
import dev.cadence.data.local.AppDatabase
import dev.cadence.data.local.LoggedItem
import dev.cadence.data.local.OutboxDao
import dev.cadence.data.local.Session
import dev.cadence.data.local.SessionDao
import dev.cadence.data.local.SetEntry
import dev.cadence.data.local.SyncMeta
import dev.cadence.data.local.SyncMetaDao
import dev.cadence.data.local.SyncMetaKeys
import dev.cadence.data.local.SyncStatus
import dev.cadence.data.remote.SyncApi
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

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
 * **Aggregate sync**: the session is the sync unit; its logged items + sets travel with it as one
 * document. A session is a bounded aggregate edited by effectively one device at a time, so this
 * avoids per-set outbox rows and per-set conflict resolution — aggregate Last-Write-Wins by
 * `Session.updatedAt` is correct and far simpler. Exercises aren't synced (seeded on every device).
 */
class SyncEngine(
    private val database: AppDatabase,
    private val sessionDao: SessionDao,
    private val outboxDao: OutboxDao,
    private val syncMetaDao: SyncMetaDao,
    private val api: SyncApi,
) {
    private val loggedItemDao get() = database.loggedItemDao()
    private val setEntryDao get() = database.setEntryDao()

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
     * Drain the outbox. Build each pending session's full aggregate DTO (session + its logged
     * items + sets), push them, and ONLY on success remove the outbox rows and flip the sessions
     * to SYNCED — both in ONE Room transaction so the local "it's synced" record can't drift from
     * the outbox being cleared.
     */
    private suspend fun push() {
        val pending = outboxDao.getAll()
        if (pending.isEmpty()) return

        val sessionIds = pending.filter { it.entityType == "session" }
            .map { it.entityId }
            .distinct()
        val dtos = sessionIds.mapNotNull { id -> buildSessionDto(id) }

        api.push(dtos) // throws on transport failure → caught by sync(); outbox stays intact

        database.useWriterConnection { connection ->
            connection.immediateTransaction {
                outboxDao.deleteByIds(pending.map { it.id })
                sessionDao.markStatus(sessionIds, SyncStatus.SYNCED)
            }
        }
    }

    /**
     * Pull everything changed since our cursor and merge under Last-Write-Wins: a remote session
     * is applied only if we have no local copy or the remote `updatedAt` is strictly newer. When
     * it wins, the session row AND its children are replaced wholesale (delete local items/sets,
     * insert the remote ones) in one transaction — that's the aggregate write. Pulled writes do
     * NOT enqueue outbox rows (that would loop). Advance the cursor last.
     */
    private suspend fun pull() {
        val cursor = syncMetaDao.get(SyncMetaKeys.PULL_CURSOR)?.toLongOrNull()
        val response = api.pull(cursor)

        for (dto in response.changes) {
            val local = sessionDao.getById(dto.id)
            if (local == null || dto.updatedAt > local.updatedAt) {
                applyRemoteSession(dto)
            }
        }

        syncMetaDao.set(SyncMeta(SyncMetaKeys.PULL_CURSOR, response.nextCursor.toString()))
    }

    /** Load a session and its children into the nested wire DTO. */
    private suspend fun buildSessionDto(sessionId: String): SessionDto? {
        val session = sessionDao.getById(sessionId) ?: return null
        val items = loggedItemDao.getBySession(sessionId).map { item ->
            LoggedItemDto(
                exerciseId = item.exerciseId,
                orderIndex = item.orderIndex,
                sets = setEntryDao.getForLoggedItem(item.id).map { set ->
                    SetDto(set.setNumber, set.reps, set.loadKg, set.timeSec, set.distanceM, set.rpe)
                },
            )
        }
        return SessionDto(
            id = session.id,
            startedAt = session.startedAt,
            name = session.name,
            type = session.type,
            notes = session.notes,
            updatedAt = session.updatedAt,
            deleted = session.deleted,
            loggedItems = items,
        )
    }

    /** Replace a session + its children with the remote aggregate, atomically. */
    @OptIn(ExperimentalUuidApi::class)
    private suspend fun applyRemoteSession(dto: SessionDto) {
        val session = Session(
            id = dto.id,
            startedAt = dto.startedAt,
            name = dto.name,
            type = dto.type,
            notes = dto.notes,
            updatedAt = dto.updatedAt,
            deleted = dto.deleted,
            syncStatus = SyncStatus.SYNCED, // authoritative from server → already synced
        )
        // Rebuild the child rows locally with fresh ids (children are replaced wholesale, so ids
        // are purely local — the wire contract carries no child ids).
        val itemsWithSets = dto.loggedItems.map { itemDto ->
            val itemId = Uuid.random().toString()
            val item = LoggedItem(itemId, dto.id, itemDto.exerciseId, itemDto.orderIndex)
            val sets = itemDto.sets.map { setDto ->
                SetEntry(
                    id = Uuid.random().toString(),
                    loggedItemId = itemId,
                    setNumber = setDto.setNumber,
                    reps = setDto.reps,
                    loadKg = setDto.loadKg,
                    timeSec = setDto.timeSec,
                    distanceM = setDto.distanceM,
                    rpe = setDto.rpe,
                )
            }
            item to sets
        }
        database.useWriterConnection { connection ->
            connection.immediateTransaction {
                val oldItemIds = loggedItemDao.getBySession(dto.id).map { it.id }
                if (oldItemIds.isNotEmpty()) setEntryDao.deleteForLoggedItems(oldItemIds)
                loggedItemDao.deleteBySession(dto.id)
                sessionDao.upsert(session)
                itemsWithSets.forEach { (item, sets) ->
                    loggedItemDao.insert(item)
                    sets.forEach { setEntryDao.insert(it) }
                }
            }
        }
    }
}
