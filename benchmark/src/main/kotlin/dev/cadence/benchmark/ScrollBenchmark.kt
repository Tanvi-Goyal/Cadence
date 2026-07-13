package dev.cadence.benchmark

import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.FrameTimingMetric
import androidx.benchmark.macro.StartupMode
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

    private fun measure(compilationMode: CompilationMode) = rule.measureRepeated(
        packageName = TARGET_PACKAGE,
        metrics = listOf(FrameTimingMetric()),
        compilationMode = compilationMode,
        startupMode = StartupMode.COLD,
        iterations = 10,
        setupBlock = {
            launchWithSeed()   // seed + land on Home
            openHistory()      // move to the list we're about to fling
        },
    ) {
        flingHistory()
    }
}
