package com.mindset

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.mindset.model.BottomNavTab

fun NavGraphBuilder.profileScreen(onTab: (BottomNavTab) -> Unit) {
    composable<Profile> {
        ProfileScreen(onTab = onTab)
    }
}

fun NavGraphBuilder.creditsScreen(onBack: () -> Unit) {
    composable<Credits> {
        CreditsScreen(onBack = onBack)
    }
}
