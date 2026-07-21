package dev.cadence.sync

import androidx.room3.immediateTransaction
import androidx.room3.useWriterConnection
import dev.cadence.contracts.LoggedItemDto
import dev.cadence.contracts.SessionDto
import dev.cadence.contracts.SetDto
import dev.cadence.data.implicitBlockId
import dev.cadence.data.local.AppDatabase
import dev.cadence.data.local.Block
import dev.cadence.data.local.ExerciseEntry
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
    private val blockDao get() = database.blockDao()
    private val entryDao get() = database.exerciseEntryDao()
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

    /**
     * Load a session and its children into the (still-flat) wire DTO. The block layer is flattened
     * away here — every entry across the session's blocks becomes a `LoggedItemDto` — because the
     * wire contract has no blocks yet (A8 adds them). Interim, and lossy on multi-block sessions,
     * but every session currently has a single implicit block, so nothing is lost today.
     */
    private suspend fun buildSessionDto(sessionId: String): SessionDto? {
        val session = sessionDao.getById(sessionId) ?: return null
        val items = entryDao.getBySession(sessionId).map { entry ->
            LoggedItemDto(
                exerciseId = entry.exerciseId,
                orderIndex = entry.orderIndex,
                sets = setEntryDao.getForEntry(entry.id).map { set ->
                    SetDto(
                        setNumber = set.setNumber,
                        reps = set.reps,
                        loadKg = set.loadKg,
                        timeSec = set.timeSec,
                        distanceM = set.distanceM,
                        rpe = set.rpe,
                        targetReps = set.targetReps,
                        targetLoadKg = set.targetLoadKg,
                        targetTimeSec = set.targetTimeSec,
                        targetDistanceM = set.targetDistanceM,
                    )
                },
            )
        }
        return SessionDto(
            id = session.id,
            startedAt = session.startedAt,
            name = session.name,
            type = session.type,
            notes = session.notes,
            isTemplate = session.isTemplate,
            source = session.source,
            templateId = session.templateId,
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
            isTemplate = dto.isTemplate,
            source = dto.source,
            templateId = dto.templateId,
            updatedAt = dto.updatedAt,
            deleted = dto.deleted,
            syncStatus = SyncStatus.SYNCED, // authoritative from server → already synced
        )
        // Rebuild the child rows locally with fresh ids under one implicit STRAIGHT block (children
        // are replaced wholesale, so ids are purely local — the flat wire contract carries none yet).
        val blockId = implicitBlockId(dto.id)
        val entriesWithSets = dto.loggedItems.map { itemDto ->
            val entryId = Uuid.random().toString()
            val entry = ExerciseEntry(
                id = entryId, blockId = blockId, exerciseId = itemDto.exerciseId,
                orderIndex = itemDto.orderIndex, createdAt = dto.updatedAt, updatedAt = dto.updatedAt,
            )
            val sets = itemDto.sets.map { setDto ->
                SetEntry(
                    id = Uuid.random().toString(),
                    exerciseEntryId = entryId,
                    setNumber = setDto.setNumber,
                    reps = setDto.reps,
                    loadKg = setDto.loadKg,
                    timeSec = setDto.timeSec,
                    distanceM = setDto.distanceM,
                    rpe = setDto.rpe,
                    targetReps = setDto.targetReps,
                    targetLoadKg = setDto.targetLoadKg,
                    targetTimeSec = setDto.targetTimeSec,
                    targetDistanceM = setDto.targetDistanceM,
                    createdAt = dto.updatedAt,
                    updatedAt = dto.updatedAt,
                )
            }
            entry to sets
        }
        database.useWriterConnection { connection ->
            connection.immediateTransaction {
                val oldEntryIds = entryDao.getBySession(dto.id).map { it.id }
                if (oldEntryIds.isNotEmpty()) setEntryDao.deleteForEntries(oldEntryIds)
                entryDao.deleteBySession(dto.id)
                blockDao.deleteBySession(dto.id)
                sessionDao.upsert(session)
                blockDao.insert(
                    Block(
                        id = blockId, sessionId = dto.id, type = "STRAIGHT", orderIndex = 0,
                        rounds = 1, createdAt = dto.updatedAt, updatedAt = dto.updatedAt,
                    ),
                )
                entriesWithSets.forEach { (entry, sets) ->
                    entryDao.insert(entry)
                    sets.forEach { setEntryDao.insert(it) }
                }
            }
        }
    }
}
