import SwiftUI
import Shared

/// Searchable exercise picker, presented as a sheet from Log Workout. Picking an exercise calls
/// back with its id (Log Workout then adds it to the session via the shared VM).
struct ExercisePickerView: View {
    let onPick: (String) -> Void

    @Environment(\.dismiss) private var dismiss
    @StateObject private var store = ExercisePickerStore()
    @State private var query = ""

    private var filtered: [Exercise] {
        query.isEmpty
            ? store.exercises
            : store.exercises.filter { $0.name.localizedCaseInsensitiveContains(query) }
    }

    var body: some View {
        NavigationStack {
            List(filtered, id: \.id) { exercise in
                Button {
                    onPick(exercise.id)
                } label: {
                    VStack(alignment: .leading, spacing: 2) {
                        Text(exercise.name).foregroundStyle(.primary)
                        Text(exercise.category).font(.caption).foregroundColor(.secondary)
                    }
                }
            }
            .searchable(text: $query)
            .navigationTitle("Add exercise")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .topBarTrailing) {
                    Button("Cancel") { dismiss() }
                }
            }
        }
        .onAppear { store.load() }
    }
}
