package dev.cadence.domain

/** Weight unit the UI displays loads/volume in. Storage/compute is always kg; this is display-only. */
enum class WeightUnit { KG, LB }

/** App theme preference. SYSTEM follows the OS light/dark setting. */
enum class ThemeMode { SYSTEM, LIGHT, DARK }

/** User-tunable preferences. Device-local (never synced). */
data class UserPreferences(
    val weightUnit: WeightUnit = WeightUnit.KG,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
)

/**
 * Weight conversion + labelling. All weights are stored and computed in **kg**; this converts to the
 * user's display unit (and back for input) so the conversion math lives once, shared by both platforms.
 */
object Units {
    private const val LB_PER_KG = 2.2046226218

    /** kg (canonical) → the value shown in [unit]. */
    fun toDisplay(kg: Double, unit: WeightUnit): Double =
        when (unit) {
            WeightUnit.KG -> kg
            WeightUnit.LB -> kg * LB_PER_KG
        }

    /** A value the user typed in [unit] → kg (canonical) for storage. */
    fun toKg(input: Double, unit: WeightUnit): Double =
        when (unit) {
            WeightUnit.KG -> input
            WeightUnit.LB -> input / LB_PER_KG
        }

    fun label(unit: WeightUnit): String =
        when (unit) {
            WeightUnit.KG -> "kg"
            WeightUnit.LB -> "lb"
        }
}
