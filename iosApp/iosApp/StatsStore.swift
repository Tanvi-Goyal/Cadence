import Foundation
import Shared

/// Observable adapter around the shared `StatsViewModel`. Bridges `uiState` and forwards the
/// exercise-selection intent.
final class StatsStore: ObservableObject {
    @Published private(set) var state: StatsUiState?

    private let viewModel: StatsViewModel
    private var subscription: FlowSubscription?

    init() {
        viewModel = KoinIosKt.statsViewModel()
        state = viewModel.uiState.value as? StatsUiState
        subscription = FlowObserverKt.subscribe(flow: viewModel.uiState) { [weak self] value in
            self?.state = value as? StatsUiState
        }
    }

    func select(_ exerciseId: String) {
        viewModel.onSelectExercise(exerciseId: exerciseId)
    }

    deinit {
        subscription?.cancel()
    }
}
