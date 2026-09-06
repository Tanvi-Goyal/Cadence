package com.mindset.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mindset.MindSetIcons
import com.mindset.MindSetWordmark
import com.mindset.domain.ActiveWorkoutController
import com.mindset.icons.ArrowBack
import com.mindset.icons.NavAccount
import org.koin.compose.koinInject

@Composable
fun MindSetTopBar(onProfileClick: () -> Unit, modifier: Modifier = Modifier, onBack: (() -> Unit)? = null) {
    val colors = MaterialTheme.colorScheme
    TopAppBar(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.surface),
        title = {
            MindSetWordmark()
        },
        navigationIcon = {
            if (onBack != null) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = MindSetIcons.ArrowBack,
                        contentDescription = "Back",
                        tint = colors.onSurface,
                    )
                }
            }
        },
        actions = {
            // A minimized race lives HERE rather than floating bottom-right, where it sat on top of
            // the bottom tabs. See [LiveWorkoutPillSlot] for how the app-root fallback stays out of
            // the way while this toolbar is on screen.
            MinimizedWorkoutAction()
            IconButton(
                onClick = onProfileClick,
            ) {
                Icon(
                    imageVector = NavAccount,
                    contentDescription = "Profile",
                    tint = colors.onSurface,
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = colors.surface,
            scrolledContainerColor = colors.surface,
        ),
    )
}

/**
 * The toolbar's live-race slot. Injects the app-scoped [ActiveWorkoutController] directly rather than
 * threading it through every screen's parameters — it is a singleton with no screen-scoped state, and
 * four call sites passing the same instance down would be four places to forget it.
 *
 * Recomposition: this collects the ~5 Hz controller flow, so the pill's clock re-reads on every tick.
 * It is a leaf of the toolbar's `actions` row — the wordmark, back button and profile icon are
 * siblings, not parents of the changing state, so none of them are invalidated by a tick.
 */
@Composable
private fun MinimizedWorkoutAction(controller: ActiveWorkoutController = koinInject()) {
    DisposableEffect(Unit) {
        LiveWorkoutPillSlot.claim()
        onDispose { LiveWorkoutPillSlot.release() }
    }

    val workout by controller.state.collectAsStateWithLifecycle()
    val expanded by controller.expanded.collectAsStateWithLifecycle()
    val live = workout ?: return
    if (expanded) return

    LiveWorkoutPill(workout = live, onClick = controller::expand)
}
