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

    fun pause()
    fun resume()

    fun next()

    fun reset()

    fun collapse()

    fun expand()

    fun dismiss()
}
