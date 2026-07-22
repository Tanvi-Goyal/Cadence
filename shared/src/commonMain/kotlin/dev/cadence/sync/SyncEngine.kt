package dev.cadence.sync

import androidx.room3.immediateTransaction
import androidx.room3.useWriterConnection
import dev.cadence.contracts.BlockDto
import dev.cadence.contracts.ExerciseEntryDto
import dev.cadence.contracts.SessionDto
import dev.cadence.contracts.SetDto
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

    /** Load a session and its full block tree into the wire DTO, preserving every node's id + envelope. */
    private suspend fun buildSessionDto(sessionId: String): SessionDto? {
        val session = sessionDao.getById(sessionId) ?: return null
        val entriesByBlock = entryDao.getBySession(sessionId).groupBy { it.blockId }
        val blocks = blockDao.getBySession(sessionId).map { block ->
            BlockDto(
                id = block.id,
                type = block.type,
                orderIndex = block.orderIndex,
                rounds = block.rounds,
                restBetweenRoundsMs = block.restBetweenRoundsMs,
                label = block.label,
                createdAt = block.createdAt,
                updatedAt = block.updatedAt,
                deletedAt = block.deletedAt,
                entries = entriesByBlock[block.id].orEmpty().map { entry ->
                    ExerciseEntryDto(
                        id = entry.id,
                        exerciseId = entry.exerciseId,
                        orderIndex = entry.orderIndex,
                        targetSets = entry.targetSets,
                        restMs = entry.restMs,
                        createdAt = entry.createdAt,
                        updatedAt = entry.updatedAt,
                        deletedAt = entry.deletedAt,
                        sets = setEntryDao.getForEntry(entry.id).map { it.toDto() },
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
            createdAt = session.createdAt,
            updatedAt = session.updatedAt,
            deletedAt = session.deletedAt,
            isTemplate = session.isTemplate,
            source = session.source,
            templateId = session.templateId,
            blocks = blocks,
        )
    }

    /**
     * Replace a session + its children with the remote aggregate, atomically. Children are rebuilt
     * from the wire using their OWN ids + envelopes (no local id minting) — the aggregate is still
     * replaced wholesale, but identity is now stable across the round-trip.
     */
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
            createdAt = dto.createdAt,
            updatedAt = dto.updatedAt,
            deletedAt = dto.deletedAt,
            syncStatus = SyncStatus.SYNCED, // authoritative from server → already synced
        )
        database.useWriterConnection { connection ->
            connection.immediateTransaction {
                val oldEntryIds = entryDao.getBySession(dto.id).map { it.id }
                if (oldEntryIds.isNotEmpty()) setEntryDao.deleteForEntries(oldEntryIds)
                entryDao.deleteBySession(dto.id)
                blockDao.deleteBySession(dto.id)
                sessionDao.upsert(session)
                dto.blocks.forEach { blockDto ->
                    blockDao.insert(
                        Block(
                            id = blockDto.id, sessionId = dto.id, type = blockDto.type,
                            orderIndex = blockDto.orderIndex, rounds = blockDto.rounds,
                            restBetweenRoundsMs = blockDto.restBetweenRoundsMs, label = blockDto.label,
                            createdAt = blockDto.createdAt, updatedAt = blockDto.updatedAt,
                            deletedAt = blockDto.deletedAt,
                        ),
                    )
                    blockDto.entries.forEach { entryDto ->
                        entryDao.insert(
                            ExerciseEntry(
                                id = entryDto.id, blockId = blockDto.id, exerciseId = entryDto.exerciseId,
                                orderIndex = entryDto.orderIndex, targetSets = entryDto.targetSets,
                                restMs = entryDto.restMs, createdAt = entryDto.createdAt,
                                updatedAt = entryDto.updatedAt, deletedAt = entryDto.deletedAt,
                            ),
                        )
                        entryDto.sets.forEach { setEntryDao.insert(it.toEntity(entryDto.id)) }
                    }
                }
            }
        }
    }

    private fun SetEntry.toDto(): SetDto = SetDto(
        id = id, setNumber = setNumber, reps = reps, loadKg = loadKg, timeSec = timeSec,
        distanceM = distanceM, calories = calories, rpe = rpe, targetReps = targetReps,
        targetLoadKg = targetLoadKg, targetTimeSec = targetTimeSec, targetDistanceM = targetDistanceM,
        targetCalories = targetCalories, createdAt = createdAt, updatedAt = updatedAt, deletedAt = deletedAt,
    )

    private fun SetDto.toEntity(exerciseEntryId: String): SetEntry = SetEntry(
        id = id, exerciseEntryId = exerciseEntryId, setNumber = setNumber, reps = reps, loadKg = loadKg,
        timeSec = timeSec, distanceM = distanceM, rpe = rpe, targetReps = targetReps,
        targetLoadKg = targetLoadKg, targetTimeSec = targetTimeSec, targetDistanceM = targetDistanceM,
        calories = calories, targetCalories = targetCalories, createdAt = createdAt, updatedAt = updatedAt,
        deletedAt = deletedAt,
    )
}
