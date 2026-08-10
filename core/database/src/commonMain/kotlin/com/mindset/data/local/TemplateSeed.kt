package com.mindset.data.local

import com.mindset.model.BlockSection

/**
 * Program templates seeded into the DB on first launch (gated by
 * [SyncMetaKeys.TEMPLATE_SEED_VERSION]). A seeded template is an ordinary [SessionEntity] with
 * `isTemplate = true` and a full multi-block tree of **target-only** sets; starting it deep-copies
 * the tree into a live session (`SessionRepositoryImpl.instantiateTemplate`).
 *
 * Authored from the HyFit **Weeks 4–6** block. Within the block the 5-day split is constant and only
 * the rep schemes change week to week, so each weekday is defined once and parameterised by [week];
 * the per-week schemes sit together in a `wk(...)` call so they're easy to eyeball against the source.
 * Exercise ids reference the seeded catalog ([ExerciseImporter]) — dataset slugs (`Pushups`, …) or
 * HyFit custom slugs (`db-stiff-legged-deadlift`, …). All ids here are deterministic → re-seed replaces.
 *
 * NOTE: authored by transcription from spreadsheet images — the schemes are pending owner verification.
 */
class SeededTemplate(
    val sessionEntity: SessionEntity,
    val blockEntities: List<BlockEntity>,
    val entries: List<ExerciseEntryEntity>,
    val sets: List<SetEntryEntity>,
)

object TemplateSeed {

    /** All 15 templates (Weeks 4–6 × Mon–Fri). Bump [SyncMetaKeys.TEMPLATE_SEED_VERSION] when it changes. */
    fun all(now: Long): List<SeededTemplate> = buildList {
        for (week in 4..6) {
            add(monday(week, now))
            add(tuesday(week, now))
            add(wednesday(week, now))
            add(thursday(week, now))
            add(friday(week, now))
        }
    }

    /** Pick a per-week rep scheme (`4/5/6`). */
    private fun wk(week: Int, w4: IntArray, w5: IntArray, w6: IntArray): IntArray = when (week) {
        4 -> w4
        5 -> w5
        else -> w6
    }

    // ── Monday — Glutes & Hamstring ────────────────────────────────────────────────────────────
    private fun monday(week: Int, now: Long): SeededTemplate {
        val b =
            Builder(
                "mon",
                week,
                "Glutes & Hamstring",
                "Build glute & hamstring strength — explosive jumps into heavy hinging and single-leg work.",
                now,
            )

        b.block(BlockSection.WARMUP, "Warm-up").let { w ->
            b.rep(b.entry(w, "hamstring-scoops", eachSide = true), 10)
            b.rep(b.entry(w, "Worlds_Greatest_Stretch", eachSide = true), 5)
            b.rep(b.entry(w, "90_90_Hamstring", eachSide = true), 6)
            b.rep(b.entry(w, "glute-bridge-bw"), 10)
        }
        b.block(BlockSection.MAIN, "Main Strength").let { m ->
            b.reps(
                b.entry(m, "vertical-jump"),
                *wk(week, intArrayOf(5, 5, 3), intArrayOf(5, 4, 3), intArrayOf(4, 4, 3)),
            )
            b.reps(
                b.entry(m, "single-leg-box-jump", eachSide = true),
                *wk(week, intArrayOf(5, 4, 2), intArrayOf(4, 4, 2, 1), intArrayOf(4, 3, 2, 1)),
            )
            b.reps(
                b.entry(m, "Barbell_Glute_Bridge"),
                *wk(week, intArrayOf(10, 8, 8), intArrayOf(8, 8, 6), intArrayOf(8, 6, 6)),
            )
            b.reps(
                b.entry(m, "db-stiff-legged-deadlift"),
                *wk(week, intArrayOf(10, 8, 8), intArrayOf(10, 8, 6), intArrayOf(8, 8, 6, 4)),
            )
            b.reps(
                b.entry(m, "db-glutes-bulgarian-split-squat"),
                *wk(week, intArrayOf(8, 8, 6), intArrayOf(8, 6, 6), intArrayOf(6, 6, 4)),
            )
        }
        b.block(
            BlockSection.ACCESSORY,
            "Accessory — 3 rounds, 8 min",
            type = "CIRCUIT",
            rounds = 3,
            capSeconds = 480,
        ).let { a ->
            b.rep(
                b.entry(a, "curtsy-lunge-pulses", eachSide = true),
                wk(week, intArrayOf(12), intArrayOf(12), intArrayOf(15))[0],
            )
            b.rep(b.entry(a, "kb-swing"), wk(week, intArrayOf(12), intArrayOf(10), intArrayOf(8))[0])
            b.rep(
                b.entry(a, "plate-zercher-squat-march", eachSide = true),
                wk(week, intArrayOf(12), intArrayOf(12), intArrayOf(15))[0],
            )
        }
        return b.build()
    }

    // ── Tuesday — Chest & Accessory ────────────────────────────────────────────────────────────
    private fun tuesday(week: Int, now: Long): SeededTemplate {
        val b =
            Builder(
                "tue",
                week,
                "Chest & Accessory",
                "Press with control at 50-55% of PR — chase depth, then a chest pump on the accessories.",
                now,
            )

        b.block(BlockSection.WARMUP, "Warm-up").let { w ->
            b.rep(b.entry(w, "cat-cow-thoracic-opener", eachSide = true), 6)
            b.rep(b.entry(w, "scap-push-up"), 12)
            b.rep(b.entry(w, "shoulder-rotation"), 10)
            b.rep(b.entry(w, "Pushups"), 8)
        }
        b.block(BlockSection.MAIN, "Main Strength").let { m ->
            b.reps(
                b.entry(
                    m,
                    "Barbell_Incline_Bench_Press_-_Medium_Grip",
                    note = "50-55% of PR — keep the weight, focus on depth",
                ),
                *wk(week, intArrayOf(8, 6, 4, 4), intArrayOf(8, 6, 4, 3), intArrayOf(6, 4, 2, 1)),
            )
            b.reps(
                b.entry(m, "db-floor-chest-fly"),
                *wk(week, intArrayOf(10, 10, 8), intArrayOf(10, 8, 8), intArrayOf(12, 10, 8)),
            )
            b.reps(
                b.entry(m, "staggered-stance-push-up", eachSide = true),
                *wk(week, intArrayOf(10, 8, 8), intArrayOf(10, 8, 6), intArrayOf(8, 8, 6)),
            )
        }
        b.block(BlockSection.ACCESSORY, "Accessory").let { a ->
            b.reps(
                b.entry(a, "Bent-Arm_Dumbbell_Pullover"),
                *wk(week, intArrayOf(10, 8, 8), intArrayOf(8, 8, 8), intArrayOf(8, 6, 6)),
            )
            b.reps(
                b.entry(a, "single-arm-db-floor-chest-press", eachSide = true),
                *wk(week, intArrayOf(12, 10, 6), intArrayOf(12, 10, 6), intArrayOf(10, 8, 6)),
            )
        }
        b.block(
            BlockSection.CONDITIONING,
            "Conditioning",
            type = "INTERVAL",
            capSeconds = 420,
        ).let { c ->
            // Week 4 uses crunches; weeks 5–6 swap to V-ups.
            val core = if (week == 4) "Crunches" else "v-ups"
            b.rep(b.entry(c, core), 18)
            b.rep(b.entry(c, "Russian_Twist"), 12)
            b.rep(b.entry(c, "Flutter_Kicks", eachSide = true), 15)
        }
        b.block(BlockSection.CORE, "Core").let { co ->
            b.timed(b.entry(co, "hollow-hold"), 60)
        }
        return b.build()
    }

    // ── Wednesday — Arms ───────────────────────────────────────────────────────────────────────
    private fun wednesday(week: Int, now: Long): SeededTemplate {
        val b =
            Builder("wed", week, "Arms", "Bias the arms — strict curls and pushdowns, then a hard EMOM finisher.", now)

        b.block(BlockSection.WARMUP, "Warm-up").let { w ->
            b.rep(b.entry(w, "dog-and-bone"), 8) // no rep count in source — nominal
            b.rep(b.entry(w, "cat-cow-thoracic-opener", eachSide = true), 6)
            b.rep(b.entry(w, "shoulder-rotation"), 10)
            b.rep(b.entry(w, "hindu-push-up"), 8)
        }
        b.block(BlockSection.MAIN, "Main Strength").let { m ->
            b.reps(
                b.entry(m, "Barbell_Curl"),
                *wk(week, intArrayOf(10, 8, 8), intArrayOf(10, 8, 8), intArrayOf(10, 10, 8)),
            )
            b.reps(
                b.entry(m, "banded-triceps-pushdown"),
                *wk(week, intArrayOf(12, 10, 8), intArrayOf(12, 10, 8), intArrayOf(14, 10, 8)),
            )
            b.reps(b.entry(m, "db-21-curl", note = "7 + 7 + 7 (bottom / top / full)"), 21, 21)
        }
        b.block(BlockSection.ACCESSORY, "Accessory").let { a ->
            b.reps(
                b.entry(a, "EZ-Bar_Skullcrusher"),
                *wk(week, intArrayOf(10, 8, 6), intArrayOf(10, 8, 6), intArrayOf(12, 10, 8)),
            )
        }
        b.block(
            BlockSection.CONDITIONING,
            "Conditioning",
            type = "INTERVAL",
            capSeconds = 540,
        ).let { c ->
            b.rep(b.entry(c, "db-man-makers"), 7)
            b.rep(b.entry(c, "bicycle-crunch", eachSide = true), 20)
            b.rep(b.entry(c, "Alternating_Renegade_Row", eachSide = true), 15)
        }
        b.block(
            BlockSection.CORE,
            "Core",
            type = "INTERVAL",
            rounds = 8,
            workSeconds = 20,
            restBetweenRoundsMs = 10_000,
        ).let { co ->
            b.entry(co, "high-knees")
            b.entry(co, "jumping-jacks")
        }
        return b.build()
    }

    // ── Thursday — Legs ────────────────────────────────────────────────────────────────────────
    private fun thursday(week: Int, now: Long): SeededTemplate {
        val b =
            Builder(
                "thu",
                week,
                "Legs",
                "Load the squat pattern, then build unilateral stability and grip for race durability.",
                now,
            )

        b.block(BlockSection.WARMUP, "Warm-up").let { w ->
            b.rep(b.entry(w, "Worlds_Greatest_Stretch", eachSide = true), 5)
            b.rep(b.entry(w, "adductor-opener", eachSide = true), 5)
            b.rep(b.entry(w, "90_90_Hamstring", eachSide = true), 6)
            b.rep(b.entry(w, "glute-bridge-bw"), 10)
        }
        b.block(BlockSection.MAIN, "Main Strength").let { m ->
            b.reps(
                b.entry(m, "broad-jump"),
                *wk(week, intArrayOf(5, 4, 3), intArrayOf(5, 4, 3), intArrayOf(5, 4, 3, 1)),
            )
            b.reps(
                b.entry(m, "Zercher_Squats"),
                *wk(week, intArrayOf(10, 8, 8), intArrayOf(8, 8, 6), intArrayOf(8, 6, 4, 1)),
            )
            b.reps(
                b.entry(m, "kb-half-staggered-squat", eachSide = true),
                *wk(week, intArrayOf(10, 10, 8), intArrayOf(10, 8, 8), intArrayOf(10, 8, 6)),
            )
        }
        b.block(BlockSection.ACCESSORY, "Accessory").let { a ->
            b.reps(
                b.entry(a, "pistol-box-squat", eachSide = true),
                *wk(week, intArrayOf(8, 6, 4), intArrayOf(8, 6, 4), intArrayOf(6, 4, 3, 2)),
            )
            b.timed(b.entry(a, "wall-squat-hold"), 40, count = 3)
        }
        b.block(BlockSection.CONDITIONING, "Conditioning — grip").let { c ->
            b.timed(b.entry(c, "plate-hold", eachSide = true), 35, count = 3)
        }
        b.block(BlockSection.CORE, "Core").let { co ->
            b.reps(b.entry(co, "feet-elevated-calf-raise"), 20, 20, 20)
        }
        return b.build()
    }

    // ── Friday — Back & Shoulder ───────────────────────────────────────────────────────────────
    private fun friday(week: Int, now: Long): SeededTemplate {
        val b =
            Builder(
                "fri",
                week,
                "Back & Shoulder",
                "Own the pull — rows and presses through full range, capped by a TABATA burner.",
                now,
            )

        b.block(BlockSection.WARMUP, "Warm-up").let { w ->
            b.rep(b.entry(w, "cat-cow-thoracic-opener", eachSide = true), 6)
            b.rep(b.entry(w, "Worlds_Greatest_Stretch", eachSide = true), 5)
            b.rep(b.entry(w, "reverse-snow-angel"), 10)
            b.rep(b.entry(w, "scap-push-up"), 12)
        }
        b.block(BlockSection.MAIN, "Main Strength").let { m ->
            b.reps(
                b.entry(m, "landmine-single-arm-row", eachSide = true),
                *wk(week, intArrayOf(12, 10, 8), intArrayOf(10, 8, 6), intArrayOf(8, 6, 4, 2)),
            )
            b.reps(
                b.entry(m, "half-kneeling-sa-bb-shoulder-press", eachSide = true),
                *wk(week, intArrayOf(10, 8, 6), intArrayOf(10, 8, 6), intArrayOf(8, 6, 4, 2)),
            )
            b.reps(
                b.entry(m, "half-kneeling-band-pull", eachSide = true),
                *wk(week, intArrayOf(14, 12, 10), intArrayOf(12, 10, 8), intArrayOf(12, 10, 8)),
            )
            b.reps(b.entry(m, "db-rear-delt-fly"), 12, 10, 8)
        }
        b.block(BlockSection.ACCESSORY, "Accessory — 3 rounds", type = "CIRCUIT", rounds = 3).let { a ->
            val pullMax = if (week == 6) "6-8 max" else "5-6 max"
            b.rep(b.entry(a, "Pullups", note = pullMax), wk(week, intArrayOf(6), intArrayOf(6), intArrayOf(8))[0])
            b.rep(b.entry(a, "kb-upright-row"), wk(week, intArrayOf(14), intArrayOf(14), intArrayOf(12))[0])
            b.rep(b.entry(a, "db-shrug"), 14)
        }
        b.block(
            BlockSection.CONDITIONING,
            "Conditioning",
            type = "INTERVAL",
            rounds = 8,
            workSeconds = 20,
            restBetweenRoundsMs = 10_000,
        ).let { c ->
            b.entry(c, "devil-press")
            b.entry(c, "mma-plank")
        }
        b.block(BlockSection.CORE, "Core").let { co ->
            b.timed(b.entry(co, "Superman"), 30, count = 3)
        }
        return b.build()
    }

    /**
     * Accumulates one day's tree with deterministic ids (`hyfit-w{week}-{day}`…) and auto-incremented
     * order indices, so the day definitions above stay declarative.
     */
    private class Builder(day: String, week: Int, focus: String, goal: String, private val now: Long) {
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
}
