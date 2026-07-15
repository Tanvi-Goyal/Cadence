package dev.cadence.di

import dev.cadence.data.SessionRepository
import dev.cadence.presentation.HistoryViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.mp.KoinPlatform

/**
 * Swift-friendly entry point, callable from `iOSApp.init()`. In a clean-named file so the
 * generated Objective-C/Swift facade is the predictable `KoinIosKt`.
 */
fun doInitKoin() {
    initKoin()
}

/**
 * Resolve the shared [HistoryViewModel] for Swift. The ViewModel graph lives in `commonMain`; this
 * is the one Swift-callable seam that hands an instance across (Swift holds it in an ObservableObject).
 */
fun historyViewModel(): HistoryViewModel = KoinPlatform.getKoin().get()

/**
 * Dev-only: top up the DB to a small set of sessions so the History screen shows real data on iOS
 * (there is no create-session screen on iOS yet). Idempotent; fire-and-forget off the main thread.
 * Remove once iOS can create sessions itself.
 */
fun seedDemoData() {
    val repository = KoinPlatform.getKoin().get<SessionRepository>()
    CoroutineScope(SupervisorJob() + Dispatchers.Default).launch {
        repository.seedBenchmarkSessions(20)
    }
}
