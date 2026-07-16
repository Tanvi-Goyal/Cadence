import Foundation

/// Small display formatters shared across screens. Kotlin exposes timestamps as epoch millis
/// (`Long` → `Int64`) and volumes as `Double`; these mirror the Android UI's presentation.
enum Format {
    static func relativeDate(_ epochMillis: Int64) -> String {
        let date = Date(timeIntervalSince1970: Double(epochMillis) / 1000.0)
        let formatter = RelativeDateTimeFormatter()
        formatter.unitsStyle = .full
        return formatter.localizedString(for: date, relativeTo: Date())
    }

    static func volume(_ kg: Double) -> String {
        kg >= 1000 ? String(format: "%.1fk kg", kg / 1000) : String(format: "%.0f kg", kg)
    }
}
