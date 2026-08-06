import org.jetbrains.kotlin.gradle.dsl.JvmTarget

/*
 * :core:designsystem — the "Obsidian Performance" theme (color/type/shape/spacing + Inter fonts) and the
 * reusable Compose primitives. Android-only (the iOS app is a SwiftUI shell), so this is a plain
 * com.android.library + Compose rather than a KMP module.
 */
plugins {
    // AGP is already on the shared plugin classpath (build-logic includes it), so apply by id without
    // a version. AGP 9 provides built-in Kotlin, so no kotlin-android plugin. Compose plugins aren't
    // on that classpath, so they use the catalog alias.
    id("com.android.library")
    alias(libs.plugins.composeMultiplatform) // repo's compose artifacts are org.jetbrains.compose.*
    alias(libs.plugins.composeCompiler)
}

android {
    namespace = "com.mindset.designsystem"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
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
    implementation(projects.core.domain) // Theme.kt provides ThemeMode / WeightUnit via CompositionLocals
    implementation(libs.compose.runtime)
    implementation(libs.compose.foundation)
    implementation(libs.compose.material3)
    implementation(libs.compose.ui)
}
