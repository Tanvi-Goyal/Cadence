import org.jetbrains.kotlin.gradle.dsl.JvmTarget

/* :feature:history — session history list + session detail. */
plugins {
    id("mindset.kmp.library")
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    androidLibrary {
        namespace = "com.mindset.feature.history"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
        compilerOptions {
            jvmTarget = JvmTarget.JVM_11
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.domain) // SessionRepository + LoggedItemUi/toLoggedItemUis + model
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.paging.common) // HistoryViewModel: PagingData / cachedIn
            implementation(libs.kotlinx.coroutines.core)
            implementation(project.dependencies.platform(libs.koin.bom))
            implementation(libs.koin.core)
            implementation(libs.koin.core.viewmodel)
            implementation(libs.compose.runtime)
        }
        androidMain.dependencies {
            implementation(projects.core.ui)          // SessionRow / formatVolume / relativeDate / MindSetBottomBar
            implementation(projects.core.designsystem)
            implementation(projects.core.navigation)   // SessionDetail typed route
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.androidx.navigation.compose)
            implementation(libs.androidx.paging.compose)   // collectAsLazyPagingItems (Pro list)
            implementation(project.dependencies.platform(libs.koin.bom))
            implementation(libs.koin.compose.viewmodel)
        }
    }
}
