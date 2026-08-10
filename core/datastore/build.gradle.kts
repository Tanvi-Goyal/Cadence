import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("mindset.kmp.library")
}

kotlin {
    androidLibrary {
        namespace = "com.mindset.datastore"
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
            implementation(libs.androidx.datastore.preferences.core) // Preferences DataStore (KMP)
            implementation(libs.kotlinx.coroutines.core)
            // dataStorePlatformModule (DI): platform-supplied DataStore<Preferences>.
            implementation(project.dependencies.platform(libs.koin.bom))
            implementation(libs.koin.core)
        }
        androidMain.dependencies {
            implementation(libs.koin.android) // dataStorePlatformModule uses androidContext()
        }
    }
}
