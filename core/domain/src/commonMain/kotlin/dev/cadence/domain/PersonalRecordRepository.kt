package dev.cadence.domain

import dev.cadence.model.PersonalRecord
import kotlinx.coroutines.flow.Flow

/**
 * Read access to cached personal records. Writes happen as a side effect of completing a set (in the
 * data layer), so this port is read-only for now; a recompute-from-history path lands with the PB screen.
 */
interface PersonalRecordRepository {
    /** All live records for an exercise, reactive — for the exercise-detail PB list. */
    fun observeForExercise(exerciseId: String): Flow<List<PersonalRecord>>
}
