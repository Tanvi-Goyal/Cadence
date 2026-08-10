package com.mindset

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable

/*
 * Navigation entry point the auth feature contributes to the app graph. The app supplies the nav
 * actions as lambdas (the feature stays decoupled from the NavHost). Login is the app's start
 * destination; any sign-in action routes onward. No real auth yet — see LoginScreen.
 */
fun NavGraphBuilder.loginScreen(onSignedIn: () -> Unit, onCreateAccount: () -> Unit, onForgotPassword: () -> Unit) {
    composable<Login> {
        LoginScreen(
            onSignedIn = onSignedIn,
            onCreateAccount = onCreateAccount,
            onForgotPassword = onForgotPassword,
        )
    }
}
