import SwiftUI
import Shared

struct ContentView: View {
    @State private var selection = 0
    // One preferences store for the whole app; injected so every screen reads the current unit,
    // and the root drives the color scheme from the theme preference.
    @StateObject private var prefs = PreferencesStore()

    var body: some View {
        TabView(selection: $selection) {
            HomeView()
                .tabItem { Label("Home", systemImage: "house") }
                .tag(0)
            HistoryView()
                .tabItem { Label("History", systemImage: "clock") }
                .tag(1)
            StatsView()
                .tabItem { Label("Stats", systemImage: "chart.line.uptrend.xyaxis") }
                .tag(2)
            ProfileView()
                .tabItem { Label("Profile", systemImage: "person") }
                .tag(3)
        }
        .environmentObject(prefs)
        .preferredColorScheme(prefs.colorScheme)
    }
}
