package com.mindset.data

import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.mindset.common.UuidV7Generator
import com.mindset.data.local.AppDatabase
import com.mindset.data.local.BlockEntity
import com.mindset.data.local.ExerciseAssetReader
import com.mindset.data.local.ExerciseEntryEntity
import com.mindset.data.local.SessionType
import com.mindset.data.local.SetEntryEntity
import com.mindset.model.Gender
import com.mindset.model.HyroxStation
import com.mindset.model.HyroxVariant
import com.mindset.model.RaceMode
import com.mindset.model.PrKind
import com.mindset.model.SessionSource
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import com.mindset.data.local.SessionEntity as SessionEntity

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

        // Asserted against the row, not the history feed: the feed deliberately hides a session with
        // nothing logged in it (see the next two tests), so it can't stand in for "was persisted".
        val row = database.sessionDao().getById(created.id)
        assertEquals(created.id, row?.id, "session should be persisted")
        assertEquals(1, database.outboxDao().count(), "outbox entry should be enqueued in the same write")
    }

    /**
     * The quick-start FAB persists the session row *before* Log Session opens, so an abandoned open
     * would otherwise surface as an empty session. The live feeds require a session to be substantive
     * — finished, or carrying at least one live entry — which is the safety net for rows the exit path
     * misses (process death, rows predating the fix).
     */
    @Test
    fun observeSessions_hidesSessionWithNothingLogged() = runTest {
        val repo = repo(emptyExerciseAssetReader)

        val session = repo.createSession(SessionType.STRENGTH)

        assertTrue(
            repo.observeSessions().first().none { it.id == session.id },
            "a session with no entries must not appear in history",
        )
        assertTrue(
            repo.observeRecentSessions(limit = 4).first().none { it.id == session.id },
            "nor in Home's recent widget",
        )

        repo.updateSessionNotes(session.id, "felt strong")

        assertTrue(
            repo.observeSessions().first().none { it.id == session.id },
            "a note alone doesn't make it a session either",
        )

        repo.addExercise(session.id, "bench-press")

        assertTrue(
            repo.observeSessions().first().any { it.id == session.id },
            "logging anything into it makes it substantive",
        )
    }

    /**
     * The write-path half of the same fix: backing out of an untouched session drops the row outright
     * (tombstone + outbox touch), so it never reaches the server either. Guarded so it can only ever
     * discard a session that is genuinely untouched.
     */
    @Test
    fun discardSessionIfEmpty_dropsUntouchedSessionOnly() = runTest {
        val repo = repo(emptyExerciseAssetReader)

        val untouched = repo.createSession(SessionType.STRENGTH)
        assertTrue(repo.discardSessionIfEmpty(untouched.id), "an untouched session is discarded")
        assertTrue(
            database.sessionDao().getById(untouched.id)?.deletedAt != null,
            "discarding tombstones the row so the deletion re-syncs",
        )
        assertTrue(
            database.outboxDao().getAll().any { it.entityId == untouched.id },
            "the tombstone leaves an outbox row",
        )
        assertTrue(!repo.discardSessionIfEmpty(untouched.id), "idempotent — already discarded")

        val withEntry = repo.createSession(SessionType.STRENGTH)
        repo.addExercise(withEntry.id, "bench-press")
        assertTrue(!repo.discardSessionIfEmpty(withEntry.id), "a logged session is kept")

        val notesOnly = repo.createSession(SessionType.STRENGTH)
        repo.updateSessionNotes(notesOnly.id, "felt strong")
        assertTrue(
            repo.discardSessionIfEmpty(notesOnly.id),
            "a note against nothing logged is still not a session",
        )

        val template = repo.createTemplate(name = "Upper A", type = SessionType.STRENGTH)
        assertTrue(!repo.discardSessionIfEmpty(template.id), "templates are never touched")
    }

    /**
     * D2 core flow: instantiating a template deep-copies its tree with fresh ids, carrying the
     * prescription (`target*`) over and leaving actuals null so the UI can show ghost values. The
     * spawned row is a real (non-template) session that records its `templateId` provenance and,
     * unlike the template, appears in the history list.
     */
    /**
     * Quick-add seeds a *whole* race, so a second pick has to swap the session's contents rather than
     * stack a second race on top of the first (the append default stays for the add-a-station path).
     * Replacing clears everything logged — including hand-added exercises — and re-indexes from 0.
     */
    @Test
    fun addHyroxVariant_replaceExisting_swapsTheRaceInsteadOfAppending() = runTest {
        val repo = repo(emptyExerciseAssetReader)
        val session = repo.createSession(SessionType.HYROX)

        repo.addHyroxVariant(session.id, "MEN", HyroxVariant.FULL, RaceMode.SINGLES, Gender.MEN)
        val full = database.exerciseEntryDao().getBySession(session.id)
        assertTrue(full.isNotEmpty(), "the full race seeded its segments")

        // A hand-added exercise is part of "everything logged" and must go too.
        repo.addExercise(session.id, "bench-press")
        val staleIds = database.exerciseEntryDao().getBySession(session.id).map { it.id }.toSet()

        repo.addHyroxVariant(
            session.id,
            "MEN",
            HyroxVariant.FIRST_HALF,
            RaceMode.SINGLES,
            Gender.MEN,
            replaceExisting = true,
        )

        val after = database.exerciseEntryDao().getBySession(session.id)
        val expected = repo.hyroxFormat("MEN", HyroxVariant.FIRST_HALF, RaceMode.SINGLES, Gender.MEN)
            .count { it.segmentKey != null }
        assertEquals(expected, after.size, "only the new variant's segments remain")
        assertTrue(after.none { it.id in staleIds }, "every previous entry was tombstoned")
        assertEquals(
            List(after.size) { it },
            after.map { it.orderIndex },
            "the new race re-indexes from 0 rather than continuing past the cleared entries",
        )
        assertTrue(
            staleIds.all { database.setEntryDao().getForEntry(it).isEmpty() },
            "the cleared entries' sets are tombstoned too, so no orphans survive",
        )
        assertTrue(
            database.outboxDao().getAll().any { it.entityId == session.id },
            "the swap leaves an outbox row so it re-syncs",
        )
    }

    /** The default stays append — adding a station must not wipe the race already in the session. */
    @Test
    fun addHyroxVariant_withoutReplace_appendsAfterExistingEntries() = runTest {
        val repo = repo(emptyExerciseAssetReader)
        val session = repo.createSession(SessionType.HYROX)

        repo.addHyroxVariant(session.id, "MEN", HyroxVariant.FIRST_HALF, RaceMode.SINGLES, Gender.MEN)
        val first = database.exerciseEntryDao().getBySession(session.id)
        repo.addHyroxVariant(session.id, "MEN", HyroxVariant.SECOND_HALF, RaceMode.SINGLES, Gender.MEN)

        val after = database.exerciseEntryDao().getBySession(session.id)
        assertTrue(after.size > first.size, "the second variant appended")
        assertTrue(first.all { old -> after.any { it.id == old.id } }, "the first half survived")
        assertEquals(
            List(after.size) { it },
            after.map { it.orderIndex },
            "order stays contiguous across the two adds",
        )
    }

    /**
     * Total Time is the sum of the logged splits, and a Hyrox race is half running — so the run
     * segments have to count. The old wall-clock derivation (`finishedAt − startedAt`) counted
     * neither, which is what made a logged session read as seconds long.
     */
    @Test
    fun observeDurationsBySession_sumsRunAndStationSplits() = runTest {
        val repo = repo(emptyExerciseAssetReader)
        val session = repo.createSession(SessionType.HYROX)
        repo.addHyroxVariant(session.id, "MEN", HyroxVariant.FIRST_HALF, RaceMode.SINGLES, Gender.MEN)

        val entries = database.exerciseEntryDao().getBySession(session.id)
        val run = entries.first { it.segmentKey?.contains("run") == true }
        val station = entries.first { it.segmentKey?.contains("run") != true }

        suspend fun logSplit(entryId: String, seconds: Int) {
            val set = database.setEntryDao().getForEntry(entryId).first()
            repo.updateSet(session.id, set.toDomain().copy(timeSec = seconds))
        }
        logSplit(run.id, 260)
        logSplit(station.id, 95)

        assertEquals(
            355,
            repo.observeDurationsBySession().first()[session.id],
            "the run split (260s) must be counted alongside the station split (95s)",
        )

        // Removing a segment retracts its split — the aggregate follows the tombstones.
        repo.removeEntry(session.id, run.id)
        assertEquals(
            95,
            repo.observeDurationsBySession().first()[session.id],
            "a tombstoned segment stops contributing",
        )
    }

    /** A strength session has no splits at all, so it reports no duration rather than a bogus one. */
    @Test
    fun observeDurationsBySession_omitsUntimedSession() = runTest {
        val repo = repo(emptyExerciseAssetReader)
        val session = repo.createSession(SessionType.STRENGTH)
        repo.addExercise(session.id, "bench-press")
        val entry = database.exerciseEntryDao().getBySession(session.id).first().id
        repo.addSet(session.id, entry, reps = 5, loadKg = 100.0)

        assertNull(
            repo.observeDurationsBySession().first()[session.id],
            "no timed sets → no duration (the row shows volume instead)",
        )
    }

    /**
     * PB bucketing reads `sessions.divisionKey`, which nothing used to write — so every record was
     * stored division-less and "Sled Push @ Men" could never be a separate record from another
     * division. Adding Hyrox content now stamps the session, and the PB inherits it.
     */
    @Test
    fun addingHyroxContent_stampsDivisionSoPbsAreBucketed() = runTest {
        val repo = repo(emptyExerciseAssetReader)
        val session = repo.createSession(SessionType.HYROX)
        assertNull(database.sessionDao().getById(session.id)?.divisionKey, "unstamped to begin with")

        val sledPush = repo.hyroxStations("MEN", RaceMode.SINGLES, Gender.MEN)
            .first { it.segmentKey?.contains("sled-push") == true }
        repo.addStation(session.id, "MEN", sledPush.segmentKey!!, RaceMode.SINGLES, Gender.MEN)

        assertEquals("MEN", database.sessionDao().getById(session.id)?.divisionKey)

        // Logging an actual time against it produces a PB carrying that division.
        val entry = database.exerciseEntryDao().getBySession(session.id).first()
        val set = database.setEntryDao().getForEntry(entry.id).first()
        repo.updateSet(session.id, set.toDomain().copy(timeSec = 165, distanceM = 50))

        val prs = database.personalRecordDao().getForExercise(entry.exerciseId)
        val best = prs.first { it.kind == PrKind.BEST_TIME.name }
        assertEquals("MEN", best.divisionKey, "the PB is bucketed by the session's division")
        assertEquals(50, best.distanceBucketM, "and by the exact distance")
        assertEquals(165.0, best.value)
    }

    /** First write wins: switching division later must not re-bucket a session already being logged. */
    @Test
    fun stampDivision_keepsTheFirstDivisionUnlessContentIsReplaced() = runTest {
        val repo = repo(emptyExerciseAssetReader)
        val session = repo.createSession(SessionType.HYROX)

        repo.addHyroxVariant(session.id, "MEN", HyroxVariant.FIRST_HALF, RaceMode.SINGLES, Gender.MEN)
        assertEquals("MEN", database.sessionDao().getById(session.id)?.divisionKey)

        // Appending more content under a different division leaves the original stamp alone.
        val station = repo.hyroxStations("WOMEN", RaceMode.SINGLES, Gender.WOMEN)
            .first { it.segmentKey?.contains("sled-push") == true }
        repo.addStation(session.id, "WOMEN", station.segmentKey!!, RaceMode.SINGLES, Gender.WOMEN)
        assertEquals(
            "MEN",
            database.sessionDao().getById(session.id)?.divisionKey,
            "appending must not retroactively re-bucket what is already logged",
        )

        // Replacing the session's contents re-seeds it, so the stamp follows.
        repo.addHyroxVariant(
            session.id,
            "WOMEN",
            HyroxVariant.FIRST_HALF,
            RaceMode.SINGLES,
            Gender.WOMEN,
            replaceExisting = true,
        )
        assertEquals(
            "WOMEN",
            database.sessionDao().getById(session.id)?.divisionKey,
            "a wholesale replace re-stamps — nothing is left to stay consistent with",
        )
    }

    /**
     * The Station board's RECENT + SESSIONS columns. Both are scoped to one division so a card can't
     * pair a Women's recent with a Men's PB, RECENT is the newest *logged* split, and SESSIONS counts
     * only sessions that actually logged a time — a station merely added and left blank must not count.
     */
    @Test
    fun observeStationAggregates_scopesToDivisionAndCountsActualLogsOnly() = runTest {
        val repo = repo(emptyExerciseAssetReader)

        suspend fun backdate(sessionId: String, startedAt: Long) {
            val row = database.sessionDao().getById(sessionId)!!
            database.sessionDao().upsert(row.copy(startedAt = startedAt))
        }

        suspend fun logSledPush(divisionKey: String, gender: Gender, seconds: Int?, startedAt: Long): String {
            val session = repo.createSession(SessionType.HYROX)
            val segment = repo.hyroxStations(divisionKey, RaceMode.SINGLES, gender)
                .first { it.segmentKey?.contains("sled-push") == true }
            repo.addStation(session.id, divisionKey, segment.segmentKey!!, RaceMode.SINGLES, gender)
            if (seconds != null) {
                val entry = database.exerciseEntryDao().getBySession(session.id).first()
                val set = database.setEntryDao().getForEntry(entry.id).first()
                repo.updateSet(session.id, set.toDomain().copy(timeSec = seconds, distanceM = 50))
            }
            backdate(session.id, startedAt) // explicit ordering — creates land in the same millisecond
            return session.id
        }

        logSledPush("MEN", Gender.MEN, seconds = 200, startedAt = 1_000)
        logSledPush("MEN", Gender.MEN, seconds = 180, startedAt = 2_000)

        val men = repo.observeStationAggregates("MEN").first()[HyroxStation.SLED_PUSH]
        assertEquals(180, men?.recentTimeSec, "RECENT is the newest logged split, not the fastest")
        assertEquals(200, men?.previousTimeSec, "PREVIOUS is the session before it — the trend baseline")
        assertEquals(2, men?.sessionCount)

        // A different division must not leak into this card.
        logSledPush("WOMEN", Gender.WOMEN, seconds = 100, startedAt = 3_000)
        val menAfterWomen = repo.observeStationAggregates("MEN").first()[HyroxStation.SLED_PUSH]
        assertEquals(180, menAfterWomen?.recentTimeSec, "a Women's session must not become the Men's recent")
        assertEquals(2, menAfterWomen?.sessionCount)
        assertEquals(
            100,
            repo.observeStationAggregates("WOMEN").first()[HyroxStation.SLED_PUSH]?.recentTimeSec,
            "and the Women's board reads its own split",
        )

        // Added but never logged → not a session for this station.
        logSledPush("MEN", Gender.MEN, seconds = null, startedAt = 4_000)
        val menAfterBlank = repo.observeStationAggregates("MEN").first()[HyroxStation.SLED_PUSH]
        assertEquals(2, menAfterBlank?.sessionCount, "an unlogged station must not inflate the count")
        assertEquals(180, menAfterBlank?.recentTimeSec)
    }

    /**
     * The board shows effort **and** weight, so the reference model has to carry the division's
     * rulebook load string — "2×24 kg" for Farmers Carry, not a misleading single "24 kg".
     */
    @Test
    fun hyroxStations_exposeTheDivisionsRulebookLoadString() = runTest {
        val repo = repo(emptyExerciseAssetReader)
        val stations = repo.hyroxStations("MEN", RaceMode.SINGLES, Gender.MEN)

        val sledPush = stations.first { it.segmentKey?.contains("sled-push") == true }
        assertEquals(50, sledPush.targetDistanceM)
        assertTrue(
            sledPush.loadDisplay?.contains("kg") == true,
            "a weighted station carries its load string: ${sledPush.loadDisplay}",
        )

        val farmers = stations.first { it.segmentKey?.contains("farmers") == true }
        assertTrue(
            farmers.loadDisplay?.contains("×") == true,
            "farmers carry keeps the two-implement form: ${farmers.loadDisplay}",
        )

        // Wall Balls is rep-scored but still has a ball weight, so it gets a load string too.
        val wallBalls = stations.first { it.segmentKey?.contains("wall-ball") == true }
        assertEquals(100, wallBalls.targetReps)
        assertTrue(
            wallBalls.loadDisplay?.contains("kg") == true,
            "wall balls expose the ball weight: ${wallBalls.loadDisplay}",
        )
    }

    @Test
    fun instantiateTemplate_deepCopiesTargetsAndLeavesActualsNull() = runTest {
        val repo = repo(emptyExerciseAssetReader)

        val template = repo.createTemplate(name = "Upper A", type = SessionType.STRENGTH)
        repo.addExercise(template.id, "bench-press")
        val templateItemId = database.exerciseEntryDao().getBySession(template.id).first().id
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

        val items = database.exerciseEntryDao().getBySession(session.id)
        assertEquals(1, items.size)
        assertNotEquals(templateItemId, items.first().id, "copied logged item gets a fresh id")
        assertEquals("bench-press", items.first().exerciseId)

        val sets = database.setEntryDao().getForEntry(items.first().id)
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
        val templateItemId = database.exerciseEntryDao().getBySession(template.id).first().id
        repo.addTargetSet(template.id, templateItemId, reps = 5, loadKg = 100.0)

        val session = repo.instantiateTemplate(template.id)

        // Mutate the template AFTER spawning.
        repo.addTargetSet(template.id, templateItemId, reps = 3, loadKg = 110.0)

        val spawnedItemId = database.exerciseEntryDao().getBySession(session.id).first().id
        val spawnedSets = database.setEntryDao().getForEntry(spawnedItemId)
        assertEquals(1, spawnedSets.size, "spawned session must not see sets added to the template later")
        assertEquals(5, spawnedSets.first().targetReps)
    }

    /**
     * A seeded program template ([TemplateSeed]) has MANY blocks (warm-up / main / … / conditioning),
     * not the single implicit block a user-built template has. Instantiating one must deep-copy the
     * whole block graph — preserving each block's section + conditioning shape and each entry's
     * note/each-side — with all-fresh ids, not collapse it into one block.
     */
    @Test
    fun instantiateTemplate_preservesMultiBlockStructure() = runTest {
        val repo = repo(emptyExerciseAssetReader)
        // Seed a 2-block template directly via DAOs (the repo's addExercise only builds one implicit block).
        database.sessionDao().insert(
            SessionEntity(id = "t1", startedAt = 0, name = "Day", type = SessionType.STRENGTH, isTemplate = true, updatedAt = 0, createdAt = 0),
        )
        database.blockDao().insert(
            BlockEntity(
                id = "t1-b0",
                sessionId = "t1",
                type = "STRAIGHT",
                orderIndex = 0,
                section = "MAIN",
                label = "Main",
                createdAt = 0,
                updatedAt = 0,
            ),
        )
        database.blockDao().insert(
            BlockEntity(
                id = "t1-b1",
                sessionId = "t1",
                type = "INTERVAL",
                orderIndex = 1,
                section = "CONDITIONING",
                conditioningFormat = "AMRAP",
                capSeconds = 420,
                label = "Cond",
                createdAt = 0,
                updatedAt = 0,
            ),
        )
        database.exerciseEntryDao().insert(
            ExerciseEntryEntity(
                id = "t1-e0",
                blockId = "t1-b0",
                exerciseId = "bench-press",
                orderIndex = 0,
                note = "focus on depth",
                eachSide = true,
                createdAt = 0,
                updatedAt = 0,
            ),
        )
        database.exerciseEntryDao().insert(
            ExerciseEntryEntity(id = "t1-e1", blockId = "t1-b1", exerciseId = "bench-press", orderIndex = 0, createdAt = 0, updatedAt = 0),
        )
        database.setEntryDao().insert(
            SetEntryEntity(id = "t1-s0", exerciseEntryId = "t1-e0", setNumber = 1, targetReps = 8, createdAt = 0, updatedAt = 0),
        )

        val session = repo.instantiateTemplate("t1")

        val newBlocks = database.blockDao().getBySession(session.id).sortedBy { it.orderIndex }
        assertEquals(2, newBlocks.size, "both blocks copied (not flattened)")
        assertEquals("MAIN", newBlocks[0].section)
        assertEquals("CONDITIONING", newBlocks[1].section)
        assertEquals("AMRAP", newBlocks[1].conditioningFormat, "conditioning shape preserved")
        assertEquals(420L, newBlocks[1].capSeconds)
        assertTrue(newBlocks.none { it.id.startsWith("t1-") }, "copied blocks get fresh ids")

        val newEntries = database.exerciseEntryDao().getBySession(session.id)
        assertEquals(2, newEntries.size)
        val mainEntry = newEntries.first { it.blockId == newBlocks[0].id }
        assertEquals("focus on depth", mainEntry.note, "coaching note copied")
        assertEquals(true, mainEntry.eachSide, "each-side flag copied")

        val copiedSet = database.setEntryDao().getForEntry(mainEntry.id).first()
        assertEquals(8, copiedSet.targetReps, "target copied")
        assertNull(copiedSet.reps, "actual stays null")
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
        val templateItemId = database.exerciseEntryDao().getBySession(template.id).first().id
        repo.addTargetSet(template.id, templateItemId, reps = 5, loadKg = 100.0)
        val session = repo.instantiateTemplate(template.id)

        val itemId = database.exerciseEntryDao().getBySession(session.id).first().id
        val ghost = database.setEntryDao().getForEntry(itemId).first()
        val updatedAtBefore = database.sessionDao().getById(session.id)!!.updatedAt

        // Fill in what varied: 5 reps at 102.5 kg.
        // updateSet takes a domain SetEntry now; map the entity ghost across the boundary.
        repo.updateSet(session.id, ghost.toDomain().copy(reps = 5, loadKg = 102.5))

        val saved = database.setEntryDao().getForEntry(itemId).first()
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
