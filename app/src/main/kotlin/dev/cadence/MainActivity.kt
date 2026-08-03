package dev.cadence

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.cadence.domain.SessionRepository
import dev.cadence.presentation.PreferencesViewModel
import kotlinx.coroutines.runBlocking
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.context.GlobalContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // Benchmark-only: the scroll Macrobenchmark launches with `--ei cadence_seed N` to bulk-seed
        // a long History list before first frame. Never fires in normal use (no extra present).
        val seed = intent.getIntExtra("cadence_seed", 0)
        if (seed > 0) {
            runBlocking { GlobalContext.get().get<SessionRepository>().seedBenchmarkSessions(seed) }
        }

        setContent {
            // testTagsAsResourceId maps every Compose testTag to a UI Automator resource-id.
            // Set once at the root; it propagates down the semantics tree to all descendants.
            // Zero cost in production traffic — it only changes what the accessibility tree exposes.
            // Read preferences once at the root and publish theme + unit down the tree, so the whole
            // app re-themes / re-labels from a single source when the Profile screen changes them.
            val prefs by koinViewModel<PreferencesViewModel>().preferences.collectAsStateWithLifecycle()
            // In-memory design-system toggle (NOT persisted, defaults to Obsidian) — an ephemeral
            // switch to compare the two themes; one is removed later. Resets on process death by design.
            var themeVariant by remember { mutableStateOf(ThemeVariant.OBSIDIAN) }
            Box(Modifier.semantics { testTagsAsResourceId = true }) {
                CompositionLocalProvider(
                    LocalThemeMode provides prefs.themeMode,
                    LocalWeightUnit provides prefs.weightUnit,
                    LocalThemeVariant provides themeVariant,
                    LocalSetThemeVariant provides { themeVariant = it },
                ) {
                    CadenceNavHost()
                    // App-scoped live-workout overlay: floats over every screen, survives navigation, and
                    // observes the single ActiveWorkoutController (the seam a future widget / notification reuse).
                    // Inside the provider so it recolors with the active theme variant too.
                    ActiveWorkoutHost(koinInject())
                }
            }
        }
    }
}