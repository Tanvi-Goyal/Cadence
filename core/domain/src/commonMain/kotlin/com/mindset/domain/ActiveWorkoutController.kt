package com.mindset.domain

import com.mindset.model.ActiveWorkout
import com.mindset.model.HyroxVariant
import kotlinx.coroutines.flow.StateFlow

/**
 * App-scoped single source of truth for the one live workout. Deliberately NOT a screen ViewModel:
 * it owns its own tick loop and outlives navigation, so any surface — the Templates timer sheet, Home,
 * a future widget, a future foreground service — observes the SAME [state]. The underlying session is
 * persisted (splits + finish time), which also makes later process-death restore additive.
 */
interface ActiveWorkoutController {

    val state: StateFlow<ActiveWorkout?>

    val expanded: StateFlow<Boolean>

    fun startHyrox(divisionKey: String, variant: HyroxVariant, templateId: String)

    /**
     * Rehydrates a race that outlived the process, from device-local storage.
     *
     * Idempotent and fire-and-forget — the first call per process wins. A restored race is published
     * **paused** at its last observed elapsed: wall time that passed while nothing was watching the
     * clock is deliberately NOT counted, so a restored total can never silently exceed the race that
     * was actually run. No-ops when a race is already live, so it can never clobber one.
     */
    fun restore()

    fun pause()
    fun resume()

    fun next()

    fun reset()

    fun collapse()

    fun expand()

    fun dismiss()
}
