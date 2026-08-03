package dev.cadence.data.local

import androidx.room3.Dao
import androidx.room3.Entity
import androidx.room3.PrimaryKey
import androidx.room3.Query
import androidx.room3.Upsert
import kotlinx.coroutines.flow.Flow

/**
 * Single-row table holding the athlete's profile + target race, captured in Onboarding. Device-local —
 * like [PreferencesEntity] it carries no sync fields and never enqueues an outbox row. `defaultDivision`
 * / `raceFormat` are stored as plain TEXT (a `hyrox_divisions.key` such as "MEN", and a race format such
 * as "SINGLES"); `onboardingComplete` gates the app's start destination.
 */
@Entity(tableName = "athlete_profile")
data class AthleteProfileEntity(
    @PrimaryKey val id: Int = SINGLETON_ID,
    val fullName: String,
    val bodyweightKg: Double?,
    val heightCm: Double?,
    val defaultDivision: String,
    val raceDate: Long?,
    val raceFormat: String?,
    val raceCity: String?,
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
