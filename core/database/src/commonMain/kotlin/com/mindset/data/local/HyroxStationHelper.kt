package com.mindset.data.local

import com.mindset.data.local.ExerciseImporter.seedRow
import com.mindset.model.HyroxStation
import com.mindset.model.MetricType
import com.mindset.model.Modality

class HyroxStationHelper {

    private object Load {
        const val SLED_PUSH = "SLED_PUSH"
        const val SLED_PULL = "SLED_PULL"
        const val FARMERS = "FARMERS"
        const val SANDBAG = "SANDBAG"
        const val WALL_BALL = "WALL_BALL"
    }

    val DT = MetricType.DISTANCE_TIME.name
    private val REPS = MetricType.REPS_ONLY.name
    val RUN_EXERCISE = "hyrox-run"

    val hyroxStations: List<Exercise> = listOf(
        station(
            "hyrox-ski-erg",
            "Ski Erg",
            Modality.CONDITIONING,
            MetricType.DISTANCE_TIME,
            HyroxStation.SKI_ERG,
            "ski erg",
        ),
        station(
            "hyrox-sled-push",
            "Sled Push",
            Modality.STRENGTH,
            MetricType.DISTANCE_TIME,
            HyroxStation.SLED_PUSH,
            "sled",
        ),
        station(
            "hyrox-sled-pull",
            "Sled Pull",
            Modality.STRENGTH,
            MetricType.DISTANCE_TIME,
            HyroxStation.SLED_PULL,
            "sled rope",
        ),
        station(
            "hyrox-burpee-broad-jump",
            "Burpee Broad Jump",
            Modality.CONDITIONING,
            MetricType.DISTANCE_TIME,
            HyroxStation.BURPEE_BROAD_JUMP,
            "body only",
        ),
        station(
            "hyrox-rowing",
            "Rowing",
            Modality.CONDITIONING,
            MetricType.DISTANCE_TIME,
            HyroxStation.ROWING,
            "rower",
        ),
        station(
            "hyrox-farmers-carry",
            "Farmers Carry",
            Modality.STRENGTH,
            MetricType.DISTANCE_TIME,
            HyroxStation.FARMERS_CARRY,
            "kettlebell",
        ),
        station(
            "hyrox-sandbag-lunges",
            "Sandbag Lunges",
            Modality.STRENGTH,
            MetricType.DISTANCE_TIME,
            HyroxStation.SANDBAG_LUNGES,
            "sandbag",
        ),
        station(
            "hyrox-wall-balls",
            "Wall Balls",
            Modality.CONDITIONING,
            MetricType.REPS_TIME,
            HyroxStation.WALL_BALLS,
            "medicine ball",
        ),
    )

    /**
     * The 1 km run leg interleaved between HYROX stations. A real catalog row so a synthesized Hyrox
     * session's run steps resolve to a named exercise (distance + time → pace) like any other.
     */
    val hyroxRun: List<Exercise> = listOf(
        seedRow("hyrox-run", "Run", "cardio", Modality.RUN, MetricType.DISTANCE_TIME, "body only", station = null),
    )

    private fun station(
        id: String,
        name: String,
        modality: Modality,
        metric: MetricType,
        station: HyroxStation,
        equipment: String,
    ): Exercise =
        seedRow(id, name, "hyrox", modality, metric, equipment, station)

    /** The 8 stations in race order; runs are interleaved before each when segments are built. */
    data class StationSpec(
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

    val stations =
        listOf(
            StationSpec(
                "ski-erg",
                "hyrox-ski-erg",
                "SkiErg",
                "Block 1: Start",
                DT,
                1000,
                null,
                "Distance",
                null,
            ),
            StationSpec(
                "sled-push",
                "hyrox-sled-push",
                "Sled Push",
                "Block 2: Strength",
                DT,
                50,
                null,
                "Heavy Push",
                Load.SLED_PUSH,
            ),
            StationSpec(
                "sled-pull",
                "hyrox-sled-pull",
                "Sled Pull",
                "Block 2: Strength",
                DT,
                50,
                null,
                "Resistance",
                Load.SLED_PULL,
            ),
            StationSpec(
                "burpee-broad-jump",
                "hyrox-burpee-broad-jump",
                "Burpee Broad Jumps",
                "Block 3: Agility",
                DT,
                80,
                null,
                "Ground Coverage",
                null,
            ),
            StationSpec(
                "rowing",
                "hyrox-rowing",
                "Rowing",
                "Block 4: Engine",
                DT,
                1000,
                null,
                "Distance",
                null,
            ),
            StationSpec(
                "farmers-carry",
                "hyrox-farmers-carry",
                "Farmers Carry",
                "Block 5: Grip & Core",
                DT,
                200,
                null,
                "Kettlebell Carry",
                Load.FARMERS,
            ),
            StationSpec(
                "sandbag-lunges",
                "hyrox-sandbag-lunges",
                "Sandbag Lunges",
                "Block 5: Grip & Core",
                DT,
                100,
                null,
                "Walking Lunges",
                Load.SANDBAG,
            ),
            StationSpec(
                "wall-balls",
                "hyrox-wall-balls",
                "Wall Balls",
                "Block 6: The Finish",
                REPS,
                null,
                100,
                "Wall Balls",
                Load.WALL_BALL,
            ),
        )
}
