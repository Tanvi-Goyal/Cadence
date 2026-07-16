import SwiftUI
import Shared

/// Log Workout — add exercises to a session and log sets under each, all through the shared
/// `LogWorkoutViewModel`. Writes go to the DB; the observed `uiState` reflects them back.
struct LogWorkoutView: View {
    private let onFinish: () -> Void
    @StateObject private var store: LogWorkoutStore
    @State private var showPicker = false

    init(sessionId: String, onFinish: @escaping () -> Void) {
        self.onFinish = onFinish
        _store = StateObject(wrappedValue: LogWorkoutStore(sessionId: sessionId))
    }

    var body: some View {
        List {
            if let state = store.state {
                if state.items.isEmpty {
                    Text("No exercises yet — tap Add exercise.")
                        .foregroundColor(.secondary)
                }
                ForEach(state.items, id: \.loggedItemId) { item in
                    Section(item.exerciseName) {
                        ForEach(item.sets, id: \.id) { set in
                            HStack {
                                Text("Set \(set.setNumber)")
                                Spacer()
                                Text(setDescription(set)).foregroundColor(.secondary)
                            }
                        }
                        AddSetRow(metric: item.metric) { reps, load in
                            store.addStrengthSet(item.loggedItemId, reps: reps, loadKg: load)
                        } onAddCardio: { time, distance in
                            store.addCardioSet(item.loggedItemId, timeSec: time, distanceM: distance)
                        }
                    }
                }
            }
        }
        .navigationTitle(store.state?.sessionName ?? "Log Workout")
        .navigationBarTitleDisplayMode(.inline)
        .toolbar {
            ToolbarItem(placement: .topBarLeading) {
                Button { showPicker = true } label: { Label("Add exercise", systemImage: "plus") }
            }
            ToolbarItem(placement: .topBarTrailing) {
                Button("Finish", action: onFinish)
            }
        }
        .sheet(isPresented: $showPicker) {
            ExercisePickerView { exerciseId in
                store.addExercise(exerciseId)
                showPicker = false
            }
        }
    }

    private func setDescription(_ set: SetEntry) -> String {
        if let time = set.timeSec?.intValue, let dist = set.distanceM?.intValue {
            return "\(time)s · \(dist) m"
        }
        let reps = set.reps?.intValue
        let load = set.loadKg?.doubleValue
        if let reps, let load { return "\(reps) reps × \(Int(load)) kg" }
        if let reps { return "\(reps) reps" }
        return ""
    }
}

/// Inline set entry — branches on the exercise's metric (strength: reps × kg, cardio: sec / m).
private struct AddSetRow: View {
    let metric: String
    let onAddStrength: (Int32, Double) -> Void
    let onAddCardio: (Int32, Int32) -> Void

    @State private var first = ""
    @State private var second = ""

    var body: some View {
        HStack {
            if metric == "TIME_DISTANCE" {  // ExerciseMetric.TIME_DISTANCE
                TextField("sec", text: $first).keyboardType(.numberPad)
                TextField("m", text: $second).keyboardType(.numberPad)
                Button("Add") {
                    onAddCardio(Int32(first) ?? 0, Int32(second) ?? 0)
                    first = ""; second = ""
                }
                .disabled(first.isEmpty)
            } else {
                TextField("reps", text: $first).keyboardType(.numberPad)
                TextField("kg", text: $second).keyboardType(.decimalPad)
                Button("Add") {
                    onAddStrength(Int32(first) ?? 0, Double(second) ?? 0)
                    first = ""; second = ""
                }
                .disabled(first.isEmpty)
            }
        }
        .textFieldStyle(.roundedBorder)
    }
}
