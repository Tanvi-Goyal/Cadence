package dev.cadence.data

import dev.cadence.data.local.AppDatabase
import dev.cadence.model.PersonalRecord
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Read access to cached personal records. Writes happen as a side effect of completing a set (see
 * `SessionRepositoryImpl`), so this port is read-only for now; a recompute-from-history path lands
 * with the PB screen.
 */
interface PersonalRecordRepository {
    /** All live records for an exercise, reactive — for the exercise-detail PB list. */
    fun observeForExercise(exerciseId: String): Flow<List<PersonalRecord>>
}

class PersonalRecordRepositoryImpl(
    private val database: AppDatabase,
) : PersonalRecordRepository {

    override fun observeForExercise(exerciseId: String): Flow<List<PersonalRecord>> =
        database.personalRecordDao().observeForExercise(exerciseId).map { rows -> rows.map { it.toDomain() } }
}
