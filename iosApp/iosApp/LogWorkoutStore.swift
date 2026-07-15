import Foundation
import Shared

/// Adapter around the parameterized shared `LogWorkoutViewModel`. Observes `uiState` and forwards
/// the mutation intents (add exercise / add set) — each writes through the shared repository.
final class LogWorkoutStore: ObservableObject {
    @Published private(set) var state: LogWorkoutUiState?

    private let viewModel: LogWorkoutViewModel
    private var subscription: FlowSubscription?

    init(sessionId: String) {
        viewModel = KoinIosKt.logWorkoutViewModel(sessionId: sessionId)
        state = viewModel.uiState.value as? LogWorkoutUiState
        subscription = FlowObserverKt.subscribe(flow: viewModel.uiState) { [weak self] value in
            self?.state = value as? LogWorkoutUiState
        }
    }

    func addExercise(_ exerciseId: String) {
        viewModel.addExercise(exerciseId: exerciseId)
    }

    func addStrengthSet(_ loggedItemId: String, reps: Int32, loadKg: Double) {
        viewModel.addStrengthSet(loggedItemId: loggedItemId, reps: reps, loadKg: loadKg)
    }

    func addCardioSet(_ loggedItemId: String, timeSec: Int32, distanceM: Int32) {
        viewModel.addCardioSet(loggedItemId: loggedItemId, timeSec: timeSec, distanceM: distanceM)
    }

    deinit {
        subscription?.cancel()
    }
}
