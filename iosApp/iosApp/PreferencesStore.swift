import SwiftUI
import Shared

/// App-wide preferences, resolved once at the root and injected via `.environmentObject`. Observes
/// the shared `PreferencesViewModel` (same Flow bridge as every other screen) and forwards the two
/// intents. Exposes a SwiftUI `colorScheme` for the theme preference.
final class PreferencesStore: ObservableObject {
    @Published private(set) var units: WeightUnit = .kg
    @Published private(set) var themeMode: ThemeMode = .system

    private let viewModel: PreferencesViewModel
    private var subscription: FlowSubscription?

    init() {
        viewModel = KoinIosKt.preferencesViewModel()
        if let initial = viewModel.preferences.value as? UserPreferences {
            units = initial.weightUnit
            themeMode = initial.themeMode
        }
        subscription = FlowObserverKt.subscribe(flow: viewModel.preferences) { [weak self] value in
            guard let prefs = value as? UserPreferences else { return }
            self?.units = prefs.weightUnit
            self?.themeMode = prefs.themeMode
        }
    }

    /// nil = follow the system setting.
    var colorScheme: ColorScheme? {
        switch themeMode {
        case .light: return .light
        case .dark: return .dark
        default: return nil
        }
    }

    func setUnit(_ unit: WeightUnit) { viewModel.onWeightUnitChange(unit: unit) }
    func setTheme(_ mode: ThemeMode) { viewModel.onThemeModeChange(mode: mode) }

    deinit {
        subscription?.cancel()
    }
}
