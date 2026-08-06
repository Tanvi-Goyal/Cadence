import org.jetbrains.kotlin.gradle.dsl.JvmTarget

/* :feature:home — the home dashboard. Depends on domain only (Syncer via interface, not :core:sync). */
plugins {
    id("mindset.kmp.library")
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    androidLibrary {
        namespace = "com.mindset.feature.home"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
        compilerOptions {
            jvmTarget = JvmTarget.JVM_11
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.domain) // SessionRepository + Syncer + model (PlannedSession/Session)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.kotlinx.coroutines.core)
            implementation(project.dependencies.platform(libs.koin.bom))
            implementation(libs.koin.core)
            implementation(libs.koin.core.viewmodel)
            implementation(libs.compose.runtime)
        }
        androidMain.dependencies {
            implementation(projects.core.ui)          // SessionRow + shared session helpers + MindSetBottomBar
            implementation(projects.core.designsystem) // tokens, spacing, MindSetIcons, components, LocalWeightUnit
            implementation(projects.core.navigation)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.androidx.navigation.compose)
            implementation(libs.compose.uiToolingPreview)
            implementation(project.dependencies.platform(libs.koin.bom))
            implementation(libs.koin.compose.viewmodel)
        }
    }
}
