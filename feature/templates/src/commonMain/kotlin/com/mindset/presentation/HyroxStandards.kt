package com.mindset.presentation

/*
 * HYROX station standards + a pure builder that turns (division, variant) into the block list the
 * detail screen renders.
 *
 * Weights verified against hyrox.com → "The Fitness Race" → "Weights, Distances & Repetitions"
 * (captured 2026). Distances and reps are CONSTANT across divisions; only the weights change. This is
 * ILLUSTRATIVE-BUT-VERIFIED sample data living in code — it must migrate to verified DB seed data in the
 * data pass (station standards are data, not hardcode; see the Hyrox-standards note). Doubles/Relay reuse
 * the Open weights and Mixed (F/M split) is deferred, so only the four distinct weight profiles exist.
 */

/** The four HYROX weight profiles the selector exposes. */
enum class HyroxDivision(val label: String) {
    WOMEN("Women"),
    MEN("Men"),
    WOMEN_PRO("Women Pro"),
    MEN_PRO("Men Pro"),
}

/** Which slice of the race a sim covers. FULL = the complete 8 (full sim). The half sim offers the
 *  other three: the front four, the back four, or all eight at half distance/reps. */
enum class HyroxVariant(val label: String) {
    FULL("Full"),
    FIRST_HALF("1st Half"),
    SECOND_HALF("2nd Half"),
    HALVED("Full (Halved)"),
}

object HyroxStandards {
    /** Which division-dependent weight a station uses (null = bodyweight/erg, no weight). */
    private enum class Load { SLED_PUSH, SLED_PULL, FARMERS, SANDBAG, WALL_BALL }

    /** Per-division weights (display strings straight from the rulebook table). */
    private data class DivWeights(
        val sledPush: String,
        val sledPull: String,
        val farmers: String,
        val sandbag: String,
        val wallBallKg: Int,
        val wallTargetM: String,
    )

    private val WEIGHTS =
        mapOf(
            HyroxDivision.WOMEN to DivWeights("102 kg", "78 kg", "2×16 kg", "10 kg", 4, "2.7"),
            HyroxDivision.MEN to DivWeights("152 kg", "103 kg", "2×24 kg", "20 kg", 6, "3"),
            // Women Pro carries the same loads as Men (Open) but keeps the women's wall-ball target.
            HyroxDivision.WOMEN_PRO to DivWeights("152 kg", "103 kg", "2×24 kg", "20 kg", 6, "2.7"),
            HyroxDivision.MEN_PRO to DivWeights("202 kg", "153 kg", "2×32 kg", "30 kg", 9, "3"),
        )

    /** One canonical station in race order. `distanceM` is the full-race distance (halved for HALVED);
     *  wall balls use `reps` instead. `load` picks the division weight; null stations show distance. */
    private data class Station(
        val number: Int,
        val glyph: HyroxGlyph,
        val name: String,
        val block: String,
        val runBefore: String,
        val distanceM: Int,
        val descriptor: String,
        val load: Load?,
        val reps: Int? = null,
    )

    private val STATIONS =
        listOf(
            Station(
                1,
                HyroxGlyph.SKI_ERG,
                "SkiErg",
                "Block 1: Start",
                "Controlled Start",
                1000,
                "Distance",
                null,
            ),
            Station(
                2,
                HyroxGlyph.SLED_PUSH,
                "Sled Push",
                "Block 2: Strength",
                "Aerobic Base",
                50,
                "Heavy Push",
                Load.SLED_PUSH,
            ),
            Station(
                3,
                HyroxGlyph.SLED_PULL,
                "Sled Pull",
                "Block 2: Strength",
                "Pacing Strategy",
                50,
                "Resistance",
                Load.SLED_PULL,
            ),
            Station(
                4,
                HyroxGlyph.BURPEE,
                "Burpee Broad Jumps",
                "Block 3: Agility",
                "Interval Mode",
                80,
                "Ground Coverage",
                null,
            ),
            Station(
                5,
                HyroxGlyph.ROWING,
                "Rowing",
                "Block 4: Engine",
                "Recovery Pace",
                1000,
                "Distance",
                null,
            ),
            Station(
                6,
                HyroxGlyph.FARMERS_CARRY,
                "Farmers Carry",
                "Block 5: Grip & Core",
                "Steady State",
                200,
                "Kettlebell Carry",
                Load.FARMERS,
            ),
            Station(
                7,
                HyroxGlyph.SANDBAG_LUNGES,
                "Sandbag Lunges",
                "Block 5: Grip & Core",
                "Threshold Pace",
                100,
                "Walking Lunges",
                Load.SANDBAG,
            ),
            Station(
                8,
                HyroxGlyph.WALL_BALLS,
                "Wall Balls",
                "Block 6: The Finish",
                "Final Push",
                0,
                "",
                Load.WALL_BALL,
                reps = 100,
            ),
        )

    /** Build the run/station block list for a division + variant. Pure — no state. */
    fun buildBlocks(division: HyroxDivision, variant: HyroxVariant): List<HyroxBlock> {
        val selected =
            when (variant) {
                HyroxVariant.FIRST_HALF -> STATIONS.filter { it.number in 1..4 }
                HyroxVariant.SECOND_HALF -> STATIONS.filter { it.number in 5..8 }
                HyroxVariant.FULL, HyroxVariant.HALVED -> STATIONS
            }
        val halve = variant == HyroxVariant.HALVED
        val runValue = if (halve) "0.5 km" else "1.0 km"
        val weights = WEIGHTS.getValue(division)

        // Preserve race order while grouping consecutive stations under their thematic block.
        val grouped = LinkedHashMap<String, MutableList<HyroxRow>>()
        for (s in selected) {
            val rows = grouped.getOrPut(s.block) { mutableListOf() }
            rows +=
                HyroxRow(HyroxRowKind.RUN, HyroxGlyph.RUN, "Run ${s.number}", s.runBefore, runValue)
            rows += stationRow(s, weights, halve)
        }
        return grouped.map { (label, rows) -> HyroxBlock(label, rows) }
    }

    private fun stationRow(s: Station, w: DivWeights, halve: Boolean): HyroxRow {
        val title = "${s.number}. ${s.name}"
        return when (s.load) {
            Load.WALL_BALL -> {
                val reps = if (halve) (s.reps ?: 100) / 2 else (s.reps ?: 100)
                HyroxRow(
                    HyroxRowKind.STATION,
                    s.glyph,
                    title,
                    detail = "${w.wallBallKg}kg Ball • ${w.wallTargetM}m Target",
                    value = "$reps reps",
                )
            }

            null -> {
                val dist = if (halve) s.distanceM / 2 else s.distanceM
                HyroxRow(
                    HyroxRowKind.STATION,
                    s.glyph,
                    title,
                    "${dist}m ${s.descriptor}",
                    "${dist}m",
                )
            }

            else -> {
                val dist = if (halve) s.distanceM / 2 else s.distanceM
                HyroxRow(
                    HyroxRowKind.STATION,
                    s.glyph,
                    title,
                    "${dist}m ${s.descriptor}",
                    weightFor(s.load, w),
                )
            }
        }
    }

    private fun weightFor(load: Load, w: DivWeights): String = when (load) {
        Load.SLED_PUSH -> w.sledPush
        Load.SLED_PULL -> w.sledPull
        Load.FARMERS -> w.farmers
        Load.SANDBAG -> w.sandbag
        Load.WALL_BALL -> "${w.wallBallKg} kg"
    }
}
