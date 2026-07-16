import SwiftUI
import Shared

@main
struct iOSApp: App {
    init() {
        // Start the shared Koin graph before any screen resolves dependencies.
        // No Context needed on iOS — the DB builder uses a Documents-directory path.
        KoinIosKt.doInitKoin()
        // Dev-only: seed a few sessions so History shows real data (no create screen on iOS yet).
        KoinIosKt.seedDemoData()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}