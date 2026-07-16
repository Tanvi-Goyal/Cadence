import SwiftUI
import Shared

/// Home screen — summary stats, the planned session, and recent sessions, all from the shared
/// `HomeViewModel.uiState`.
/// Destinations reachable from the Home logging flow.
enum LogRoute: Hashable {
    case newSession
    case logWorkout(String)
    case sessionDetail(String)
}

struct HomeView: View {
    @StateObject private var store = HomeStore()
    @EnvironmentObject private var prefs: PreferencesStore
    @State private var path: [LogRoute] = []

    var body: some View {
        NavigationStack(path: $path) {
            ScrollView {
                if let state = store.state {
                    VStack(alignment: .leading, spacing: 20) {
                        statsRow(state.stats)
                        if let plan = state.plannedSession {
                            plannedCard(plan)
                        }
                        recentSessions(state)
                    }
                    .padding()
                } else {
                    ProgressView().padding()
                }
            }
            .navigationTitle("Home")
            .toolbar {
                ToolbarItem(placement: .topBarLeading) {
                    Button { path.append(.newSession) } label: { Image(systemName: "plus") }
                }
                ToolbarItem(placement: .topBarTrailing) {
                    Button { store.sync() } label: { Image(systemName: "arrow.triangle.2.circlepath") }
                }
            }
            .navigationDestination(for: LogRoute.self) { route in
                switch route {
                case .newSession:
                    // Replace the stack so Back from Log Workout returns to Home, not New Session.
                    NewSessionView { sessionId in path = [.logWorkout(sessionId)] }
                case .logWorkout(let sessionId):
                    LogWorkoutView(sessionId: sessionId) { path = [] }
                case .sessionDetail(let sessionId):
                    SessionDetailView(sessionId: sessionId)
                }
            }
        }
    }

    private func statsRow(_ stats: HomeStats) -> some View {
        HStack(spacing: 12) {
            statTile("Sessions", "\(stats.total)")
            statTile("Day streak", "\(stats.dayStreak)")
            statTile("Volume", Format.volume(stats.totalVolumeKg, prefs.units))
        }
    }

    private func statTile(_ label: String, _ value: String) -> some View {
        VStack(spacing: 4) {
            Text(value).font(.title2.bold())
            Text(label).font(.caption).foregroundColor(.secondary)
        }
        .frame(maxWidth: .infinity)
        .padding(.vertical, 16)
        .background(Color(.secondarySystemBackground))
        .clipShape(RoundedRectangle(cornerRadius: 16))
    }

    private func plannedCard(_ plan: PlannedSession) -> some View {
        VStack(alignment: .leading, spacing: 4) {
            Text("Up next").font(.caption).foregroundColor(.secondary)
            Text(plan.name).font(.headline)
            Text("\(plan.focus) · \(plan.targetDurationMin) min")
                .font(.subheadline).foregroundColor(.secondary)
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding()
        .background(Color(.secondarySystemBackground))
        .clipShape(RoundedRectangle(cornerRadius: 16))
    }

    @ViewBuilder
    private func recentSessions(_ state: HomeUiState) -> some View {
        Text("Recent").font(.headline)
        if state.sessions.isEmpty {
            Text("No sessions logged yet.").foregroundColor(.secondary)
        } else {
            ForEach(state.sessions.prefix(10), id: \.id) { session in
                NavigationLink(value: LogRoute.sessionDetail(session.id)) {
                    HStack {
                        VStack(alignment: .leading, spacing: 2) {
                            Text(session.name).font(.body.weight(.medium)).foregroundStyle(.primary)
                            Text(Format.relativeDate(session.startedAt))
                                .font(.caption).foregroundColor(.secondary)
                        }
                        Spacer()
                        let volume = state.volumeBySession[session.id]?.doubleValue ?? 0
                        if volume > 0 {
                            Text(Format.volume(volume, prefs.units)).font(.subheadline).foregroundColor(.secondary)
                        }
                    }
                    .contentShape(Rectangle())
                }
                .buttonStyle(.plain)
                .padding(.vertical, 6)
                Divider()
            }
        }
    }
}
