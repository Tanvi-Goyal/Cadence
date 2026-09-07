import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("mindset.kmp.library")
}

kotlin {
    androidLibrary {
        namespace = "com.mindset.common"
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
        withHostTest {}
    }

    sourceSets {
        commonMain.dependencies {
            // FlowObserver (iosMain) bridges Kotlin Flow → Swift; UuidV7Generator uses kotlin.time.
            implementation(libs.kotlinx.coroutines.core)
            // commonModule (DI): Clock + UuidGenerator bindings.
            implementation(project.dependencies.platform(libs.koin.bom))
            implementation(libs.koin.core)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}
