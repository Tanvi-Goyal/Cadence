package dev.cadence.di

import dev.cadence.FlowSubscription
import dev.cadence.model.MuscleDiagram
import dev.cadence.domain.MuscleImageProvider
import dev.cadence.domain.repository.SessionRepository
import dev.cadence.model.Exercise
import dev.cadence.presentation.HistoryViewModel
import dev.cadence.presentation.HomeViewModel
import dev.cadence.presentation.LogWorkoutViewModel
import dev.cadence.presentation.NewSessionViewModel
import dev.cadence.presentation.PreferencesViewModel
import dev.cadence.presentation.SessionDetailViewModel
import dev.cadence.presentation.StatsViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.core.parameter.parametersOf
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

fun homeViewModel(): HomeViewModel = KoinPlatform.getKoin().get()

fun statsViewModel(): StatsViewModel = KoinPlatform.getKoin().get()

fun preferencesViewModel(): PreferencesViewModel = KoinPlatform.getKoin().get()

/** Parameterized: the [SessionDetailViewModel] factory takes the sessionId via Koin `parametersOf`. */
fun sessionDetailViewModel(sessionId: String): SessionDetailViewModel =
    KoinPlatform.getKoin().get { parametersOf(sessionId) }

// `do` prefix (as with [doInitKoin]): Kotlin/Native mangles Obj-C selectors starting with `new`
// (the ARC "new" method family), so a bare `newSessionViewModel()` would surface to Swift under a
// surprising auto-generated name. Naming it explicitly keeps Kotlin and Swift in sync.
fun doNewSessionViewModel(): NewSessionViewModel = KoinPlatform.getKoin().get()

fun logWorkoutViewModel(sessionId: String): LogWorkoutViewModel =
    KoinPlatform.getKoin().get { parametersOf(sessionId) }

/**
 * Load the full exercise catalog for the iOS picker. The shared [ExerciseLibraryViewModel] exposes
 * `Flow<PagingData<Exercise>>` (Paging 3), which has no Swift consumer — so on iOS we read the
 * bounded catalog directly and filter client-side. Returns a [FlowSubscription] for cancellation.
 */
fun loadExercises(onResult: (List<Exercise>) -> Unit): FlowSubscription {
    val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    scope.launch {
        val catalog = KoinPlatform.getKoin().get<SessionRepository>().exercisesById()
        onResult(catalog.values.toList())
    }
    return FlowSubscription(scope)
}

/** wger muscle diagram (base body + overlay URLs) for a muscle name; suspend → Swift async. Null if unmapped/offline. */
suspend fun muscleDiagram(muscleName: String): MuscleDiagram? =
    KoinPlatform.getKoin().get<MuscleImageProvider>().diagram(muscleName)
