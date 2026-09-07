package com.mindset.data.local

import com.mindset.model.BlockSection


/**
 * Accumulates one day's tree with deterministic ids (`hyfit-w{week}-{day}`…) and auto-incremented
 * order indices, so the day definitions above stay declarative.
 */
class Builder(
    day: String,
    week: Int,
    focus: String,
    goal: String,
    private val now: Long,
) {
    private val id = "hyfit-w$week-$day"
    private val sessionEntity = SessionEntity(
        id = id,
        startedAt = now,
        name = "W$week ${day.replaceFirstChar { it.uppercase() }} — $focus",
        type = SessionType.STRENGTH,
        notes = goal, // rendered as the Session Goal card on the detail
        isTemplate = true,
        source = SessionSource.MANUAL,
        category = "HyFit 6-Week Strength",
        focus = focus,
        programWeek = week,
        createdAt = now,
        updatedAt = now,
    )
    private val blockEntities = mutableListOf<BlockEntity>()
    private val entries = mutableListOf<ExerciseEntryEntity>()
    private val sets = mutableListOf<SetEntryEntity>()

    fun block(
        section: BlockSection,
        label: String,
        type: String = "STRAIGHT",
        capSeconds: Long? = null,
        rounds: Int? = null,
        workSeconds: Long? = null,
        restBetweenRoundsMs: Long? = null,
    ): String {
        val blockId = "$id-b${blockEntities.size}"
        blockEntities += BlockEntity(
            id = blockId,
            sessionId = id,
            type = type,
            orderIndex = blockEntities.size,
            rounds = rounds ?: 1,
            section = section.name,
            capSeconds = capSeconds,
            workSeconds = workSeconds,
            createdAt = now,
            updatedAt = now,
        )
        return blockId
    }

    fun entry(blockId: String, exerciseId: String, note: String? = null, eachSide: Boolean = false): String {
        val order = entries.count { it.blockId == blockId }
        val entryId = "$id-e${entries.size}"
        entries += ExerciseEntryEntity(
            id = entryId,
            blockId = blockId,
            exerciseId = exerciseId,
            orderIndex = order,
            eachSide = eachSide,
            createdAt = now,
            updatedAt = now,
        )
        return entryId
    }

    /** One target set per rep count (e.g. 8-6-4-4 → four sets). */
    fun reps(entryId: String, vararg reps: Int) {
        reps.forEachIndexed { i, r ->
            sets +=
                SetEntryEntity(
                    id = "$entryId-s${i + 1}",
                    exerciseEntryId = entryId,
                    setNumber = i + 1,
                    targetReps = r,
                    createdAt = now,
                    updatedAt = now,
                )
        }
    }

    /** Single target set of [count] reps. */
    fun rep(entryId: String, count: Int) = reps(entryId, count)

    /** [count] timed target sets (holds). */
    fun timed(entryId: String, seconds: Int, count: Int = 1) {
        repeat(count) { i ->
            sets +=
                SetEntryEntity(
                    id = "$entryId-s${i + 1}",
                    exerciseEntryId = entryId,
                    setNumber = i + 1,
                    targetTimeSec = seconds,
                    createdAt = now,
                    updatedAt = now,
                )
        }
    }

    fun build(): SeededTemplate {
        val countByEntry = sets.groupingBy { it.exerciseEntryId }.eachCount()
        val withCounts = entries.map { it.copy(targetSets = countByEntry[it.id] ?: 0) }
        return SeededTemplate(sessionEntity, blockEntities, withCounts, sets)
    }
}
