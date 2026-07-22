import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.baselineprofile)
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_11
    }
}

composeCompiler {
    // Treat immutable :shared model types as stable (that module has no Compose compiler to infer
    // it). See composeApp/compose_stability.conf for the rationale and scope.
    stabilityConfigurationFiles.add(layout.projectDirectory.file("compose_stability.conf"))

    // Compose compiler stability/skippability reports, opt-in via `-PcomposeReports=true` so normal
    // builds aren't slowed. Output lands in composeApp/build/compose_compiler/*.txt.
    if (project.findProperty("composeReports") == "true") {
        val dir = layout.buildDirectory.dir("compose_compiler")
        reportsDestination = dir
        metricsDestination = dir
    }
}
dependencies {
    implementation(projects.shared)
    implementation(projects.core.designsystem)

    implementation(libs.androidx.activity.compose)
    implementation(libs.compose.runtime)
    implementation(libs.compose.foundation)
    implementation(libs.compose.material3)
    implementation(libs.compose.ui)
    implementation(libs.androidx.lifecycle.runtimeCompose)

    implementation(project.dependencies.platform(libs.koin.bom))
    implementation(libs.koin.android)
    implementation(libs.koin.compose)
    implementation(libs.koin.compose.viewmodel)

    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.paging.compose)

    // Lazy-loaded exercise images (Coil 3, with OkHttp network fetcher + disk cache).
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)
    implementation(libs.coil.svg) // wger muscle diagrams are SVG

    // Installs the baseline profile packaged in the APK at first run (Phase 3).
    implementation(libs.androidx.profileinstaller)

    implementation(libs.compose.uiToolingPreview)
    debugImplementation(libs.compose.uiTooling)

    // The :benchmark module produces the baseline profile this app consumes.
    baselineProfile(projects.benchmark)

    // Compose UI (instrumented) tests — the capture-flow test. Wired now; executed on a device / in
    // CI (LLD §9 "wired now, gated in CI from Phase 3"), not in the host/sim gate loop. Running on a
    // device additionally needs `androidx.compose.ui:ui-test-manifest` (JB doesn't publish one).
    androidTestImplementation(libs.androidx.testExt.junit)
    androidTestImplementation(libs.compose.uiTestJunit4)
}

android {
    namespace = "dev.cadence"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "dev.cadence"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
        // Non-debuggable, profileable variant Macrobenchmark runs against (it refuses debuggable
        // builds). Signed with the debug key so the release-like APK still installs locally.
        create("benchmark") {
            initWith(getByName("release"))
            signingConfig = signingConfigs.getByName("debug")
            isMinifyEnabled = false
            matchingFallbacks += listOf("release")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}