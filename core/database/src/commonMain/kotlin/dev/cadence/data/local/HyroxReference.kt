package dev.cadence.data.local

import androidx.room3.Dao
import androidx.room3.Entity
import androidx.room3.Index
import androidx.room3.PrimaryKey
import androidx.room3.Query
import androidx.room3.Upsert

/**
 * HYROX reference data — the verified race format as DATA, not hardcode. Three tables:
 *  - [HyroxStationRef]      the 8 functional stations in race order + their fixed distances/reps.
 *  - [HyroxDivisionRef]     the 4 weight profiles (Women / Men / Women Pro / Men Pro).
 *  - [HyroxStationLoadRef]  the per-division weight for each loaded station.
 *
 * Seeded REFERENCE data — identical on every device, so (like [Exercise]) it carries NO sync
 * envelope and NO outbox row. Stable slug ids. This replaces the in-code `HyroxStandards` table so
 * the timer and the detail screen both read one source (the DB).
 *
 * Entities are suffixed `Ref` to avoid colliding with the `dev.cadence.model.HyroxStation` enum,
 * which is imported into this package by [ExerciseImporter].
 */
@Entity(tableName = "hyrox_stations")
data class HyroxStationRef(
    @PrimaryKey val id: String,          // slug, e.g. "ski-erg"
    val number: Int,                     // 1..8 race order
    val exerciseId: String,              // → catalog Exercise id (hyrox-*)
    val name: String,
    val blockLabel: String,              // thematic grouping, e.g. "Block 1: Start"
    val runBeforeLabel: String,          // the run cue that precedes this station
    val metric: String,                  // dev.cadence.model.MetricType name
    val distanceM: Int? = null,          // full-race distance (null for rep-scored stations)
    val reps: Int? = null,               // rep count for rep-scored stations (wall balls)
    val descriptor: String = "",
    /** Which per-division weight this station uses (a [HyroxLoadType]); null = bodyweight / erg. */
    val loadType: String? = null,
)

@Entity(tableName = "hyrox_divisions")
data class HyroxDivisionRef(
    @PrimaryKey val key: String,         // WOMEN / MEN / WOMEN_PRO / MEN_PRO
    val label: String,
    val orderIndex: Int,
    val wallBallKg: Int,
    val wallTargetM: String,
)

@Entity(tableName = "hyrox_station_loads", indices = [Index("divisionKey")])
data class HyroxStationLoadRef(
    @PrimaryKey val id: String,          // "<divisionKey>:<loadType>"
    val divisionKey: String,
    val loadType: String,                // a [HyroxLoadType]
    val weightDisplay: String,           // rulebook string, e.g. "2×24 kg"
)

/** The loaded stations whose weight varies by division. Wall balls read weight from the division. */
object HyroxLoadType {
    const val SLED_PUSH = "SLED_PUSH"
    const val SLED_PULL = "SLED_PULL"
    const val FARMERS = "FARMERS"
    const val SANDBAG = "SANDBAG"
    const val WALL_BALL = "WALL_BALL"
}

@Dao
interface HyroxRefDao {
    @Query("SELECT COUNT(*) FROM hyrox_stations")
    suspend fun stationCount(): Int

    @Upsert suspend fun upsertStations(rows: List<HyroxStationRef>)

    @Upsert suspend fun upsertDivisions(rows: List<HyroxDivisionRef>)

    @Upsert suspend fun upsertLoads(rows: List<HyroxStationLoadRef>)

    @Query("SELECT * FROM hyrox_stations ORDER BY number")
    suspend fun stations(): List<HyroxStationRef>

    @Query("SELECT * FROM hyrox_divisions ORDER BY orderIndex")
    suspend fun divisions(): List<HyroxDivisionRef>

    @Query("SELECT * FROM hyrox_station_loads WHERE divisionKey = :divisionKey")
    suspend fun loadsForDivision(divisionKey: String): List<HyroxStationLoadRef>
}
