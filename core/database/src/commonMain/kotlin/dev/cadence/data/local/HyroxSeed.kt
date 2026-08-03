package dev.cadence.data.local

import dev.cadence.model.MetricType

/**
 * Seed rows for the HYROX reference tables ([HyroxStationRef]/[HyroxDivisionRef]/[HyroxStationLoadRef]).
 *
 * Values verified against hyrox.com → "Weights, Distances & Repetitions" (captured 2026). Distances
 * and reps are CONSTANT across divisions; only the weights change. These are the same figures that
 * previously lived in `feature/templates`'s in-code `HyroxStandards` — moved here so the format is
 * DB-seeded reference data (like the exercise catalog) and the app never reads hardcoded standards at
 * runtime. Bump [SessionRepositoryImpl]'s HYROX_SEED_VERSION when these change.
 */
object HyroxSeed {

    val stations: List<HyroxStationRef> = listOf(
        HyroxStationRef(
            id = "ski-erg", number = 1, exerciseId = "hyrox-ski-erg", name = "SkiErg",
            blockLabel = "Block 1: Start", runBeforeLabel = "Controlled Start",
            metric = MetricType.DISTANCE_TIME.name, distanceM = 1000, descriptor = "Distance",
        ),
        HyroxStationRef(
            id = "sled-push", number = 2, exerciseId = "hyrox-sled-push", name = "Sled Push",
            blockLabel = "Block 2: Strength", runBeforeLabel = "Aerobic Base",
            metric = MetricType.DISTANCE_TIME.name, distanceM = 50, descriptor = "Heavy Push",
            loadType = HyroxLoadType.SLED_PUSH,
        ),
        HyroxStationRef(
            id = "sled-pull", number = 3, exerciseId = "hyrox-sled-pull", name = "Sled Pull",
            blockLabel = "Block 2: Strength", runBeforeLabel = "Pacing Strategy",
            metric = MetricType.DISTANCE_TIME.name, distanceM = 50, descriptor = "Resistance",
            loadType = HyroxLoadType.SLED_PULL,
        ),
        HyroxStationRef(
            id = "burpee-broad-jump", number = 4, exerciseId = "hyrox-burpee-broad-jump",
            name = "Burpee Broad Jumps", blockLabel = "Block 3: Agility", runBeforeLabel = "Interval Mode",
            metric = MetricType.DISTANCE_TIME.name, distanceM = 80, descriptor = "Ground Coverage",
        ),
        HyroxStationRef(
            id = "rowing", number = 5, exerciseId = "hyrox-rowing", name = "Rowing",
            blockLabel = "Block 4: Engine", runBeforeLabel = "Recovery Pace",
            metric = MetricType.DISTANCE_TIME.name, distanceM = 1000, descriptor = "Distance",
        ),
        HyroxStationRef(
            id = "farmers-carry", number = 6, exerciseId = "hyrox-farmers-carry", name = "Farmers Carry",
            blockLabel = "Block 5: Grip & Core", runBeforeLabel = "Steady State",
            metric = MetricType.DISTANCE_TIME.name, distanceM = 200, descriptor = "Kettlebell Carry",
            loadType = HyroxLoadType.FARMERS,
        ),
        HyroxStationRef(
            id = "sandbag-lunges", number = 7, exerciseId = "hyrox-sandbag-lunges", name = "Sandbag Lunges",
            blockLabel = "Block 5: Grip & Core", runBeforeLabel = "Threshold Pace",
            metric = MetricType.DISTANCE_TIME.name, distanceM = 100, descriptor = "Walking Lunges",
            loadType = HyroxLoadType.SANDBAG,
        ),
        HyroxStationRef(
            id = "wall-balls", number = 8, exerciseId = "hyrox-wall-balls", name = "Wall Balls",
            blockLabel = "Block 6: The Finish", runBeforeLabel = "Final Push",
            metric = MetricType.REPS_ONLY.name, reps = 100, descriptor = "",
            loadType = HyroxLoadType.WALL_BALL,
        ),
    )

    val divisions: List<HyroxDivisionRef> = listOf(
        HyroxDivisionRef("WOMEN", "Women", 0, wallBallKg = 4, wallTargetM = "2.7"),
        HyroxDivisionRef("MEN", "Men", 1, wallBallKg = 6, wallTargetM = "3"),
        // Women Pro carries the Men (Open) loads but keeps the women's wall-ball target.
        HyroxDivisionRef("WOMEN_PRO", "Women Pro", 2, wallBallKg = 6, wallTargetM = "2.7"),
        HyroxDivisionRef("MEN_PRO", "Men Pro", 3, wallBallKg = 9, wallTargetM = "3"),
    )

    val loads: List<HyroxStationLoadRef> = buildList {
        fun add(division: String, push: String, pull: String, farmers: String, sandbag: String) {
            this += HyroxStationLoadRef("$division:${HyroxLoadType.SLED_PUSH}", division, HyroxLoadType.SLED_PUSH, push)
            this += HyroxStationLoadRef("$division:${HyroxLoadType.SLED_PULL}", division, HyroxLoadType.SLED_PULL, pull)
            this += HyroxStationLoadRef("$division:${HyroxLoadType.FARMERS}", division, HyroxLoadType.FARMERS, farmers)
            this += HyroxStationLoadRef("$division:${HyroxLoadType.SANDBAG}", division, HyroxLoadType.SANDBAG, sandbag)
        }
        add("WOMEN", push = "102 kg", pull = "78 kg", farmers = "2×16 kg", sandbag = "10 kg")
        add("MEN", push = "152 kg", pull = "103 kg", farmers = "2×24 kg", sandbag = "20 kg")
        add("WOMEN_PRO", push = "152 kg", pull = "103 kg", farmers = "2×24 kg", sandbag = "20 kg")
        add("MEN_PRO", push = "202 kg", pull = "153 kg", farmers = "2×32 kg", sandbag = "30 kg")
    }
}
