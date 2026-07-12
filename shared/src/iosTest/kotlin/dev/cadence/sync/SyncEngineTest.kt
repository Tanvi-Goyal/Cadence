package dev.cadence.sync

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import dev.cadence.contracts.PullResponse
import dev.cadence.contracts.PushResponse
import dev.cadence.contracts.SessionDto
import dev.cadence.data.SessionRepositoryImpl
import dev.cadence.data.local.AppDatabase
import dev.cadence.data.local.Session
import dev.cadence.data.local.SyncStatus
import dev.cadence.data.remote.SyncApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Exercises the common [SyncEngine] against a fake [SyncApi] (no live server). Lives in iosTest
 * because the no-arg in-memory Room builder is Context-free on native (same reason as the Phase 0
 * repository test).
 */
class SyncEngineTest {

    private lateinit var database: AppDatabase

    /** Fake transport: records what was pushed, returns a scripted pull payload. */
    private class FakeSyncApi(
        var pullChanges: List<SessionDto> = emptyList(),
        var nextCursor: Long = 0,
    ) : SyncApi {
        val pushed = mutableListOf<SessionDto>()
        override suspend fun push(changes: List<SessionDto>): PushResponse {
            pushed += changes
            return PushResponse(accepted = changes.size)
        }
        override suspend fun pull(cursor: Long?): PullResponse =
            PullResponse(changes = pullChanges, nextCursor = nextCursor)
    }

    private fun engineWith(api: SyncApi) = SyncEngine(
        database = database,
        sessionDao = database.sessionDao(),
        outboxDao = database.outboxDao(),
        syncMetaDao = database.syncMetaDao(),
        api = api,
    )

    @BeforeTest
    fun setup() {
        database = Room.inMemoryDatabaseBuilder<AppDatabase>()
            .setDriver(BundledSQLiteDriver())
            .build()
    }

    @AfterTest
    fun teardown() = database.close()

    @Test
    fun push_drains_outbox_and_marks_session_synced() = runTest {
        val repo = SessionRepositoryImpl(database)
        val created = repo.createSession() // enqueues an outbox row (Phase 0 behavior)
        assertEquals(1, database.outboxDao().count())

        val api = FakeSyncApi()
        engineWith(api).sync()

        assertEquals(1, api.pushed.size, "the pending session should have been pushed")
        assertEquals(created.id, api.pushed.first().id)
        assertEquals(0, database.outboxDao().count(), "outbox should be drained on success")
        assertEquals(SyncStatus.SYNCED, database.sessionDao().getById(created.id)?.syncStatus)
    }

    @Test
    fun pull_applies_newer_remote_row_and_ignores_stale_one() = runTest {
        val local = Session(id = "s1", startedAt = 1, updatedAt = 100, syncStatus = SyncStatus.SYNCED)
        database.sessionDao().upsert(local)

        // Remote sends a NEWER version (updatedAt 200) → should win.
        engineWith(FakeSyncApi(pullChanges = listOf(dto("s1", updatedAt = 200)), nextCursor = 1)).sync()
        assertEquals(200, database.sessionDao().getById("s1")?.updatedAt, "newer remote should apply")

        // Remote sends an OLDER version (updatedAt 50) → local (200) must be kept.
        engineWith(FakeSyncApi(pullChanges = listOf(dto("s1", updatedAt = 50)), nextCursor = 2)).sync()
        assertEquals(200, database.sessionDao().getById("s1")?.updatedAt, "stale remote must be ignored (LWW)")
    }

    @Test
    fun pulled_soft_delete_hides_row_from_the_observed_list() = runTest {
        val local = Session(id = "s1", startedAt = 1, updatedAt = 100, syncStatus = SyncStatus.SYNCED)
        database.sessionDao().upsert(local)
        assertEquals(1, database.sessionDao().observeAll().first().size)

        engineWith(FakeSyncApi(pullChanges = listOf(dto("s1", updatedAt = 300, deleted = true)), nextCursor = 1)).sync()

        assertTrue(
            database.sessionDao().observeAll().first().isEmpty(),
            "a pulled soft-delete should drop the row from the UI-facing query",
        )
    }

    @Test
    fun session_name_and_type_round_trip_through_push_and_pull() = runTest {
        val repo = SessionRepositoryImpl(database)
        val plan = dev.cadence.data.local.PlannedSession(
            id = "p1", name = "Leg Day", type = dev.cadence.data.local.SessionType.CONDITIONING,
            targetDurationMin = 40, focus = "Pull focus",
        )
        val created = repo.startPlannedSession(plan)

        val api = FakeSyncApi()
        engineWith(api).sync()

        // Push carries name/type over the wire.
        assertEquals("Leg Day", api.pushed.first().name)
        assertEquals(dev.cadence.data.local.SessionType.CONDITIONING, api.pushed.first().type)

        // Pull writes a remote name/type into the local entity.
        engineWith(
            FakeSyncApi(
                pullChanges = listOf(
                    SessionDto(id = "s2", startedAt = 1, name = "Hyrox Sim", type = "HYROX", updatedAt = 500),
                ),
                nextCursor = 5,
            ),
        ).sync()
        val pulled = database.sessionDao().getById("s2")
        assertEquals("Hyrox Sim", pulled?.name)
        assertEquals("HYROX", pulled?.type)
    }

    private fun dto(id: String, updatedAt: Long, deleted: Boolean = false) =
        SessionDto(id = id, startedAt = 1, notes = null, updatedAt = updatedAt, deleted = deleted)
}
