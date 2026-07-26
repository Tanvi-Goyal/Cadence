package dev.cadence.presentation

/*
 * Illustrative sample content for the three Strength sessions (Push / Pull / Lower Body). Like
 * HyroxStandards, this is design-accurate placeholder data so the layout is exact and reviewable —
 * real per-template content is DB-observed in a later pass that touches :shared. Numbers (loads, RPE,
 * tempo) are plausible but not prescriptive; PREV is a muted placeholder since a template has no history.
 */
object StrengthSessions {

    fun buildSession(session: StrengthSession): TemplateStrengthDetailUiState = when (session) {
        StrengthSession.PUSH -> push()
        StrengthSession.PULL -> pull()
        StrengthSession.LOWER -> lower()
    }

    // ── Push (Chest · Shoulders · Triceps) ──────────────────────────────────────────────────────
    private fun push() = TemplateStrengthDetailUiState(
        session = StrengthSession.PUSH,
        title = "Upper Strength A",
        subtitle = "Push Focus — Horizontal & Vertical Press",
        tags = tags("Push"),
        duration = "45 min",
        calories = "~380 kcal",
        focusAreas = listOf("Chest", "Shoulders", "Triceps"),
        sessionGoal = "Build pressing strength through heavy compound work, then chase a controlled " +
            "hypertrophy pump on the isolation finishers.",
        warmUp = ProtocolItem(BlockGlyph.WARM_UP, "Warm-up", "Band pull-aparts & light presses", "8:00"),
        groups = listOf(
            superset(
                exercise("A1", StrengthGlyph.BARBELL, "Barbell Bench Press", "4 Sets • 6-8 Reps • 150s Rest",
                    target = "80 kg", prev = "77.5 kg", rpe = "8", tempo = "3-1-1"),
                exercise("A2", StrengthGlyph.DUMBBELL, "Seated DB Shoulder Press", "4 Sets • 8-10 Reps • 120s Rest",
                    target = "26 kg", prev = "24 kg", rpe = "8.5", tempo = "2-0-2"),
            ),
            superset(
                exercise("B1", StrengthGlyph.DUMBBELL, "Incline DB Press", "3 Sets • 10-12 Reps • 90s Rest",
                    target = "28 kg", prev = "26 kg", rpe = "9", tempo = "2-1-2"),
                exercise("B2", StrengthGlyph.MACHINE, "Cable Lateral Raise", "3 Sets • 12-15 Reps • 60s Rest",
                    target = "10 kg", prev = "9 kg", rpe = "9", tempo = "2-0-1"),
            ),
            standalone(
                exercise("C1", StrengthGlyph.MACHINE, "Triceps Rope Pushdown", "3 Sets • 12-15 Reps • 60s Rest",
                    target = "25 kg", prev = "22.5 kg", rpe = "9.5", tempo = "2-0-1"),
            ),
        ),
        coolDownTitle = "Cool-down",
        coolDownDuration = "6:00",
        coolDownItems = listOf(
            ProtocolItem(BlockGlyph.COOL_DOWN, "Doorway Pec Stretch", "2 × 30s per side", "2:00"),
            ProtocolItem(BlockGlyph.COOL_DOWN, "Overhead Triceps Stretch", "2 × 30s per side", "2:00"),
            ProtocolItem(BlockGlyph.COOL_DOWN, "Cross-Body Shoulder Stretch", "2 × 30s per side", "2:00"),
        ),
        coolDownExpanded = false,
    )

    // ── Pull (Back · Biceps · Rear Delts) ───────────────────────────────────────────────────────
    private fun pull() = TemplateStrengthDetailUiState(
        session = StrengthSession.PULL,
        title = "Upper Strength B",
        subtitle = "Pull Focus — Vertical & Horizontal Pull",
        tags = tags("Pull"),
        duration = "45 min",
        calories = "~360 kcal",
        focusAreas = listOf("Back", "Lats", "Biceps", "Rear Delts"),
        sessionGoal = "Own the vertical and horizontal pull. Drive scapular control on the compounds " +
            "before isolating the arms and rear delts.",
        warmUp = ProtocolItem(BlockGlyph.WARM_UP, "Warm-up", "Scapular pulls & band rows", "8:00"),
        groups = listOf(
            superset(
                exercise("A1", StrengthGlyph.BARBELL, "Weighted Pull-up", "4 Sets • 6-8 Reps • 150s Rest",
                    target = "+10 kg", prev = "+7.5 kg", rpe = "8.5", tempo = "2-1-2"),
                exercise("A2", StrengthGlyph.DUMBBELL, "Chest-Supported Row", "4 Sets • 8-10 Reps • 120s Rest",
                    target = "30 kg", prev = "28 kg", rpe = "8", tempo = "2-1-2"),
            ),
            superset(
                exercise("B1", StrengthGlyph.MACHINE, "Lat Pulldown", "3 Sets • 10-12 Reps • 90s Rest",
                    target = "60 kg", prev = "55 kg", rpe = "9", tempo = "2-0-2"),
                exercise("B2", StrengthGlyph.MACHINE, "Face Pull", "3 Sets • 15-20 Reps • 60s Rest",
                    target = "20 kg", prev = "17.5 kg", rpe = "9", tempo = "2-1-1"),
            ),
            standalone(
                exercise("C1", StrengthGlyph.DUMBBELL, "EZ-Bar Curl", "3 Sets • 10-12 Reps • 60s Rest",
                    target = "30 kg", prev = "27.5 kg", rpe = "9.5", tempo = "2-0-1"),
            ),
        ),
        coolDownTitle = "Cool-down",
        coolDownDuration = "6:00",
        coolDownItems = listOf(
            ProtocolItem(BlockGlyph.COOL_DOWN, "Lat Hang Stretch", "2 × 30s", "2:00"),
            ProtocolItem(BlockGlyph.COOL_DOWN, "Child's Pose Reach", "2 × 45s", "2:00"),
            ProtocolItem(BlockGlyph.COOL_DOWN, "Biceps Wall Stretch", "2 × 30s per side", "2:00"),
        ),
        coolDownExpanded = false,
    )

    // ── Lower Body (Quads · Glutes · Hamstrings) ────────────────────────────────────────────────
    private fun lower() = TemplateStrengthDetailUiState(
        session = StrengthSession.LOWER,
        title = "Lower Body",
        subtitle = "Squat & Hinge — Quads, Glutes, Hamstrings",
        tags = tags("Lower Body"),
        duration = "50 min",
        calories = "~450 kcal",
        focusAreas = listOf("Quads", "Glutes", "Hamstrings", "Calves"),
        sessionGoal = "Load the squat and hinge patterns hard, then build unilateral stability and " +
            "hamstring resilience for race-day durability.",
        warmUp = ProtocolItem(BlockGlyph.WARM_UP, "Warm-up", "Hip openers & goblet squats", "10:00"),
        groups = listOf(
            superset(
                exercise("A1", StrengthGlyph.BARBELL, "Barbell Back Squat", "4 Sets • 5-8 Reps • 180s Rest",
                    target = "110 kg", prev = "105 kg", rpe = "8.5", tempo = "3-1-1"),
                exercise("A2", StrengthGlyph.BARBELL, "Romanian Deadlift", "4 Sets • 8-10 Reps • 120s Rest",
                    target = "90 kg", prev = "85 kg", rpe = "8", tempo = "3-1-1"),
            ),
            superset(
                exercise("B1", StrengthGlyph.MACHINE, "Leg Press", "3 Sets • 12-15 Reps • 90s Rest",
                    target = "220 kg", prev = "200 kg", rpe = "9", tempo = "2-0-2"),
                exercise("B2", StrengthGlyph.LOWER_BODY, "Walking Lunge", "3 Sets • 20 Steps • 90s Rest",
                    target = "24 kg DB", prev = "20 kg DB", rpe = "8.5", tempo = "EXPL"),
            ),
            standalone(
                exercise("C1", StrengthGlyph.MACHINE, "Seated Leg Curl", "3 Sets • 12-15 Reps • 60s Rest",
                    target = "45 kg", prev = "40 kg", rpe = "9.5", tempo = "2-1-1"),
            ),
        ),
        coolDownTitle = "Cool-down",
        coolDownDuration = "7:00",
        coolDownItems = listOf(
            ProtocolItem(BlockGlyph.COOL_DOWN, "Couch Stretch", "2 × 45s per side", "3:00"),
            ProtocolItem(BlockGlyph.COOL_DOWN, "Seated Hamstring Fold", "2 × 45s", "2:00"),
            ProtocolItem(BlockGlyph.COOL_DOWN, "Figure-4 Glute Stretch", "2 × 30s per side", "2:00"),
        ),
        coolDownExpanded = false,
    )

    // ── Builders ────────────────────────────────────────────────────────────────────────────────

    /** STRENGTH (modality) + the session focus, both on the accent tone. */
    private fun tags(focus: String) = listOf(DetailTag("Strength"), DetailTag(focus))

    private fun exercise(
        tag: String,
        glyph: StrengthGlyph,
        name: String,
        scheme: String,
        target: String,
        prev: String,
        rpe: String,
        tempo: String,
    ) = StrengthExercise(
        tag = tag,
        glyph = glyph,
        name = name,
        scheme = scheme,
        stats = listOf(
            StrengthStat("Target", target),
            StrengthStat("Prev", prev, muted = true),
            StrengthStat("RPE", rpe),
            StrengthStat("Tempo", tempo),
        ),
    )

    private fun superset(vararg exercises: StrengthExercise) = ExerciseGroup(exercises.toList())

    private fun standalone(exercise: StrengthExercise) = ExerciseGroup(listOf(exercise))
}
