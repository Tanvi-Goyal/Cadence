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
    @Suppress("UNUSED_PARAMETER") onStarted: (String) -> Unit,
) {
    // The Templates route now renders the redesigned Template Library (Figma 33:1068). Its first pass
    // opens a tapped template in the builder (onEditTemplate); the legacy start flow (onStarted) is
    // reintroduced once cards carry real DB template ids — see TemplatesScreen.kt (kept until parity).
    composable<Templates> {
        TemplateLibraryScreen(
            onBack = onBack,
            onNewTemplate = onNewTemplate,
            onOpenTemplate = onEditTemplate,
        )
    }
}

/** Template Detail (Figma 33:1455). Opened from a Library card; START WORKOUT and the more-menu edit
 *  route out via callbacks. Carries the tapped template id for [onEdit] (real per-template content +
 *  the instantiate-on-start flow land with the data pass). */
fun NavGraphBuilder.templateDetailScreen(
    onBack: () -> Unit,
    onStart: () -> Unit,
    onEdit: (String) -> Unit,
) {
    composable<TemplateDetail> { entry ->
        val templateId = entry.toRoute<TemplateDetail>().templateId
        TemplateDetailScreen(
            onBack = onBack,
            onStart = onStart,
            onEdit = { onEdit(templateId) },
        )
    }
}

/** Template Detail — Hyrox sim (Figma 33:1727). The station-list detail variant, shared by the full
 *  and half simulations; the VM seeds its content from the template id. */
fun NavGraphBuilder.templateHyroxDetailScreen(
    onBack: () -> Unit,
    onEdit: (String) -> Unit,
) {
    composable<TemplateHyroxDetail> { entry ->
        val templateId = entry.toRoute<TemplateHyroxDetail>().templateId
        TemplateHyroxDetailScreen(
            templateId = templateId,
            onBack = onBack,
            onEdit = { onEdit(templateId) },
        )
    }
}

/** Template Detail — Strength (Push / Pull / Lower Body). The exercise-list detail variant; the VM
 *  seeds its content from the template id. */
fun NavGraphBuilder.templateStrengthDetailScreen(
    onBack: () -> Unit,
    onStarted: (String) -> Unit,
    onEdit: (String) -> Unit,
) {
    composable<TemplateStrengthDetail> { entry ->
        val templateId = entry.toRoute<TemplateStrengthDetail>().templateId
        TemplateStrengthDetailScreen(
            templateId = templateId,
            onBack = onBack,
            onStarted = onStarted,
            onEdit = { onEdit(templateId) },
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
