package dev.cadence.data

import dev.cadence.data.local.AppDatabase
import dev.cadence.data.local.AthleteProfileEntity
import dev.cadence.domain.AthleteProfile
import dev.cadence.domain.AthleteProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/** Room-backed [AthleteProfileRepository]. Plain constructor injection, matching [PreferencesRepositoryImpl]. */
class AthleteProfileRepositoryImpl(
    private val database: AppDatabase,
) : AthleteProfileRepository {

    private val dao get() = database.athleteProfileDao()

    override fun observe(): Flow<AthleteProfile> =
        dao.observe().map { it?.toDomain() ?: AthleteProfile() }

    override suspend fun save(profile: AthleteProfile) = dao.upsert(profile.toEntity())

    override suspend fun setOnboardingComplete(complete: Boolean) {
        val current = dao.observe().first()?.toDomain() ?: AthleteProfile()
        dao.upsert(current.copy(onboardingComplete = complete).toEntity())
    }
}

private fun AthleteProfileEntity.toDomain() = AthleteProfile(
    fullName = fullName,
    bodyweightKg = bodyweightKg,
    heightCm = heightCm,
    defaultDivisionKey = defaultDivisionKey,
    defaultMode = defaultMode,
    onboardingComplete = onboardingComplete,
)

private fun AthleteProfile.toEntity() = AthleteProfileEntity(
    fullName = fullName,
    bodyweightKg = bodyweightKg,
    heightCm = heightCm,
    defaultDivisionKey = defaultDivisionKey,
    defaultMode = defaultMode,
    onboardingComplete = onboardingComplete,
)
