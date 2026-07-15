import SwiftUI
import Charts
import Shared

/// Stats screen — an exercise selector plus the selected exercise's volume-over-time trend, from
/// the shared `StatsViewModel`. The chart re-queries in shared code (flatMapLatest) on selection.
struct StatsView: View {
    @StateObject private var store = StatsStore()

    var body: some View {
        NavigationStack {
            VStack(alignment: .leading, spacing: 16) {
                if let state = store.state {
                    if state.exercises.isEmpty {
                        Spacer()
                        Text("No exercise history yet.")
                            .foregroundColor(.secondary)
                            .frame(maxWidth: .infinity)
                        Spacer()
                    } else {
                        exercisePicker(state)
                        chart(state)
                        Spacer()
                    }
                } else {
                    ProgressView()
                }
            }
            .padding()
            .navigationTitle("Stats")
        }
    }

    private func exercisePicker(_ state: StatsUiState) -> some View {
        Picker("Exercise", selection: Binding(
            get: { state.selectedExerciseId ?? state.exercises.first?.id ?? "" },
            set: { store.select($0) }
        )) {
            ForEach(state.exercises, id: \.id) { exercise in
                Text(exercise.name).tag(exercise.id)
            }
        }
        .pickerStyle(.menu)
    }

    @ViewBuilder
    private func chart(_ state: StatsUiState) -> some View {
        if state.points.isEmpty {
            Text("No data for this exercise yet.")
                .foregroundColor(.secondary)
                .frame(maxWidth: .infinity, minHeight: 220)
        } else {
            Chart(state.points, id: \.startedAt) { point in
                LineMark(
                    x: .value("Date", Date(timeIntervalSince1970: Double(point.startedAt) / 1000.0)),
                    y: .value("Volume (kg)", point.volume)
                )
                PointMark(
                    x: .value("Date", Date(timeIntervalSince1970: Double(point.startedAt) / 1000.0)),
                    y: .value("Volume (kg)", point.volume)
                )
            }
            .frame(height: 260)
        }
    }
}
