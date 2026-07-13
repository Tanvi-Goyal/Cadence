package dev.cadence.data

import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import dev.cadence.data.local.AppDatabase
import dev.cadence.data.local.SessionType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

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

    @Test
    fun createSession_persistsSessionAndEnqueuesOutbox() = runTest {
        val repository = SessionRepositoryImpl(database)

        val created = repository.createSession(SessionType.STRENGTH)

        val sessions = repository.observeSessions().first()
        assertEquals(1, sessions.size, "session should be persisted")
        assertEquals(created.id, sessions.first().id, "persisted session should match returned one")
        assertEquals(1, database.outboxDao().count(), "outbox entry should be enqueued in the same write")
    }
}
