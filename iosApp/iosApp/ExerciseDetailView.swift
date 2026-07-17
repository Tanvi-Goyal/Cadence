import SwiftUI
import Shared

/// Full exercise detail — image, tags, muscles, and step-by-step instructions — with an
/// "Add to workout" action. Reached from the picker; the exercise is passed in directly.
struct ExerciseDetailView: View {
    let exercise: Exercise
    let onAdd: () -> Void

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 16) {
                if let url = exercise.imageUrlsList.first {
                    AsyncImage(url: URL(string: url)) { image in
                        image.resizable().scaledToFill()
                    } placeholder: {
                        Color(.secondarySystemBackground)
                    }
                    .frame(maxWidth: .infinity)
                    .frame(height: 220)
                    .clipShape(RoundedRectangle(cornerRadius: 16))
                }

                FlowChips(
                    tags: [exercise.level, exercise.equipment, exercise.mechanic, exercise.force]
                        .compactMap { $0?.capitalized },
                    filled: false,
                )
                FlowChips(tags: exercise.primaryMusclesList.map { $0.capitalized }, filled: true)

                if let muscle = exercise.primaryMusclesList.first {
                    MuscleDiagramView(muscleName: muscle)
                }

                if !exercise.instructionsList.isEmpty {
                    Text("Instructions").font(.headline)
                    ForEach(Array(exercise.instructionsList.enumerated()), id: \.offset) { i, step in
                        HStack(alignment: .top, spacing: 8) {
                            Text("\(i + 1).").fontWeight(.bold).foregroundColor(.accentColor)
                            Text(step).foregroundColor(.secondary)
                        }
                    }
                }
            }
            .padding()
        }
        .navigationTitle(exercise.name)
        .navigationBarTitleDisplayMode(.inline)
        .safeAreaInset(edge: .bottom) {
            Button(action: onAdd) {
                Text("Add to workout").fontWeight(.semibold).frame(maxWidth: .infinity)
            }
            .buttonStyle(.borderedProminent)
            .controlSize(.large)
            .padding()
        }
    }
}

/// Simple wrapping row of pill tags (uses a horizontal scroll to stay dependency-free).
private struct FlowChips: View {
    let tags: [String]
    let filled: Bool

    var body: some View {
        ScrollView(.horizontal, showsIndicators: false) {
            HStack(spacing: 8) {
                ForEach(tags, id: \.self) { tag in
                    Text(tag)
                        .font(.footnote).fontWeight(filled ? .semibold : .regular)
                        .padding(.horizontal, 12).padding(.vertical, 6)
                        .background(filled ? Color.accentColor : Color(.secondarySystemBackground))
                        .foregroundColor(filled ? .white : .primary)
                        .clipShape(Capsule())
                }
            }
        }
    }
}
