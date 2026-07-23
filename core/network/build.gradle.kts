import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("cadence.kmp.library")
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    androidLibrary {
        namespace = "dev.cadence.network"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
        compilerOptions {
            jvmTarget = JvmTarget.JVM_11
        }
        withHostTest {}
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.contracts) // SyncApi speaks the wire DTOs
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.contentNegotiation)
            implementation(libs.ktor.serialization.kotlinxJson)
            // networkModule + networkPlatformModule (DI).
            implementation(project.dependencies.platform(libs.koin.bom))
            implementation(libs.koin.core)
        }
        androidMain.dependencies {
            implementation(libs.ktor.client.okhttp) // networkPlatformModule engine
        }
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin) // networkPlatformModule engine
        }
    }
}
