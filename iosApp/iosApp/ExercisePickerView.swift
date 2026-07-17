import SwiftUI
import Shared

/// Searchable, filterable exercise picker, presented as a sheet from Log Workout. Tapping a row
/// opens the detail; adding from there calls back with the id (Log Workout adds it via the shared VM).
struct ExercisePickerView: View {
    let onPick: (String) -> Void

    @Environment(\.dismiss) private var dismiss
    @StateObject private var store = ExercisePickerStore()
    @State private var query = ""
    @State private var muscle: String?
    @State private var equipment: String?

    private var filtered: [Exercise] {
        store.exercises.filter { ex in
            (query.isEmpty || ex.keywords.localizedCaseInsensitiveContains(query)) &&
            (muscle == nil || ex.primaryMusclesList.contains(muscle!)) &&
            (equipment == nil || ex.equipment == equipment)
        }
    }

    var body: some View {
        NavigationStack {
            VStack(spacing: 8) {
                ChipRow(options: ExerciseFilters.shared.muscles, selected: $muscle)
                ChipRow(options: ExerciseFilters.shared.equipment, selected: $equipment)
                List(filtered, id: \.id) { exercise in
                    NavigationLink {
                        ExerciseDetailView(exercise: exercise) { onPick(exercise.id) }
                    } label: {
                        HStack(spacing: 12) {
                            AsyncImage(url: URL(string: exercise.imageUrlsList.first ?? "")) { image in
                                image.resizable().scaledToFill()
                            } placeholder: {
                                Color(.secondarySystemBackground)
                            }
                            .frame(width: 44, height: 44)
                            .clipShape(RoundedRectangle(cornerRadius: 8))

                            VStack(alignment: .leading, spacing: 2) {
                                Text(exercise.name)
                                Text(subtitle(exercise)).font(.caption).foregroundColor(.secondary)
                            }
                        }
                    }
                }
                .listStyle(.plain)
            }
            .searchable(text: $query)
            .navigationTitle("Add exercise")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .topBarTrailing) { Button("Cancel") { dismiss() } }
            }
        }
        .onAppear { store.load() }
    }

    private func subtitle(_ ex: Exercise) -> String {
        [ex.primaryMusclesList.first?.capitalized, ex.equipment]
            .compactMap { $0 }.joined(separator: " · ")
    }
}

/// Horizontally-scrolling single-select filter chips.
private struct ChipRow: View {
    let options: [String]
    @Binding var selected: String?

    var body: some View {
        ScrollView(.horizontal, showsIndicators: false) {
            HStack(spacing: 8) {
                ForEach(options, id: \.self) { option in
                    let isOn = option == selected
                    Text(option.capitalized)
                        .font(.footnote)
                        .fontWeight(isOn ? .semibold : .regular)
                        .padding(.horizontal, 12).padding(.vertical, 6)
                        .background(isOn ? Color.accentColor : Color(.secondarySystemBackground))
                        .foregroundColor(isOn ? .white : .primary)
                        .clipShape(Capsule())
                        .onTapGesture { selected = isOn ? nil : option }
                }
            }
            .padding(.horizontal)
        }
    }
}
