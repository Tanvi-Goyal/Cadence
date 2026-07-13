package dev.cadence

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dev.cadence.data.SessionRepository
import kotlinx.coroutines.runBlocking
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
            CadenceNavHost()
        }
    }
}