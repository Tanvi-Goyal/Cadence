package com.mindset.benchmark

import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Generates the Baseline Profile by exercising the app's hot paths — cold launch plus the screens
 * users hit first (Home → History with a scroll → Stats). The classes/methods touched here get
 * ahead-of-time compiled on install, which is what the before/after table measures.
 *
 * Run via `./gradlew :composeApp:generateBaselineProfile` on a connected device.
 */
@RunWith(AndroidJUnit4::class)
class BaselineProfileGenerator {
    @get:Rule
    val rule = BaselineProfileRule()

    @Test
    fun generate() = rule.collect(packageName = TARGET_PACKAGE) {
        pressHome()
        launchWithSeed() // cold start (+ seed so History is a real list)
        openHistory()
        flingHistory()
        device
            .findObject(
                androidx.test.uiautomator.By
                    .text("Stats"),
            )?.click()
        device.waitForIdle()
    }
}
