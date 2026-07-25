package dev.cadence

import kotlinx.serialization.Serializable

/*
 * Type-safe navigation routes. Each destination is a @Serializable object (no args) or data class
 * (typed args), so navigation is `nav.navigate(LogWorkout(id))` and args are read as real fields
 * (`entry.toRoute<LogWorkout>().sessionId`) — a wrong or renamed arg is a compile error, not a
 * runtime crash. The four bottom-nav tabs stay string-routed (see [dev.cadence.Tab]); only the
 * arg-carrying push destinations are typed.
 */

// Bottom-nav tab destinations (no args). The Tab enum in :core:ui is the UI model; these are the routes.
@Serializable
object Home

@Serializable
object History

@Serializable
object Stats

@Serializable
object Profile

@Serializable
object NewSession

@Serializable
data class LogWorkout(val sessionId: String)

@Serializable
data class ExercisePicker(val target: PickerTarget)

@Serializable
data class ExerciseDetail(val exerciseId: String, val target: PickerTarget)

@Serializable
data class SessionDetail(val sessionId: String)

@Serializable
object Credits

@Serializable
object Templates

@Serializable
data class TemplateDetail(val templateId: String)

@Serializable
data class TemplateHyroxDetail(val templateId: String)

@Serializable
data class TemplateStrengthDetail(val templateId: String)

@Serializable
object NewTemplate

@Serializable
data class TemplateBuilder(val templateId: String)

/**
 * Where the shared exercise picker should hand its pick back to. Both Log Workout and the Template
 * Builder launch the same picker → detail flow; the target rides through the picker/detail routes as
 * a typed arg so the detail screen knows which back-stack entry to return the chosen exercise to.
 */
@Serializable
enum class PickerTarget { LOG, BUILDER
}

/**
 * `savedStateHandle` key the exercise detail screen writes the chosen exercise id into, for the
 * originating Log Workout / Template Builder entry to read on the way back. A shared constant so the
 * writer and reader agree once these screens split into separate feature modules (B11).
 */
const val PICKED_EXERCISE = "pickedExercise"
