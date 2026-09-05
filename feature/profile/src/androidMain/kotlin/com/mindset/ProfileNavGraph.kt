package com.mindset

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.mindset.model.BottomNavTab

/*
 * Navigation entry points the profile feature contributes to the app graph. The app supplies the
 * nav actions as lambdas (features stay decoupled from each other and from the NavHost).
 */

/** The Profile tab (string-routed, like the other bottom-nav tabs). */
fun NavGraphBuilder.profileScreen(onTab: (BottomNavTab) -> Unit) {
    composable<Profile> {
        ProfileScreen(onTab = onTab)
    }
}

/**
 * The Credits push (typed route). Currently registered but unreachable: the Profile footer that
 * linked here was removed with the screen's placeholder content, and nothing else navigates to it.
 * Left in place rather than deleted — giving Credits a home again is its own decision.
 */
fun NavGraphBuilder.creditsScreen(onBack: () -> Unit) {
    composable<Credits> {
        CreditsScreen(onBack = onBack)
    }
}
