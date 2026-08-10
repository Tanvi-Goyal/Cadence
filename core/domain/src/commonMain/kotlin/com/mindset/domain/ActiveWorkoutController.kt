package com.mindset.domain

import com.mindset.model.HyroxStationModel
import com.mindset.model.HyroxVariant
import kotlinx.coroutines.flow.StateFlow

/**
 * A live HYROX workout in progress — the observable state the timer UI (and later a foreground-service
 * notification / home-screen widget) render. Elapsed values tick up while running; splits reset each
 * time the user advances a step.
 */
data class ActiveWorkout(
    val sessionId: String,
    val divisionKey: String,
    val variant: HyroxVariant,
    val steps: List<HyroxStationModel>,
    val currentIndex: Int,
    val totalElapsedMs: Long,
    val splitElapsedMs: Long,
    val paused: Boolean,
    val finished: Boolean,
) {
    val totalSteps: Int get() = steps.size
    val current: HyroxStationModel? get() = steps.getOrNull(currentIndex)
    val next1: HyroxStationModel? get() = steps.getOrNull(currentIndex + 1)
    val next2: HyroxStationModel? get() = steps.getOrNull(currentIndex + 2)
}

/**
 * App-scoped single source of truth for the one live workout. Deliberately NOT a screen ViewModel:
 * it owns its own tick loop and outlives navigation, so any surface — the Templates timer sheet, Home,
 * a future widget, a future foreground service — observes the SAME [state]. The underlying session is
 * persisted (splits + finish time), which also makes later process-death restore additive.
 */
interface ActiveWorkoutController {

    /** The current live workout, or null when none is running. */
    val state: StateFlow<ActiveWorkout?>

    /**
     * Presentation-only: whether the full timer sheet is [expand]ed (true) or minimized to the Home
     * mini-card ([collapse]d, false). Does NOT affect the clock, splits, or persistence — the workout
     * keeps running either way; this only chooses which surface shows it. Meaningless while [state] is
     * null.
     */
    val expanded: StateFlow<Boolean>

    /* Synthesizes + starts a HYROX workout for the chosen division/variant and begins the clock. */
//    fun startHyrox(divisionKey: String, variant: HyroxVariant, templateId: String)

    /** Pause / resume the running clock (both total and current split). */
    fun pause()
    fun resume()

    /** Complete the current step (persisting its split) and advance — or finish if it was the last. */
    fun next()

    /** Restart the whole workout from the first step with a zeroed clock. */
    fun reset()

    /** Minimize the full sheet to the Home mini-card (the workout keeps running). */
    fun collapse()

    /** Re-open the full sheet from the minimized Home mini-card. */
    fun expand()

    /** Clear the live state (close the sheet). A finished workout stays in History. */
    fun dismiss()
}
