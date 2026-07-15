import SwiftUI
import Shared

/// Read-only detail of a past session — its logged exercises and sets, from the shared
/// `SessionDetailViewModel`. Pushed from History (and Home) with a `sessionId`.
struct SessionDetailView: View {
    private let sessionId: String
    @StateObject private var store: SessionDetailStore

    init(sessionId: String) {
        self.sessionId = sessionId
        _store = StateObject(wrappedValue: SessionDetailStore(sessionId: sessionId))
    }

    var body: some View {
        List {
            if let state = store.state {
                Section {
                    VStack(alignment: .leading, spacing: 4) {
                        Text(Format.relativeDate(state.startedAt))
                            .font(.subheadline).foregroundColor(.secondary)
                        if state.totalVolumeKg > 0 {
                            Text("Total volume: \(Format.volume(state.totalVolumeKg))")
                                .font(.subheadline).foregroundColor(.secondary)
                        }
                    }
                }
                ForEach(state.items, id: \.loggedItemId) { item in
                    Section(item.exerciseName) {
                        ForEach(item.sets, id: \.id) { set in
                            HStack {
                                Text("Set \(set.setNumber)")
                                Spacer()
                                Text(Self.setDescription(set)).foregroundColor(.secondary)
                            }
                        }
                    }
                }
            }
        }
        .navigationTitle(store.state?.name ?? "Session")
        .navigationBarTitleDisplayMode(.inline)
    }

    private static func setDescription(_ set: SetEntry) -> String {
        let reps = set.reps?.intValue
        let load = set.loadKg?.doubleValue
        if let reps, let load {
            return "\(reps) reps × \(Int(load)) kg"
        }
        if let reps {
            return "\(reps) reps"
        }
        return ""
    }
}
