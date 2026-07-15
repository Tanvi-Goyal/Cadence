import Foundation
import Shared

/// Observable adapter around the shared `HistoryViewModel`. Resolves the ViewModel from Koin, then
/// bridges its `rows` StateFlow into a SwiftUI-observable `@Published` via the hand-rolled Kotlin
/// `subscribe` helper. Values arrive on the main thread (the bridge collects on `Dispatchers.Main`).
final class HistoryStore: ObservableObject {
    @Published private(set) var rows: [HistoryRow] = []

    private let viewModel: HistoryViewModel
    private var subscription: FlowSubscription?

    init() {
        viewModel = KoinIosKt.historyViewModel()
        subscription = FlowObserverKt.subscribe(flow: viewModel.rows) { [weak self] value in
            // Kotlin generics erase at the Obj-C boundary → the emitted List<HistoryRow> is Any?.
            self?.rows = (value as? [HistoryRow]) ?? []
        }
    }

    deinit {
        subscription?.cancel()
    }
}
