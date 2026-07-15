package dev.cadence.benchmark

import androidx.benchmark.macro.MacrobenchmarkScope
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.Until

/** The app-under-test (the `benchmark` build type keeps the release applicationId). */
const val TARGET_PACKAGE = "dev.cadence"

/** Intent extra the app honors (benchmark-only) to bulk-seed sessions so the list is long. */
const val EXTRA_SEED = "cadence_seed"

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

/** Tap the History tab and wait for its list to appear. */
fun MacrobenchmarkScope.openHistory() {
    device.findObject(By.text("History"))?.click()
    device.wait(Until.hasObject(By.res(HISTORY_LIST_TAG)), 5_000)
    device.waitForIdle()
}

/** Fling the History list a couple of times to gather frame timings. */
fun MacrobenchmarkScope.flingHistory() {
    // Re-resolve the list before EVERY fling. A UiObject2 is a handle to one
    // AccessibilityNodeInfo snapshot; the LazyColumn recycles/recomposes its node as it
    // scrolls, so a cached handle goes stale and the next call throws StaleObjectException.
    val selector = By.res(HISTORY_LIST_TAG)
    fun flingOnce(direction: Direction) {
        val list = device.findObject(selector) ?: return
        list.setGestureMargin(device.displayWidth / 5)
        list.fling(direction)
        device.waitForIdle()
    }
    repeat(3) { flingOnce(Direction.DOWN) }
    flingOnce(Direction.UP)
}
