package com.mindset.benchmark

import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.StartupTimingMetric
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Cold-start timing, the before/after pair:
 *  - [startupNoCompilation] = `CompilationMode.None()` — no AOT, the worst case (the "before").
 *  - [startupBaselineProfile] = `CompilationMode.Partial()` — uses the installed Baseline Profile
 *    (the "after").
 *
 * `StartupTimingMetric` reports `timeToInitialDisplayMs` (P50/P90/P99 across iterations) — the
 * numbers that fill the README table. No seeding here: startup measures a clean launch to first frame.
 */
@RunWith(AndroidJUnit4::class)
class StartupBenchmark {

    @get:Rule
    val rule = MacrobenchmarkRule()

    @Test
    fun startupNoCompilation() = measure(CompilationMode.None())

    @Test
    fun startupBaselineProfile() = measure(CompilationMode.Partial())

    private fun measure(compilationMode: CompilationMode) = rule.measureRepeated(
        packageName = TARGET_PACKAGE,
        metrics = listOf(StartupTimingMetric()),
        compilationMode = compilationMode,
        startupMode = StartupMode.COLD,
        iterations = 10,
    ) {
        pressHome()
        startActivityAndWait()
    }
}
