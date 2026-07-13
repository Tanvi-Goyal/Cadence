import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.androidTest)
    alias(libs.plugins.baselineprofile)
}

kotlin {
    // AGP 9 provides built-in Kotlin — no separate kotlin-android plugin (it conflicts).
    compilerOptions {
        jvmTarget = JvmTarget.JVM_11
    }
}

android {
    namespace = "dev.cadence.benchmark"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        // Macrobenchmark/BaselineProfile need API 24+; startup metrics are most accurate on 28+.
        minSdk = 28
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    // The app under test. Its `benchmark` build type is the profileable target we measure.
    targetProjectPath = ":composeApp"
    experimentalProperties["android.experimental.self-instrumenting"] = true
}

baselineProfile {
    // Run on the connected physical device (per the Phase 3 decision), not a managed emulator.
    useConnectedDevices = true
}

dependencies {
    implementation(libs.androidx.benchmark.macro.junit4)
    implementation(libs.androidx.testExt.junit)
    implementation(libs.junit)
    implementation(libs.androidx.uiautomator)
}
