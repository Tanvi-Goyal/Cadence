@file:OptIn(ExperimentalTime::class)

package dev.cadence.data

import androidx.room3.immediateTransaction
import androidx.room3.useWriterConnection
import dev.cadence.common.UuidGenerator
import dev.cadence.data.local.AppDatabase
import dev.cadence.data.local.OutboxEntry
import dev.cadence.data.local.RaceGoalEntity
import dev.cadence.data.local.SyncStatus
import dev.cadence.domain.repository.RaceGoalRepository
import dev.cadence.model.RaceGoal
import dev.cadence.model.RaceGoalStatus
import dev.cadence.model.RaceMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * Room-backed [RaceGoalRepository]. Follows the project's sync discipline: a goal is written and its
 * (single, deterministic) outbox row enqueued in ONE transaction — so Phase-2 sync adopts race goals
 * with no schema or write-path change, even though nothing drains the outbox in v1.
 */
class RaceGoalRepositoryImpl(
    private val database: AppDatabase,
    private val uuid: UuidGenerator,
    private val clock: Clock,
) : RaceGoalRepository {

    private val dao get() = database.raceGoalDao()
    private val outbox get() = database.outboxDao()

    private fun now(): Long = clock.now().toEpochMilliseconds()

    override fun observeUpcoming(): Flow<RaceGoal?> = dao.observeUpcoming().map { it?.toDomain() }

    override fun observeAll(): Flow<List<RaceGoal>> =
        dao.observeAll().map { rows -> rows.map { it.toDomain() } }

    override suspend fun create(
        formatKey: String,
        divisionKey: String,
        mode: RaceMode,
        targetDate: Instant?,
        city: String?,
        goalTimeSec: Int?,
    ): RaceGoal {
        val now = now()
        val entity = RaceGoalEntity(
            id = uuid.newId(),
            formatKey = formatKey,
            divisionKey = divisionKey,
            mode = mode.name,
            targetDate = targetDate?.toEpochMilliseconds(),
            city = city,
            goalTimeSec = goalTimeSec,
            status = RaceGoalStatus.UPCOMING.name,
            createdAt = now,
            updatedAt = now,
        )
        database.useWriterConnection { connection ->
            connection.immediateTransaction {
                dao.upsert(entity)
                enqueueOutbox(entity.id, now)
            }
        }
        return entity.toDomain()
    }

    override suspend fun updateStatus(id: String, status: RaceGoalStatus) {
        val current = dao.getById(id) ?: return
        val now = now()
        database.useWriterConnection { connection ->
            connection.immediateTransaction {
                dao.upsert(
                    current.copy(status = status.name, updatedAt = now, syncStatus = SyncStatus.PENDING),
                )
                enqueueOutbox(id, now)
            }
        }
    }

    /** One outbox row per goal (deterministic id) so repeated edits don't pile up rows. */
    private suspend fun enqueueOutbox(goalId: String, now: Long) {
        outbox.upsert(
            OutboxEntry(
                id = "race_goal:$goalId",
                entityType = "race_goal",
                entityId = goalId,
                opType = "UPSERT",
                payload = "",
                createdAt = now,
            ),
        )
    }
}

private fun RaceGoalEntity.toDomain(): RaceGoal = RaceGoal(
    id = id,
    formatKey = formatKey,
    divisionKey = divisionKey,
    mode = RaceMode.entries.firstOrNull { it.name == mode } ?: RaceMode.SINGLES,
    targetDate = targetDate?.let(Instant::fromEpochMilliseconds),
    city = city,
    goalTimeSec = goalTimeSec,
    status = RaceGoalStatus.entries.firstOrNull { it.name == status } ?: RaceGoalStatus.UPCOMING,
    createdAt = Instant.fromEpochMilliseconds(createdAt),
    updatedAt = Instant.fromEpochMilliseconds(updatedAt),
    deletedAt = deletedAt?.let(Instant::fromEpochMilliseconds),
)
