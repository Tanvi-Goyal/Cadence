package dev.cadence.data.local

import androidx.room3.ConstructedBy
import androidx.room3.Database
import androidx.room3.RoomDatabase
import androidx.room3.RoomDatabaseConstructor

/**
 * The Room database — the single source of truth the whole app reads through.
 *
 * [AppDatabaseConstructor] is an `expect` object because Room cannot use reflection to
 * instantiate the generated implementation on Kotlin/Native (iOS). KSP fills in the `actual`
 * per target, so each platform gets a concrete constructor without any reflective lookup.
 */
@Database(
    entities = [
        Session::class, OutboxEntry::class, SyncMeta::class, PlannedSession::class,
        Exercise::class, ExerciseEntry::class, SetEntry::class, PreferencesEntity::class,
        Block::class, PersonalRecord::class,
        // v10: HYROX reference tables (format-as-data) + `sessions.finishedAt`.
        HyroxStationRef::class, HyroxDivisionRef::class, HyroxStationLoadRef::class,
        // v11: athlete profile + race config (Onboarding) and the subscription-entitlement stub.
        AthleteProfileEntity::class, EntitlementEntity::class,
    ],
    version = 11,
    exportSchema = true,
)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun sessionDao(): SessionDao
    abstract fun outboxDao(): OutboxDao
    abstract fun syncMetaDao(): SyncMetaDao
    abstract fun plannedSessionDao(): PlannedSessionDao
    abstract fun exerciseDao(): ExerciseDao
    abstract fun exerciseEntryDao(): ExerciseEntryDao
    abstract fun setEntryDao(): SetEntryDao
    abstract fun statsDao(): StatsDao
    abstract fun preferencesDao(): PreferencesDao
    abstract fun blockDao(): BlockDao
    abstract fun personalRecordDao(): PersonalRecordDao
    abstract fun hyroxRefDao(): HyroxRefDao
    abstract fun athleteProfileDao(): AthleteProfileDao
    abstract fun entitlementDao(): EntitlementDao
}

// KSP generates the actual implementation per target.
@Suppress("KotlinNoActualForExpect")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase
}
