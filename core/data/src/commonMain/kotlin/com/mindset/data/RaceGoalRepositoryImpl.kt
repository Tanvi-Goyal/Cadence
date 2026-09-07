@file:OptIn(ExperimentalTime::class)

package com.mindset.data

import androidx.room3.immediateTransaction
import androidx.room3.useWriterConnection
import com.mindset.common.UuidGenerator
import com.mindset.data.local.AppDatabase
import com.mindset.data.local.OutboxEntry
import com.mindset.data.local.RaceGoalEntity
import com.mindset.data.local.SyncStatus
import com.mindset.domain.repository.RaceGoalRepository
import com.mindset.model.RaceGoal
import com.mindset.model.RaceGoalStatus
import com.mindset.model.RaceMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

class RaceGoalRepositoryImpl(private val database: AppDatabase, private val uuid: UuidGenerator, private val clock: Clock) : RaceGoalRepository {

    private val dao get() = database.raceGoalDao()
    private val outbox get() = database.outboxDao()

    private fun now(): Long = clock.now().toEpochMilliseconds()

    override fun observeUpcoming(): Flow<RaceGoal?> = dao.observeUpcoming().map { it?.toDomain() }

    override fun observeAll(): Flow<List<RaceGoal>> = dao.observeAll().map { rows -> rows.map { it.toDomain() } }

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
