import Foundation
import Shared

/// Observable adapter around the shared `HomeViewModel`. Same recipe as `HistoryStore`: resolve the
/// VM from Koin, bridge its `uiState` StateFlow into `@Published`, expose intents as methods.
final class HomeStore: ObservableObject {
    @Published private(set) var state: HomeUiState?

    private let viewModel: HomeViewModel
    private var subscription: FlowSubscription?

    init() {
        viewModel = KoinIosKt.homeViewModel()
        state = viewModel.uiState.value as? HomeUiState
        subscription = FlowObserverKt.subscribe(flow: viewModel.uiState) { [weak self] value in
            self?.state = value as? HomeUiState
        }
    }

    func sync() {
        viewModel.onSyncClick()
    }

    deinit {
        subscription?.cancel()
    }
}
