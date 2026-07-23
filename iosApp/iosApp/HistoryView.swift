import SwiftUI
import Shared

/// The History screen — the first real iOS screen driven entirely by shared code. Every row here
/// comes from `HistoryViewModel.rows` (shared Kotlin), observed via `HistoryStore`.
struct HistoryView: View {
    @StateObject private var store = HistoryStore()
    @EnvironmentObject private var prefs: PreferencesStore

    var body: some View {
        NavigationStack {
            List(store.rows, id: \.session.id) { row in
                NavigationLink(value: row.session.id) {
                    VStack(alignment: .leading, spacing: 4) {
                        Text(row.session.name)
                            .font(.headline)
                        HStack {
                            Text(Format.relativeDate(row.session.startedAt.toEpochMilliseconds()))
                                .font(.subheadline)
                                .foregroundColor(.secondary)
                            Spacer()
                            if row.volumeKg > 0 {
                                Text(Format.volume(row.volumeKg, prefs.units))
                                    .font(.subheadline)
                                    .foregroundColor(.secondary)
                            }
                        }
                    }
                }
            }
            .navigationTitle("History")
            .navigationDestination(for: String.self) { sessionId in
                SessionDetailView(sessionId: sessionId)
            }
            .overlay {
                if store.rows.isEmpty {
                    Text("No sessions logged yet.")
                        .foregroundColor(.secondary)
                }
            }
        }
    }
}
