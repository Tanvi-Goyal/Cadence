import SwiftUI

/// Open-data attribution for the exercise content (CC-BY-SA requires crediting wger).
struct CreditsView: View {
    var body: some View {
        List {
            Section("Exercise data & photos") {
                Text("free-exercise-db").fontWeight(.semibold)
                Text("Public domain (Unlicense)")
                Text("github.com/yuhonas/free-exercise-db").foregroundColor(.accentColor).font(.footnote)
            }
            Section("Muscle diagrams") {
                Text("wger contributors").fontWeight(.semibold)
                Text("CC-BY-SA 4.0 / 3.0")
                Text("wger.de").foregroundColor(.accentColor).font(.footnote)
            }
        }
        .navigationTitle("Open data & credits")
        .navigationBarTitleDisplayMode(.inline)
    }
}
