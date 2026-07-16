import Foundation
import Shared

/// Adapter around the shared `NewSessionViewModel`. `create` is fire-and-forget with a completion
/// callback carrying the new session id (fired on the main thread from `viewModelScope`).
final class NewSessionStore: ObservableObject {
    private let viewModel: NewSessionViewModel

    init() {
        viewModel = KoinIosKt.doNewSessionViewModel()
    }

    func create(type: String, onCreated: @escaping (String) -> Void) {
        viewModel.create(type: type, onCreated: onCreated)
    }
}
