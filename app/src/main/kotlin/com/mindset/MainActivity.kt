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
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
        )
        super.onCreate(savedInstanceState)

        val seed = intent.getIntExtra("mindset_seed", 0)
        if (seed > 0) {
            runBlocking { GlobalContext.get().get<SessionRepository>().seedBenchmarkSessions(seed) }
        }

        setContent {
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
