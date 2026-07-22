import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("cadence.kmp.library")
}

kotlin {
    androidLibrary {
        namespace = "dev.cadence.model"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
        compilerOptions {
            jvmTarget = JvmTarget.JVM_11
        }
        withHostTest {}
    }

    sourceSets {
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}
