import Foundation
import Shared

/// Loads the exercise catalog for the picker. The shared `ExerciseLibraryViewModel` is Paging-based
/// (`Flow<PagingData<Exercise>>`), which has no Swift consumer — so we read the bounded catalog via
/// the shared repository (`KoinIosKt.loadExercises`) and filter client-side.
final class ExercisePickerStore: ObservableObject {
    @Published private(set) var exercises: [Exercise] = []

    private var loader: FlowSubscription?

    func load() {
        loader = KoinIosKt.loadExercises { [weak self] list in
            self?.exercises = (list as? [Exercise]) ?? []
        }
    }

    deinit {
        loader?.cancel()
    }
}
