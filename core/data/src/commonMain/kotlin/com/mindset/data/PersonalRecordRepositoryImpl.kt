package com.mindset.data

import com.mindset.data.local.AppDatabase
import com.mindset.domain.repository.PersonalRecordRepository
import com.mindset.model.PersonalRecord
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Room-backed [PersonalRecordRepository]; maps cached PB rows to domain records. */
class PersonalRecordRepositoryImpl(private val database: AppDatabase) : PersonalRecordRepository {
    override fun observeForExercise(exerciseId: String): Flow<List<PersonalRecord>> = database
        .personalRecordDao()
        .observeForExercise(exerciseId)
        .map { rows ->
            rows.map { it.toDomain() }
        }
}
