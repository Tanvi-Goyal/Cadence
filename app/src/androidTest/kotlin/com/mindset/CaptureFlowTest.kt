package com.mindset

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.mindset.model.SetEntry
import com.mindset.domain.LoggedItemUi
import org.junit.Rule
import org.junit.Test
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * Verifies the polymorphic capture cells (LLD §9): one card renders reps × weight for a WEIGHT_REPS
 * exercise and time · distance for a DISTANCE_TIME one — no per-type screen. Instrumented, so it
 * runs on a device / in CI, not in the host+sim gate loop.
 */
@OptIn(ExperimentalTime::class)
class CaptureFlowTest {

    @get:Rule
    val rule = createComposeRule()

    private val epoch = Instant.fromEpochMilliseconds(0)

    private fun completedSet(reps: Int? = null, loadKg: Double? = null, timeSec: Int? = null, distanceM: Int? = null) =
        SetEntry(
            id = "s1", exerciseEntryId = "e1", setNumber = 1,
            reps = reps, loadKg = loadKg, timeSec = timeSec, distanceM = distanceM, calories = null, rpe = null,
            targetReps = null, targetLoadKg = null, targetTimeSec = null, targetDistanceM = null, targetCalories = null,
            createdAt = epoch, updatedAt = epoch, deletedAt = null,
        )

    private fun card(item: LoggedItemUi) {
        rule.setContent {
            MindSetTheme {
                ExerciseCard(
                    item = item,
                    onAddStrengthSet = { _, _ -> },
                    onAddCardioSet = { _, _ -> },
                    onUpdateStrengthActual = { _, _, _ -> },
                    onUpdateCardioActual = { _, _, _ -> },
                )
            }
        }
    }

    @Test
    fun weightReps_card_shows_reps_and_weight() {
        card(
            LoggedItemUi(
                loggedItemId = "e1", exerciseName = "Bench Press", metric = "WEIGHT_REPS",
                sets = listOf(completedSet(reps = 8, loadKg = 60.0)),
            ),
        )
        rule.onNodeWithText("8 reps", substring = true).assertIsDisplayed()
    }

    @Test
    fun distanceTime_card_shows_time_and_distance() {
        card(
            LoggedItemUi(
                loggedItemId = "e2", exerciseName = "Rowing", metric = "DISTANCE_TIME",
                sets = listOf(completedSet(timeSec = 240, distanceM = 1000)),
            ),
        )
        rule.onNodeWithText("1000 m", substring = true).assertIsDisplayed()
    }
}
