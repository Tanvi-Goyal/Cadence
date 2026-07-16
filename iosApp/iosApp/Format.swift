import Foundation
import Shared

/// Small display formatters shared across screens. Weights are stored/computed in kg; conversion to
/// the user's `WeightUnit` (and the label) reuses the shared `Units` object so the math lives once.
enum Format {
    static func relativeDate(_ epochMillis: Int64) -> String {
        let date = Date(timeIntervalSince1970: Double(epochMillis) / 1000.0)
        let formatter = RelativeDateTimeFormatter()
        formatter.unitsStyle = .full
        return formatter.localizedString(for: date, relativeTo: Date())
    }

    /// Compact volume in the user's unit, e.g. "12.4k lb".
    static func volume(_ kg: Double, _ unit: WeightUnit) -> String {
        let v = Units.shared.toDisplay(kg: kg, unit: unit)
        let label = Units.shared.label(unit: unit)
        return v >= 1000 ? String(format: "%.1fk %@", v / 1000, label) : String(format: "%.0f %@", v, label)
    }

    /// Plain weight (no compaction) for a single load, e.g. "40 kg" / "88 lb".
    static func weight(_ kg: Double, _ unit: WeightUnit) -> String {
        let v = Units.shared.toDisplay(kg: kg, unit: unit)
        return String(format: "%.0f %@", v, Units.shared.label(unit: unit))
    }
}
