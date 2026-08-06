package dev.cadence.data

import dev.cadence.data.local.AppDatabase
import dev.cadence.domain.repository.PersonalRecordRepository
import dev.cadence.model.PersonalRecord
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Room-backed [PersonalRecordRepository]; maps cached PB rows to domain records. */
class PersonalRecordRepositoryImpl(
    private val database: AppDatabase,
) : PersonalRecordRepository {

    override fun observeForExercise(exerciseId: String): Flow<List<PersonalRecord>> =
        database.personalRecordDao()
            .observeForExercise(exerciseId)
            .map { rows ->
                rows.map { it.toDomain() }
            }
}
