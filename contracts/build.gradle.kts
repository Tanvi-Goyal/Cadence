import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("mindset.kmp.library")
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    // jvm() lets the JVM :server consume these DTOs; :shared consumes the android/ios variants.
    // The KMP + AGP-KMP-library plugins, iOS targets, and -Xexpect-actual-classes come from the
    // `mindset.kmp.library` convention plugin.
    jvm()

    androidLibrary {
        namespace = "com.mindset.contracts"
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
