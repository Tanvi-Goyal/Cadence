import SwiftUI
import Shared

struct ContentView: View {
    var body: some View {
        VStack(spacing: 12) {
            Text("Cadence")
                .font(.largeTitle.bold())
            Text("Shared KMP module loaded.\niOS Home screen lands in Phase 4.")
                .multilineTextAlignment(.center)
                .foregroundColor(.secondary)
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .padding()
    }
}

struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        ContentView()
    }
}