package dev.cadence.data

import dev.cadence.data.local.AppDatabase
import dev.cadence.data.local.AthleteProfileEntity
import dev.cadence.domain.AthleteProfile
import dev.cadence.domain.repository.AthleteProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class AthleteProfileRepositoryImpl(
    private val database: AppDatabase,
) : AthleteProfileRepository {

    private val dao get() = database.athleteProfileDao()

    override fun observe(): Flow<AthleteProfile> =
        dao.observe().map { it?.toDomain() ?: AthleteProfile() }

    override suspend fun save(profile: AthleteProfile) = dao.upsert(profile.toEntity())
}

private fun AthleteProfileEntity.toDomain() = AthleteProfile(
    fullName = fullName,
    bodyweightKg = bodyweightKg,
    heightCm = heightCm,
)

private fun AthleteProfile.toEntity() = AthleteProfileEntity(
    fullName = fullName,
    bodyweightKg = bodyweightKg,
    heightCm = heightCm,
)
