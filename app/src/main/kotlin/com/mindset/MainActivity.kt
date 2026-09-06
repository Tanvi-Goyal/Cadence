package com.mindset

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.mindset.domain.ActiveWorkoutController
import com.mindset.domain.repository.SessionRepository
import com.mindset.presentation.PreferencesViewModel
import com.mindset.race.RaceNotifications
import com.mindset.race.RaceTimerService
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.koin.android.ext.android.inject
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.context.GlobalContext

class MainActivity : ComponentActivity() {

    private val controller: ActiveWorkoutController by inject()

    /**
     * Asked at race start rather than app start — the platform guidance is to request in context
     * during the related interaction, and a cold-start dialog would also add nondeterminism right
     * where StartupBenchmark measures.
     */
    private val notificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            RaceTimerService.start(this)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
        )
        super.onCreate(savedInstanceState)

        // Macro benchmark-only bulk seed (:benchmark BenchmarkHelpers.EXTRA_SEED), gated to the
        // `benchmark` variant. This Activity is exported, so an ungated hook lets any app on the
        // device inject unbounded rows into a real athlete's history — and the runBlocking below
        // would ANR onCreate doing it.
        if (BuildConfig.SEED_HOOK_ENABLED) {
            val seed = intent.getIntExtra("mindset_seed", 0)
            if (seed > 0) {
                runBlocking { GlobalContext.get().get<SessionRepository>().seedBenchmarkSessions(seed) }
            }
        }

        handleRaceIntent(intent)
        observeRaceForService()

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
                    ActiveWorkoutHost(koinInject())
                }
            }
        }
    }

    /**
     * A notification tap re-opens the running race. `singleTop` means this arrives here rather than
     * recreating the Activity, so the Compose tree and nav back stack are preserved.
     */
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleRaceIntent(intent)
    }

    private fun handleRaceIntent(intent: Intent) {
        if (!intent.getBooleanExtra(RaceNotifications.EXTRA_OPEN_ACTIVE_RACE, false)) return
        intent.removeExtra(RaceNotifications.EXTRA_OPEN_ACTIVE_RACE)
        setIntent(intent)
        // The race is an overlay rather than a route, so expanding it is the whole mechanism — it
        // works regardless of which screen the NavHost happens to be showing, Splash included.
        controller.expand()
    }

    /**
     * Starts the foreground service when a race becomes live, and leaves stopping to the service
     * itself (a race can end while the app is backgrounded and this observer is stopped).
     *
     * Observed here rather than in Application so that no cold start pays for it — the controller
     * singleton is resolved during `setContent` anyway — and so every start edge is guaranteed to be a
     * foreground moment, which keeps clear of the Android 12+ background-start restriction.
     */
    private fun observeRaceForService() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                controller.state
                    .map { it != null && !it.finished }
                    .distinctUntilChanged()
                    .collect { live -> if (live) startRaceService() }
            }
        }
    }

    private fun startRaceService() {
        val needsPermission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        if (needsPermission) {
            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            RaceTimerService.start(this)
        }
    }
}
