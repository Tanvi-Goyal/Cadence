package com.mindset

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable

/*
 * Navigation entry points the profile feature contributes to the app graph. The app supplies the
 * nav actions as lambdas (features stay decoupled from each other and from the NavHost).
 */

/** The Profile tab (string-routed, like the other bottom-nav tabs). */
fun NavGraphBuilder.profileScreen(
    onTab: (Tab) -> Unit,
    onOpenCredits: () -> Unit,
) {
    composable<Profile> {
        ProfileScreen(onTab = onTab, onOpenCredits = onOpenCredits)
    }
}

/** The Credits push (typed route). */
fun NavGraphBuilder.creditsScreen(onBack: () -> Unit) {
    composable<Credits> {
        CreditsScreen(onBack = onBack)
    }
}
