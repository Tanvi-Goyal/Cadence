import org.jetbrains.kotlin.gradle.dsl.JvmTarget

/*
 * :core:ui — shared UI building blocks that compose from the design system but aren't themselves
 * tokens (currently the bottom navigation bar). Android-only Compose, same shape as :core:designsystem.
 */
plugins {
    // AGP is already on the shared plugin classpath (build-logic), so apply by id without a version.
    // AGP 9 provides built-in Kotlin. Compose plugins come from the catalog.
    id("com.android.library")
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

android {
    namespace = "com.mindset.ui"
    compileSdk =
        libs.versions.android.compileSdk
            .get()
            .toInt()

    defaultConfig {
        minSdk =
            libs.versions.android.minSdk
                .get()
                .toInt()
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_11
    }
}

dependencies {
    implementation(projects.core.designsystem) // MindSetTheme + MaterialTheme.spacing tokens
    implementation(projects.core.model) // Session / SessionType for shared session UI
    implementation(projects.core.domain) // Units / WeightUnit formatting
    implementation(libs.compose.runtime)
    implementation(libs.compose.foundation)
    implementation(libs.compose.material3)
    implementation(libs.compose.ui)
    implementation(libs.compose.uiToolingPreview)
    // MindSetTopBar hosts the minimized live-race pill: it observes the app-scoped
    // ActiveWorkoutController (:core:domain) directly instead of every screen threading it down.
    implementation(libs.androidx.lifecycle.runtimeCompose)
    implementation(project.dependencies.platform(libs.koin.bom))
    implementation(libs.koin.compose)
}
