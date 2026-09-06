package com.mindset.benchmark

import androidx.benchmark.macro.MacrobenchmarkScope
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.StaleObjectException
import androidx.test.uiautomator.Until

/** The app-under-test (the `benchmark` build type keeps the release applicationId). */
const val TARGET_PACKAGE = "com.mindset"

/** Intent extra the app honors (benchmark-only) to bulk-seed sessions so the list is long. */
const val EXTRA_SEED = "mindset_seed"

/**
 * testTag on the History `LazyColumn`, surfaced as a UI Automator resource-id by
 * `testTagsAsResourceId` at the app root. Must match `Modifier.testTag("history_list")` in the app.
 *
 * Note: `testTagsAsResourceId` writes the tag verbatim into `viewIdResourceName` (no package,
 * no `:id/` prefix), so it must be matched with the single-arg `By.res(String)` — NOT
 * `By.res(pkg, id)`, which looks for `pkg:id/history_list` and silently finds nothing.
 */
const val HISTORY_LIST_TAG = "history_list"

/** Launch the app, asking it to seed [seed] sessions first (for a non-trivial scroll list). */
fun MacrobenchmarkScope.launchWithSeed(seed: Int = 200) {
    startActivityAndWait { intent -> intent.putExtra(EXTRA_SEED, seed) }
}

/**
 * Open History from Home's "See all" and wait for its list to appear.
 *
 * History left the bottom nav (the second tab is now Log, an action that starts a workout), so the
 * entry point is the Recent widget's "See all" affordance.
 *
 * Retries the find+click: right after a cold launch the Home screen is still settling (its data
 * Flow emits and recomposes), so the tab node can be recycled between `findObject` and `click`,
 * throwing [StaleObjectException]. We settle and retry, and fail LOUDLY if the list never shows —
 * otherwise a missing list degrades into a no-op fling and the cryptic "0 found for
 * frameDurationCpuMs" aggregation error downstream.
 */
fun MacrobenchmarkScope.openHistory() {
    device.waitForIdle()
    repeat(5) {
        try {
            device.findObject(By.text("See all"))?.click()
            if (device.wait(Until.hasObject(By.res(HISTORY_LIST_TAG)), 3_000)) {
                device.waitForIdle()
                return
            }
        } catch (_: StaleObjectException) {
            // Node recycled between find and click — settle and retry below.
        }
        device.waitForIdle()
    }
    error("openHistory: History list ('$HISTORY_LIST_TAG') never appeared after 5 attempts")
}

/** Fling the History list a couple of times to gather frame timings. */
fun MacrobenchmarkScope.flingHistory() {
    // Re-resolve the list before EVERY fling. A UiObject2 is a handle to one
    // AccessibilityNodeInfo snapshot; the LazyColumn recycles/recomposes its node as it
    // scrolls, so a cached handle goes stale and the next call throws StaleObjectException.
    val selector = By.res(HISTORY_LIST_TAG)

    fun flingOnce(direction: Direction) {
        val list =
            device.findObject(selector)
                ?: error(
                    "flingHistory: list ('$HISTORY_LIST_TAG') not found — check testTag + testTagsAsResourceId",
                )
        list.setGestureMargin(device.displayWidth / 5)
        list.fling(direction)
        device.waitForIdle()
    }
    repeat(3) { flingOnce(Direction.DOWN) }
    flingOnce(Direction.UP)
}
