package dev.cadence

import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute

fun NavGraphBuilder.templatesScreen(
    onBack: () -> Unit,
    onNewTemplate: () -> Unit,
    onEditTemplate: (String) -> Unit,
    onStarted: (String) -> Unit,
) {
    composable<Templates> {
        TemplatesScreen(
            onBack = onBack,
            onNewTemplate = onNewTemplate,
            onEditTemplate = onEditTemplate,
            onStarted = onStarted,
        )
    }
}

fun NavGraphBuilder.newTemplateScreen(
    onBack: () -> Unit,
    onCreated: (String) -> Unit,
) {
    composable<NewTemplate> {
        NewTemplateScreen(onBack = onBack, onCreated = onCreated)
    }
}

/** The template builder. Same picker→origin savedStateHandle handoff as Log Workout. */
fun NavGraphBuilder.templateBuilderScreen(
    onBack: () -> Unit,
    onAddExercise: () -> Unit,
    onDone: () -> Unit,
) {
    composable<TemplateBuilder> { entry ->
        val templateId = entry.toRoute<TemplateBuilder>().templateId
        val picked by entry.savedStateHandle
            .getStateFlow<String?>(PICKED_EXERCISE, null)
            .collectAsStateWithLifecycle()
        TemplateBuilderScreen(
            templateId = templateId,
            pickedExerciseId = picked,
            onExerciseConsumed = { entry.savedStateHandle[PICKED_EXERCISE] = null },
            onBack = onBack,
            onAddExercise = onAddExercise,
            onDone = onDone,
        )
    }
}
