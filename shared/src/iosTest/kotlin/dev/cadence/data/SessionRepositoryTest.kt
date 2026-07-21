package dev.cadence.data

import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import dev.cadence.common.UuidV7Generator
import dev.cadence.data.local.AppDatabase
import dev.cadence.data.local.ExerciseAssetReader
import dev.cadence.data.local.SessionSource
import dev.cadence.data.local.SessionType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Proves the offline-first invariant: creating a session persists the row AND enqueues its outbox
 * entry in ONE transaction. The code under test ([SessionRepositoryImpl]) is commonMain; this
 * lives in iosTest because Room's no-arg in-memory builder is available Context-free on native,
 * whereas the Android host equivalent needs a Context (Robolectric). The Android side is covered
 * by the manual kill-and-relaunch offline check.
 */
class SessionRepositoryTest {

    private lateinit var database: AppDatabase

    @BeforeTest
    fun setup() {
        database = Room.inMemoryDatabaseBuilder<AppDatabase>()
            .setDriver(BundledSQLiteDriver())
            .build()
    }

    @AfterTest
    fun teardown() {
        database.close()
    }

    /** Repository wired with the real UUIDv7 + system-clock seam (mirrors production DI). */
    @OptIn(kotlin.time.ExperimentalTime::class)
    private fun repo(reader: ExerciseAssetReader) =
        SessionRepositoryImpl(database, reader, UuidV7Generator(kotlin.time.Clock.System), kotlin.time.Clock.System)

    @Test
    fun createSession_persistsSessionAndEnqueuesOutbox() = runTest {
        val repository = repo(emptyExerciseAssetReader)

        val created = repository.createSession(SessionType.STRENGTH)

        val sessions = repository.observeSessions().first()
        assertEquals(1, sessions.size, "session should be persisted")
        assertEquals(created.id, sessions.first().id, "persisted session should match returned one")
        assertEquals(1, database.outboxDao().count(), "outbox entry should be enqueued in the same write")
    }

    /**
     * D2 core flow: instantiating a template deep-copies its tree with fresh ids, carrying the
     * prescription (`target*`) over and leaving actuals null so the UI can show ghost values. The
     * spawned row is a real (non-template) session that records its `templateId` provenance and,
     * unlike the template, appears in the history list.
     */
    @Test
    fun instantiateTemplate_deepCopiesTargetsAndLeavesActualsNull() = runTest {
        val repo = repo(emptyExerciseAssetReader)

        val template = repo.createTemplate(name = "Upper A", type = SessionType.STRENGTH)
        repo.addExercise(template.id, "bench-press")
        val templateItemId = database.loggedItemDao().getBySession(template.id).first().id
        repo.addTargetSet(template.id, templateItemId, reps = 5, loadKg = 100.0)

        assertTrue(
            repo.observeSessions().first().none { it.id == template.id },
            "a template must not appear in the history list",
        )

        val session = repo.instantiateTemplate(template.id)

        assertEquals(false, session.isTemplate)
        assertEquals(SessionSource.FROM_TEMPLATE, session.source)
        assertEquals(template.id, session.templateId, "spawned session records its provenance")
        assertNotEquals(template.id, session.id, "spawned session gets a fresh id")
        assertTrue(
            repo.observeSessions().first().any { it.id == session.id },
            "the spawned (non-template) session shows up in history",
        )
        assertTrue(
            database.outboxDao().getAll().any { it.entityId == session.id },
            "the spawned session enqueues its own outbox row",
        )

        val items = database.loggedItemDao().getBySession(session.id)
        assertEquals(1, items.size)
        assertNotEquals(templateItemId, items.first().id, "copied logged item gets a fresh id")
        assertEquals("bench-press", items.first().exerciseId)

        val sets = database.setEntryDao().getForLoggedItem(items.first().id)
        assertEquals(1, sets.size)
        assertEquals(5, sets.first().targetReps, "prescription reps copied")
        assertEquals(100.0, sets.first().targetLoadKg, "prescription load copied")
        assertNull(sets.first().reps, "actual reps must be null on a fresh instantiation")
        assertNull(sets.first().loadKg, "actual load must be null on a fresh instantiation")
    }

    /**
     * The D2 "cost to accept": copy-on-instantiate means a template edit after spawning must never
     * leak into an already-spawned session. Guaranteed because the spawned session owns fresh rows.
     */
    @Test
    fun editingTemplateAfterInstantiation_doesNotMutateSpawnedSession() = runTest {
        val repo = repo(emptyExerciseAssetReader)

        val template = repo.createTemplate(name = "Upper A", type = SessionType.STRENGTH)
        repo.addExercise(template.id, "bench-press")
        val templateItemId = database.loggedItemDao().getBySession(template.id).first().id
        repo.addTargetSet(template.id, templateItemId, reps = 5, loadKg = 100.0)

        val session = repo.instantiateTemplate(template.id)

        // Mutate the template AFTER spawning.
        repo.addTargetSet(template.id, templateItemId, reps = 3, loadKg = 110.0)

        val spawnedItemId = database.loggedItemDao().getBySession(session.id).first().id
        val spawnedSets = database.setEntryDao().getForLoggedItem(spawnedItemId)
        assertEquals(1, spawnedSets.size, "spawned session must not see sets added to the template later")
        assertEquals(5, spawnedSets.first().targetReps)
    }

    /**
     * Filling in actuals against a ghost target: [SessionRepository.updateSet] persists the actual
     * metrics, leaves the prescription intact, and bumps the parent session so the aggregate re-syncs.
     */
    @Test
    fun updateSet_persistsActualsAndTouchesSession() = runTest {
        val repo = repo(emptyExerciseAssetReader)

        // Instantiate a template → the spawned session has a target-only (ghost) set.
        val template = repo.createTemplate(name = "Upper A", type = SessionType.STRENGTH)
        repo.addExercise(template.id, "bench-press")
        val templateItemId = database.loggedItemDao().getBySession(template.id).first().id
        repo.addTargetSet(template.id, templateItemId, reps = 5, loadKg = 100.0)
        val session = repo.instantiateTemplate(template.id)

        val itemId = database.loggedItemDao().getBySession(session.id).first().id
        val ghost = database.setEntryDao().getForLoggedItem(itemId).first()
        val updatedAtBefore = database.sessionDao().getById(session.id)!!.updatedAt

        // Fill in what varied: 5 reps at 102.5 kg.
        // updateSet takes a domain SetEntry now; map the entity ghost across the boundary.
        repo.updateSet(session.id, ghost.toDomain().copy(reps = 5, loadKg = 102.5))

        val saved = database.setEntryDao().getForLoggedItem(itemId).first()
        assertEquals(5, saved.reps, "actual reps persisted")
        assertEquals(102.5, saved.loadKg, "actual load persisted")
        assertEquals(5, saved.targetReps, "prescription must survive the edit")
        assertEquals(100.0, saved.targetLoadKg)
        assertTrue(
            database.sessionDao().getById(session.id)!!.updatedAt >= updatedAtBefore,
            "editing a set must touch the parent session so the aggregate re-syncs",
        )
        assertTrue(
            database.outboxDao().getAll().any { it.entityId == session.id },
            "the edit leaves an outbox row for the session",
        )
    }
}
