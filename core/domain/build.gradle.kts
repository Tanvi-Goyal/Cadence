import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("cadence.kmp.library")
}

kotlin {
    androidLibrary {
        namespace = "dev.cadence.domain"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
        compilerOptions {
            jvmTarget = JvmTarget.JVM_11
        }
        withHostTest {}
    }

    sourceSets {
        commonMain.dependencies {
            // `api`: repository interfaces + use cases expose domain model types to their consumers.
            api(projects.core.model)
            implementation(libs.kotlinx.coroutines.core) // Flow on the repo ports
            implementation(libs.androidx.paging.common)   // SessionRepository.searchExercises → PagingData
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}
