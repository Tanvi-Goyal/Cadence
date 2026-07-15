import SwiftUI
import Shared

/// The History screen — the first real iOS screen driven entirely by shared code. Every row here
/// comes from `HistoryViewModel.rows` (shared Kotlin), observed via `HistoryStore`.
struct HistoryView: View {
    @StateObject private var store = HistoryStore()

    var body: some View {
        NavigationStack {
            List(store.rows, id: \.session.id) { row in
                VStack(alignment: .leading, spacing: 4) {
                    Text(row.session.name)
                        .font(.headline)
                    HStack {
                        Text(Self.relativeDate(row.session.startedAt))
                            .font(.subheadline)
                            .foregroundColor(.secondary)
                        Spacer()
                        if row.volumeKg > 0 {
                            Text(Self.formatVolume(row.volumeKg))
                                .font(.subheadline)
                                .foregroundColor(.secondary)
                        }
                    }
                }
                .padding(.vertical, 4)
            }
            .navigationTitle("History")
            .overlay {
                if store.rows.isEmpty {
                    Text("No sessions logged yet.")
                        .foregroundColor(.secondary)
                }
            }
        }
    }

    /// `session.startedAt` is epoch millis (Kotlin `Long` → Swift `Int64`).
    private static func relativeDate(_ epochMillis: Int64) -> String {
        let date = Date(timeIntervalSince1970: Double(epochMillis) / 1000.0)
        let formatter = RelativeDateTimeFormatter()
        formatter.unitsStyle = .full
        return formatter.localizedString(for: date, relativeTo: Date())
    }

    private static func formatVolume(_ kg: Double) -> String {
        kg >= 1000 ? String(format: "%.1fk kg", kg / 1000) : String(format: "%.0f kg", kg)
    }
}
