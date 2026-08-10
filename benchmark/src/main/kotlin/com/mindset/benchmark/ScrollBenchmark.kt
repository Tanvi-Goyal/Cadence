package com.mindset.benchmark

import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.FrameTimingMetric
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Frame timing while flinging the History list (seeded to ~200 sessions). `FrameTimingMetric`
 * reports `frameOverrunMs` (how far past the frame deadline a frame ran — the jank signal) and
 * `frameDurationCpuMs`. Run with None vs Partial() to show the Baseline Profile's effect on jank.
 */
@RunWith(AndroidJUnit4::class)
class ScrollBenchmark {
    @get:Rule
    val rule = MacrobenchmarkRule()

    @Test
    fun scrollNoCompilation() = measure(CompilationMode.None())

    @Test
    fun scrollBaselineProfile() = measure(CompilationMode.Partial())

    // No `startupMode`: it's a startup-benchmark concept. With StartupMode.COLD the framework
    // kills the process *between* setupBlock and measureBlock (so the measureBlock can measure a
    // fresh launch) — which would kill the app we just navigated to History. A scroll benchmark
    // wants the app alive and on the loaded list, measuring only the fling frames, so we omit it.
    private fun measure(compilationMode: CompilationMode) = rule.measureRepeated(
        packageName = TARGET_PACKAGE,
        metrics = listOf(FrameTimingMetric()),
        compilationMode = compilationMode,
        iterations = 10,
        setupBlock = {
            launchWithSeed() // seed + land on Home
            openHistory() // move to the list we're about to fling
        },
    ) {
        flingHistory()
    }
}
