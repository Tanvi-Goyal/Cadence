import org.jetbrains.kotlin.gradle.dsl.JvmTarget

/*
 * :feature:auth — the login / sign-in feature. Design-static for now (no auth backend); the Compose
 * screen + its NavGraphBuilder extension live in androidMain. No ViewModel yet, so no commonMain
 * logic — commonMain carries only the Compose runtime the multiplatform compiler needs on every
 * target. Firebase Auth + real state land in a later phase.
 */
plugins {
    id("mindset.kmp.library")
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    androidLibrary {
        namespace = "com.mindset.feature.auth"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
        compilerOptions {
            jvmTarget = JvmTarget.JVM_11
        }
    }

    sourceSets {
        commonMain.dependencies {
            // The Compose compiler plugin runs on the iOS compilations too and needs the runtime on
            // every target's classpath, even though only androidMain has @Composable screens.
            implementation(libs.compose.runtime)
        }
        androidMain.dependencies {
            implementation(projects.core.ui)           // MindSetIcons + icon extensions
            implementation(projects.core.designsystem)  // MindSetTheme + color tokens
            implementation(projects.core.navigation)    // Login typed route
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.androidx.navigation.compose) // NavGraphBuilder / composable
        }
    }
}
