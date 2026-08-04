package dev.cadence.data.local

import androidx.room3.Dao
import androidx.room3.Entity
import androidx.room3.PrimaryKey
import androidx.room3.Query
import androidx.room3.Upsert
import kotlinx.coroutines.flow.Flow

/**
 * Single-row table holding the athlete's identity + physical baseline, captured in Onboarding.
 * Device-local — like [PreferencesEntity] it carries no sync fields and never enqueues an outbox row.
 * The **target race moved out** in v12 to the first-class, syncable `race_goal` table; this row keeps
 * only the defaults that pre-fill new goals/sims. `defaultDivisionKey` is an `event_division.key`
 * (e.g. "MEN"); `defaultMode` is a [dev.cadence.model.RaceMode] name (e.g. "SINGLES"); `onboardingComplete`
 * gates the app's start destination.
 */
@Entity(tableName = "athlete_profile")
data class AthleteProfileEntity(
    @PrimaryKey val id: Int = SINGLETON_ID,
    val fullName: String,
    val bodyweightKg: Double?,
    val heightCm: Double?,
    val defaultDivisionKey: String,
    val defaultMode: String?,
    val onboardingComplete: Boolean,
) {
    companion object {
        /** There is only ever one athlete-profile row. */
        const val SINGLETON_ID = 0
    }
}

@Dao
interface AthleteProfileDao {
    @Query("SELECT * FROM athlete_profile WHERE id = 0")
    fun observe(): Flow<AthleteProfileEntity?>

    @Upsert
    suspend fun upsert(profile: AthleteProfileEntity)
}
