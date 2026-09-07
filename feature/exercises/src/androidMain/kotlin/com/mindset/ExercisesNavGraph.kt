package com.mindset

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute

/*
 * The shared exercise picker → detail flow. Both routes carry the [PickerTarget] so the detail screen
 * can hand its pick back to whichever screen launched it — but the actual back-stack write lives in
 * the app's NavHost (the feature only surfaces the target via the lambdas, staying decoupled).
 */

fun NavGraphBuilder.exercisePickerScreen(onOpenDetail: (exerciseId: String, target: PickerTarget) -> Unit, onBack: () -> Unit) {
    composable<ExercisePicker> { entry ->
        val target = entry.toRoute<ExercisePicker>().target
        ExercisePickerScreen(
            onOpenDetail = { exerciseId -> onOpenDetail(exerciseId, target) },
            onBack = onBack,
        )
    }
}

fun NavGraphBuilder.exerciseDetailScreen(onAdd: (exerciseId: String, target: PickerTarget) -> Unit, onBack: () -> Unit) {
    composable<ExerciseDetail> { entry ->
        val route = entry.toRoute<ExerciseDetail>()
        ExerciseDetailScreen(
            exerciseId = route.exerciseId,
            onAdd = { onAdd(route.exerciseId, route.target) },
            onBack = onBack,
        )
    }
}
