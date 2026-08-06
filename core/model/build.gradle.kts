import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("mindset.kmp.library")
}

kotlin {
    androidLibrary {
        namespace = "com.mindset.model"
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
