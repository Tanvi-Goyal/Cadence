import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("mindset.kmp.library")
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    androidLibrary {
        namespace = "com.mindset.feature.onboarding"
        compileSdk =
            libs.versions.android.compileSdk
                .get()
                .toInt()
        minSdk =
            libs.versions.android.minSdk
                .get()
                .toInt()
        compilerOptions {
            jvmTarget = JvmTarget.JVM_11
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.domain)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.kotlinx.coroutines.core)
            implementation(project.dependencies.platform(libs.koin.bom))
            implementation(libs.koin.core)
            implementation(libs.koin.core.viewmodel)
            implementation(libs.compose.runtime) // required on iOS compilations too
        }
        androidMain.dependencies {
            implementation(projects.core.ui) // BrandLockup, MindSetIcons
            implementation(projects.core.designsystem) // MindSetTheme + tokens
            implementation(projects.core.navigation) // typed routes
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.androidx.lifecycle.runtimeCompose) // collectAsStateWithLifecycle
            implementation(libs.androidx.navigation.compose) // NavGraphBuilder / composable
            implementation(project.dependencies.platform(libs.koin.bom))
            implementation(libs.koin.compose.viewmodel) // koinViewModel()
        }
    }
}
