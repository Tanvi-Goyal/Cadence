package dev.cadence.domain.repository

import dev.cadence.domain.AthleteProfile
import kotlinx.coroutines.flow.Flow

interface AthleteProfileRepository {
    fun observe(): Flow<AthleteProfile>

    suspend fun save(profile: AthleteProfile)
}
