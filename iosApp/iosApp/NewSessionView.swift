import SwiftUI
import Shared

/// New Session — pick a session type; the shared VM creates it and hands back the id to navigate on.
struct NewSessionView: View {
    /// Called with the created session's id (to push Log Workout).
    let onCreated: (String) -> Void

    @StateObject private var store = NewSessionStore()

    private let types: [(id: String, label: String)] = [
        ("STRENGTH", "Strength"),
        ("CONDITIONING", "Conditioning"),
        ("HYROX", "Hyrox"),
        ("MIXED", "Mixed"),
    ]

    var body: some View {
        List {
            Section("Session type") {
                ForEach(types, id: \.id) { type in
                    Button(type.label) {
                        store.create(type: type.id, onCreated: onCreated)
                    }
                }
            }
        }
        .navigationTitle("New Session")
        .navigationBarTitleDisplayMode(.inline)
    }
}
