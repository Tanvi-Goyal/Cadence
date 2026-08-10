package com.mindset.data

import com.mindset.data.local.BlockEntity
import com.mindset.data.local.ExerciseEntryEntity
import com.mindset.data.local.SessionEntity
import com.mindset.data.local.SetEntryEntity
import com.mindset.model.BlockDetail
import com.mindset.model.Exercise
import com.mindset.model.ExerciseEntryDetail
import com.mindset.model.SessionDetail
import kotlin.collections.orEmpty

/**
 * Assemble the hydrated [SessionDetail] the UI renders, from the real block graph:
 * blocks → their exercise entries (each with resolved catalog [Exercise] + derived capture fields)
 * → their sets. [exercises] must resolve every `exerciseId` the session references.
 */
internal fun buildSessionDetail(
    session: SessionEntity,
    blocks: List<BlockEntity>,
    entries: List<ExerciseEntryEntity>,
    sets: List<SetEntryEntity>,
    exercises: Map<String, Exercise>,
): SessionDetail {
    val setsByEntry = sets.groupBy { it.exerciseEntryId }
    val entriesByBlock = entries.groupBy { it.blockId }
    val blockDetails =
        blocks.sortedBy { it.orderIndex }.map { block ->
            val entryDetails =
                entriesByBlock[block.id].orEmpty().sortedBy { it.orderIndex }.map { entry ->
                    ExerciseEntryDetail(
                        entry = entry.toDomain(),
                        exercise =
                        requireNotNull(exercises[entry.exerciseId]) {
                            "Exercise ${entry.exerciseId} not found for entry ${entry.id}"
                        },
                        sets =
                        setsByEntry[entry.id]
                            .orEmpty()
                            .sortedBy { it.setNumber }
                            .map { it.toDomain() },
                    )
                }
            BlockDetail(block.toDomain(), entryDetails)
        }
    return SessionDetail(session = session.toDomain(), blocks = blockDetails)
}
