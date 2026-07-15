import Foundation
import Shared

/// Observable adapter around the parameterized shared `SessionDetailViewModel` (resolved with a
/// `sessionId` via Koin `parametersOf`).
final class SessionDetailStore: ObservableObject {
    @Published private(set) var state: SessionDetailUiState?

    private let viewModel: SessionDetailViewModel
    private var subscription: FlowSubscription?

    init(sessionId: String) {
        viewModel = KoinIosKt.sessionDetailViewModel(sessionId: sessionId)
        state = viewModel.uiState.value as? SessionDetailUiState
        subscription = FlowObserverKt.subscribe(flow: viewModel.uiState) { [weak self] value in
            self?.state = value as? SessionDetailUiState
        }
    }

    deinit {
        subscription?.cancel()
    }
}
