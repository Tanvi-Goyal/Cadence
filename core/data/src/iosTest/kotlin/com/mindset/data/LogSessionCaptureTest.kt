package com.mindset.data

import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.mindset.common.UuidV7Generator
import com.mindset.data.local.AppDatabase
import com.mindset.data.local.ExerciseAssetReader
import com.mindset.data.local.SessionType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import com.mindset.model.SessionType as ModelSessionType

/**
 * Covers the MindSet Log Session capture additions on [SessionRepositoryImpl]: history-based prefill
 * ([SessionRepository.addExercisePrefilled]), Hyrox-station capture with division standards
 * ([SessionRepository.addStation]), inline notes, and Complete (finish + derived type). iosTest for
 * the same reason as [SessionRepositoryTest] — Room's no-arg in-memory builder is Context-free on native.
 */
class LogSessionCaptureTest {

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

    @OptIn(kotlin.time.ExperimentalTime::class)
    private fun repo(reader: ExerciseAssetReader = benchPressAssetReader) =
        SessionRepositoryImpl(database, reader, UuidV7Generator(kotlin.time.Clock.System), kotlin.time.Clock.System)

    @Test
    fun addExercisePrefilled_recreatesLastSessionsSetsAsGhosts() = runTest {
        val repo = repo()
        // Prior session: log two actual sets of bench-press.
        val s1 = repo.createSession(SessionType.STRENGTH)
        repo.addExercise(s1.id, "bench-press")
        val e1 = database.exerciseEntryDao().getBySession(s1.id).first().id
        repo.addSet(s1.id, e1, reps = 8, loadKg = 100.0)
        repo.addSet(s1.id, e1, reps = 6, loadKg = 105.0)

        // New session: prefilled add recreates those two sets as ghosts (targets, actuals null).
        val s2 = repo.createSession(SessionType.STRENGTH)
        repo.addExercisePrefilled(s2.id, "bench-press")

        val e2 = database.exerciseEntryDao().getBySession(s2.id).first().id
        val sets = database.setEntryDao().getForEntry(e2)
        assertEquals(2, sets.size, "recreates the same number of sets")
        assertEquals(listOf(1, 2), sets.map { it.setNumber })
        assertEquals(8, sets[0].targetReps)
        assertEquals(100.0, sets[0].targetLoadKg)
        assertEquals(6, sets[1].targetReps)
        assertEquals(105.0, sets[1].targetLoadKg)
        assertNull(sets[0].reps, "ghost — no actual yet")
        assertNull(sets[0].loadKg)
    }

    @Test
    fun addExercisePrefilled_withNoHistory_seedsNoSets() = runTest {
        val repo = repo()
        val s = repo.createSession(SessionType.STRENGTH)
        repo.addExercisePrefilled(s.id, "bench-press")
        val e = database.exerciseEntryDao().getBySession(s.id).first().id
        assertTrue(database.setEntryDao().getForEntry(e).isEmpty(), "no history → no seeded sets")
    }

    @Test
    fun addStation_seedsDivisionStandardGhostAndTagsSegment() = runTest {
        val repo = repo(emptyExerciseAssetReader)
        val s = repo.createSession(SessionType.HYROX)
        val stations = repo.hyroxStations("MEN")
        assertEquals(8, stations.size, "the 8 Hyrox stations are seeded")
        val sledPush = stations.first { it.segmentKey?.contains("sled-push") == true }

        repo.addStation(s.id, "MEN", sledPush.segmentKey!!)

        val entry = database.exerciseEntryDao().getBySession(s.id).first()
        assertEquals(sledPush.segmentKey, entry.segmentKey, "entry tagged with its station segment")
        assertEquals(sledPush.exerciseId, entry.exerciseId)
        val set = database.setEntryDao().getForEntry(entry.id).first()
        assertEquals(sledPush.targetLoadKg, set.targetLoadKg, "division standard load prefilled as ghost")
        assertNull(set.loadKg, "no actual yet")
    }

    @Test
    fun stationStandardLabels_showBothTiersForGender() = runTest {
        val repo = repo(emptyExerciseAssetReader)
        val labels = repo.stationStandardLabels("MEN", "SINGLES")
        val sledPush = labels.entries.first { it.key.contains("sled-push") }.value
        // Men Open sled push = 152kg, Men Pro = 202kg → "202kg (Pro) / 152kg (Open)".
        assertTrue(sledPush.contains("Pro"), "shows Pro tier: $sledPush")
        assertTrue(sledPush.contains("Open"), "shows Open tier: $sledPush")
        assertTrue(sledPush.contains("152"), "shows the Men Open weight: $sledPush")
    }

    @Test
    fun updateSessionNotes_persists() = runTest {
        val repo = repo()
        val s = repo.createSession(SessionType.STRENGTH)
        repo.updateSessionNotes(s.id, "Focus on explosive push")
        assertEquals("Focus on explosive push", database.sessionDao().getById(s.id)!!.notes)
    }

    @Test
    fun finishSession_stampsFinishedAtAndPersistsDerivedType() = runTest {
        val repo = repo()
        val s = repo.createSession(SessionType.STRENGTH) // created as STRENGTH
        repo.finishSession(s.id, ModelSessionType.HYROX)

        val saved = database.sessionDao().getById(s.id)!!
        assertNotNull(saved.finishedAt, "finish stamps finishedAt")
        assertEquals(SessionType.HYROX, saved.type, "derived type persisted on finish")
    }

    @Test
    fun removeEntry_softDeletesEntryAndItsSets() = runTest {
        val repo = repo()
        val s = repo.createSession(SessionType.STRENGTH)
        repo.addExercise(s.id, "bench-press")
        val entryId = database.exerciseEntryDao().getBySession(s.id).first().id
        repo.addSet(s.id, entryId, reps = 8, loadKg = 100.0)

        repo.removeEntry(s.id, entryId)

        assertTrue(
            database.exerciseEntryDao().getBySession(s.id).isEmpty(),
            "entry is tombstoned (gone from live reads)",
        )
        val detail = repo.observeSessionDetail(s.id).first()
        assertTrue(detail?.blocks?.flatMap { it.entries }.isNullOrEmpty(), "session detail shows no entries")
    }

    @Test
    fun finishSession_withNullType_leavesTypeUntouched() = runTest {
        val repo = repo()
        val s = repo.createSession(SessionType.STRENGTH)
        repo.finishSession(s.id) // Hyrox-timer path: no derived type
        assertEquals(SessionType.STRENGTH, database.sessionDao().getById(s.id)!!.type)
    }
}
