import org.jetbrains.kotlin.gradle.dsl.JvmTarget

/* :feature:exercises — the exercise library picker + detail (the shared picker→origin flow). */
plugins {
    id("mindset.kmp.library")
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    androidLibrary {
        namespace = "com.mindset.feature.exercises"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
        compilerOptions {
            jvmTarget = JvmTarget.JVM_11
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.domain) // SessionRepository + MuscleImageProvider + model.Exercise/ExerciseFilters
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.paging.common) // ExerciseLibraryViewModel: PagingData / cachedIn
            implementation(libs.kotlinx.coroutines.core)
            implementation(project.dependencies.platform(libs.koin.bom))
            implementation(libs.koin.core)
            implementation(libs.koin.core.viewmodel)
            implementation(libs.compose.runtime)
        }
        androidMain.dependencies {
            implementation(projects.core.ui)
            implementation(projects.core.designsystem)
            implementation(projects.core.navigation) // ExercisePicker / ExerciseDetail typed routes
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.androidx.navigation.compose)
            implementation(libs.androidx.paging.compose) // collectAsLazyPagingItems
            implementation(libs.coil.compose) // lazy-loaded exercise images
            implementation(libs.coil.network.okhttp)
            implementation(libs.coil.svg) // wger muscle diagrams are SVG
            implementation(project.dependencies.platform(libs.koin.bom))
            implementation(libs.koin.compose.viewmodel)
        }
    }
}
