import SwiftUI
import Shared

@main
struct iOSApp: App {
    init() {
        // Start the shared Koin graph before any screen resolves dependencies.
        // No Context needed on iOS — the DB builder uses a Documents-directory path.
        KoinIosKt.doInitKoin()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}