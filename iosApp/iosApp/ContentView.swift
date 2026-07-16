import SwiftUI
import Shared

struct ContentView: View {
    @State private var selection = 0

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
        }
    }
}
