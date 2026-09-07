package com.mindset.domain.repository

import com.mindset.domain.AthleteProfile
import kotlinx.coroutines.flow.Flow

interface AthleteProfileRepository {
    fun observe(): Flow<AthleteProfile>

    suspend fun save(profile: AthleteProfile)
}
