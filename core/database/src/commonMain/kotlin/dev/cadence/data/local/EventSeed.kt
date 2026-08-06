package dev.cadence.data.local

import dev.cadence.model.EventFormat
import dev.cadence.model.Gender
import dev.cadence.model.MetricType
import dev.cadence.model.RaceMode
import dev.cadence.model.SegmentKind
import dev.cadence.model.Tier

/**
 * Seed rows for the event-format reference tables (iteration 3). Hyrox is expressed as generic
 * `event_*` data — the same figures that previously lived in `HyroxSeed`/`HyroxStandards`, now
 * format-agnostic so DEKA / CrossFit arrive later as additional seed rows with no schema change.
 *
 * Values verified against hyrox.com → "Weights, Distances & Repetitions". Distances and reps are
 * CONSTANT across divisions; only weights change. Bump [VERSION] when these change (gated in
 * `SessionRepositoryImpl.ensureSeeded` via `SyncMetaKeys.EVENT_SEED_VERSION`).
 *
 * Segment order: a 1 km RUN precedes each of the 8 stations → 16 ordered segments.
 */
object EventSeed {

    /** Bump to re-seed the event reference tables on existing installs. */
    const val VERSION = 1

    private object Load {
        const val SLED_PUSH = "SLED_PUSH"
        const val SLED_PULL = "SLED_PULL"
        const val FARMERS = "FARMERS"
        const val SANDBAG = "SANDBAG"
        const val WALL_BALL = "WALL_BALL"
    }

    private val DT = MetricType.DISTANCE_TIME.name
    private val REPS = MetricType.REPS_ONLY.name
    private const val RUN_EXERCISE = "hyrox-run"

    val formats: List<EventFormatEntity> = listOf(
        EventFormatEntity(
            formatKey = EventFormat.HYROX,
            name = "HYROX",
            description = "8 × 1 km runs interleaved with 8 functional stations.",
        ),
    )

    /** The 8 stations in race order; runs are interleaved before each when segments are built. */
    private data class StationSpec(
        val slug: String,
        val exerciseId: String,
        val name: String,
        val label: String,
        val metric: String,
        val distanceM: Int?,
        val reps: Int?,
        val descriptor: String,
        val loadType: String?,
    )

    private val stations = listOf(
        StationSpec("ski-erg", "hyrox-ski-erg", "SkiErg", "Block 1: Start", DT, 1000, null, "Distance", null),
        StationSpec("sled-push", "hyrox-sled-push", "Sled Push", "Block 2: Strength", DT, 50, null, "Heavy Push", Load.SLED_PUSH),
        StationSpec("sled-pull", "hyrox-sled-pull", "Sled Pull", "Block 2: Strength", DT, 50, null, "Resistance", Load.SLED_PULL),
        StationSpec("burpee-broad-jump", "hyrox-burpee-broad-jump", "Burpee Broad Jumps", "Block 3: Agility", DT, 80, null, "Ground Coverage", null),
        StationSpec("rowing", "hyrox-rowing", "Rowing", "Block 4: Engine", DT, 1000, null, "Distance", null),
        StationSpec("farmers-carry", "hyrox-farmers-carry", "Farmers Carry", "Block 5: Grip & Core", DT, 200, null, "Kettlebell Carry", Load.FARMERS),
        StationSpec("sandbag-lunges", "hyrox-sandbag-lunges", "Sandbag Lunges", "Block 5: Grip & Core", DT, 100, null, "Walking Lunges", Load.SANDBAG),
        StationSpec("wall-balls", "hyrox-wall-balls", "Wall Balls", "Block 6: The Finish", REPS, null, 100, "Wall Balls", Load.WALL_BALL),
    )

    private fun order2(n: Int): String = n.toString().padStart(2, '0')

    /** RUN precedes station index [i]; that station's segment id is deterministic at order 2i+2. */
    private fun stationSegmentId(i: Int, slug: String): String = "hyrox:${order2(2 * i + 2)}-$slug"

    val segments: List<EventSegmentEntity> = buildList {
        stations.forEachIndexed { i, s ->
            val runOrder = 2 * i + 1
            val stationOrder = 2 * i + 2
            add(
                EventSegmentEntity(
                    id = "hyrox:${order2(runOrder)}-run",
                    formatKey = EventFormat.HYROX,
                    orderIndex = runOrder,
                    kind = SegmentKind.RUN.name,
                    exerciseId = RUN_EXERCISE,
                    name = "Run ${i + 1}",
                    label = "Run ${i + 1}",
                    metric = DT,
                    distanceM = 1000,
                    descriptor = "1 km run",
                ),
            )
            add(
                EventSegmentEntity(
                    id = stationSegmentId(i, s.slug),
                    formatKey = EventFormat.HYROX,
                    orderIndex = stationOrder,
                    kind = SegmentKind.STATION.name,
                    exerciseId = s.exerciseId,
                    name = s.name,
                    label = s.label,
                    metric = s.metric,
                    distanceM = s.distanceM,
                    reps = s.reps,
                    loadType = s.loadType,
                    descriptor = s.descriptor,
                ),
            )
        }
    }

    /** A division's per-station weights: (loadKg, display). Farmers is per-hand; display keeps "2×N kg". */
    private data class DivSpec(
        val key: String,
        val label: String,
        val gender: Gender,
        val tier: Tier,
        val order: Int,
        val push: Pair<Double, String>,
        val pull: Pair<Double, String>,
        val farmers: Pair<Double, String>,
        val sandbag: Pair<Double, String>,
        val wallBallKg: Double,
        val wallTargetM: Double,
    )

    private val divisionSpecs = listOf(
        DivSpec("WOMEN", "Women", Gender.WOMEN, Tier.OPEN, 0,
            push = 102.0 to "102 kg", pull = 78.0 to "78 kg",
            farmers = 16.0 to "2×16 kg", sandbag = 10.0 to "10 kg", wallBallKg = 4.0, wallTargetM = 2.7),
        DivSpec("MEN", "Men", Gender.MEN, Tier.OPEN, 1,
            push = 152.0 to "152 kg", pull = 103.0 to "103 kg",
            farmers = 24.0 to "2×24 kg", sandbag = 20.0 to "20 kg", wallBallKg = 6.0, wallTargetM = 3.0),
        // Women Pro carries the Men (Open) loads but keeps the women's wall-ball target.
        DivSpec("WOMEN_PRO", "Women Pro", Gender.WOMEN, Tier.PRO, 2,
            push = 152.0 to "152 kg", pull = 103.0 to "103 kg",
            farmers = 24.0 to "2×24 kg", sandbag = 20.0 to "20 kg", wallBallKg = 6.0, wallTargetM = 2.7),
        DivSpec("MEN_PRO", "Men Pro", Gender.MEN, Tier.PRO, 3,
            push = 202.0 to "202 kg", pull = 153.0 to "153 kg",
            farmers = 32.0 to "2×32 kg", sandbag = 30.0 to "30 kg", wallBallKg = 9.0, wallTargetM = 3.0),
    )

    val divisions: List<EventDivisionEntity> = divisionSpecs.map { d ->
        EventDivisionEntity(
            id = "${EventFormat.HYROX}:${d.key}",
            formatKey = EventFormat.HYROX,
            key = d.key,
            label = d.label,
            gender = d.gender.name,
            tier = d.tier.name,
            orderIndex = d.order,
        )
    }

    // Station indices in [stations] whose loads are division-specific.
    private const val I_SLED_PUSH = 1
    private const val I_SLED_PULL = 2
    private const val I_FARMERS = 5
    private const val I_SANDBAG = 6
    private const val I_WALL_BALLS = 7

    val standards: List<SegmentStandardEntity> = buildList {
        val mode = RaceMode.SINGLES.name
        divisionSpecs.forEach { d ->
            fun std(i: Int, slug: String, kg: Double, display: String, reps: Int? = null, heightM: Double? = null) {
                add(
                    SegmentStandardEntity(
                        id = "${EventFormat.HYROX}:${d.key}:$slug",
                        formatKey = EventFormat.HYROX,
                        divisionKey = d.key,
                        segmentId = stationSegmentId(i, slug),
                        mode = mode,
                        loadKg = kg,
                        loadDisplay = display,
                        targetReps = reps,
                        targetHeightM = heightM,
                    ),
                )
            }
            std(I_SLED_PUSH, "sled-push", d.push.first, d.push.second)
            std(I_SLED_PULL, "sled-pull", d.pull.first, d.pull.second)
            std(I_FARMERS, "farmers-carry", d.farmers.first, d.farmers.second)
            std(I_SANDBAG, "sandbag-lunges", d.sandbag.first, d.sandbag.second)
            std(I_WALL_BALLS, "wall-balls", d.wallBallKg, "${d.wallBallKg.toInt()} kg", reps = 100, heightM = d.wallTargetM)
        }
    }
}
