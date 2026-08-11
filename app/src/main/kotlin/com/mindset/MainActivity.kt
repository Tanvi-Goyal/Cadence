package com.mindset

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mindset.domain.repository.SessionRepository
import com.mindset.presentation.PreferencesViewModel
import kotlinx.coroutines.runBlocking
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.context.GlobalContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT)
        )
        super.onCreate(savedInstanceState)

        // Benchmark-only: the scroll Macrobenchmark launches with `--ei mindset_seed N` to bulk-seed
        // a long History list before first frame. Never fires in normal use (no extra present).
        val seed = intent.getIntExtra("mindset_seed", 0)
        if (seed > 0) {
            runBlocking { GlobalContext.get().get<SessionRepository>().seedBenchmarkSessions(seed) }
        }

        setContent {
            // testTagsAsResourceId maps every Compose testTag to a UI Automator resource-id.
            // Set once at the root; it propagates down the semantics tree to all descendants.
            // Zero cost in production traffic — it only changes what the accessibility tree exposes.
            // Read preferences once at the root and publish theme + unit down the tree, so the whole
            // app re-themes / re-labels from a single source when the Profile screen changes them.
            val prefs by koinViewModel<PreferencesViewModel>()
                .preferences
                .collectAsStateWithLifecycle()
            Box(Modifier.semantics { testTagsAsResourceId = true }) {
                CompositionLocalProvider(
                    LocalThemeMode provides prefs.themeMode,
                    LocalWeightUnit provides prefs.weightUnit,
                ) {
                    MindSetNavHost()
                    // App-scoped live-workout overlay: floats over every screen, survives navigation, and
                    // observes the single ActiveWorkoutController (the seam a future widget / notification reuse).
                    ActiveWorkoutHost(koinInject())
                }
            }
        }
    }
}
