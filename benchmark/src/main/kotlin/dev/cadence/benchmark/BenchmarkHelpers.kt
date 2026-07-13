package dev.cadence.benchmark

import androidx.benchmark.macro.MacrobenchmarkScope
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.Until

/** The app-under-test (the `benchmark` build type keeps the release applicationId). */
const val TARGET_PACKAGE = "dev.cadence"

/** Intent extra the app honors (benchmark-only) to bulk-seed sessions so the list is long. */
const val EXTRA_SEED = "cadence_seed"

/** Launch the app, asking it to seed [seed] sessions first (for a non-trivial scroll list). */
fun MacrobenchmarkScope.launchWithSeed(seed: Int = 200) {
    startActivityAndWait { intent -> intent.putExtra(EXTRA_SEED, seed) }
}

/** Tap the History tab and wait for its list to appear. */
fun MacrobenchmarkScope.openHistory() {
    device.findObject(By.text("History"))?.click()
    device.wait(Until.hasObject(By.scrollable(true)), 5_000)
    device.waitForIdle()
}

/** Fling the (only) scrollable list a couple of times to gather frame timings. */
fun MacrobenchmarkScope.flingHistory() {
    val list = device.findObject(By.scrollable(true)) ?: return
    list.setGestureMargin(device.displayWidth / 5)
    repeat(3) {
        list.fling(Direction.DOWN)
        device.waitForIdle()
    }
    list.fling(Direction.UP)
    device.waitForIdle()
}
