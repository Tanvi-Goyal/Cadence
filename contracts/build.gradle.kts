import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("cadence.kmp.library")
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    // jvm() lets the JVM :server consume these DTOs; :shared consumes the android/ios variants.
    // The KMP + AGP-KMP-library plugins, iOS targets, and -Xexpect-actual-classes come from the
    // `cadence.kmp.library` convention plugin.
    jvm()

    androidLibrary {
        namespace = "dev.cadence.contracts"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()

        compilerOptions {
            jvmTarget = JvmTarget.JVM_11
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.serialization.json)
        }
    }
}
