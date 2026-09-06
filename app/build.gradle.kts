import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.baselineprofile)
    alias(libs.plugins.googleServices) apply false
    alias(libs.plugins.firebaseCrashlytics) apply false
}

val hasFirebaseConfig: Boolean = project.file("google-services.json").exists()

if (hasFirebaseConfig) {
    apply(plugin = libs.plugins.googleServices.get().pluginId)
    apply(plugin = libs.plugins.firebaseCrashlytics.get().pluginId)
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_11
    }
}

composeCompiler {
    stabilityConfigurationFiles.add(layout.projectDirectory.file("compose_stability.conf"))

    if (project.findProperty("composeReports") == "true") {
        val dir = layout.buildDirectory.dir("compose_compiler")
        reportsDestination = dir
        metricsDestination = dir
    }
}
dependencies {
    implementation(projects.shared)
    implementation(projects.core.designsystem)
    implementation(projects.core.ui)
    implementation(projects.core.navigation)
    implementation(projects.feature.profile)
    implementation(projects.feature.stations)
    implementation(projects.feature.history)
    implementation(projects.feature.exercises)
    implementation(projects.feature.logging)
    implementation(projects.feature.templates)
    implementation(projects.feature.home)
    implementation(projects.feature.auth)
    implementation(projects.feature.onboarding)

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

    if (hasFirebaseConfig) {
        implementation(platform(libs.firebase.bom))
        implementation(libs.firebase.crashlytics)
    }

    implementation(libs.compose.uiToolingPreview)
    debugImplementation(libs.compose.uiTooling)

    // The :benchmark module produces the baseline profile this app consumes.
    baselineProfile(projects.benchmark)

    androidTestImplementation(libs.androidx.testExt.junit)
    androidTestImplementation(libs.compose.uiTestJunit4)
}

android {
    namespace = "com.mindset"
    compileSdk =
        libs.versions.android.compileSdk
            .get()
            .toInt()

    defaultConfig {
        applicationId = "com.mindset"
        minSdk =
            libs.versions.android.minSdk
                .get()
                .toInt()
        targetSdk =
            libs.versions.android.targetSdk
                .get()
                .toInt()
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Off everywhere by default; only the `benchmark` variant turns it on. MainActivity is
        // exported, so the bulk-seed intent extra it gates must never be honored in a shipped build.
        buildConfigField("boolean", "SEED_HOOK_ENABLED", "false")
    }
    buildFeatures {
        buildConfig = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    // Upload-key config, read from a gitignored keystore.properties at the repo root. Absent on CI
    // and on a fresh clone, so this must degrade to "no release signing" rather than failing
    // configuration — an unsigned assembleRelease is a clearer failure than a broken build script.
    val keystoreProperties = Properties().apply {
        val file = rootProject.file("keystore.properties")
        if (file.exists()) file.inputStream().use(::load)
    }
    val hasReleaseKeystore = keystoreProperties.getProperty("storeFile") != null

    signingConfigs {
        if (hasReleaseKeystore) {
            create("release") {
                storeFile = rootProject.file(keystoreProperties.getProperty("storeFile"))
                storePassword = keystoreProperties.getProperty("storePassword")
                keyAlias = keystoreProperties.getProperty("keyAlias")
                keyPassword = keystoreProperties.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            if (hasReleaseKeystore) signingConfig = signingConfigs.getByName("release")
        }
        // Non-debuggable, profileable variant Macrobenchmark runs against (it refuses debuggable
        // builds). Signed with the debug key so the release-like APK still installs locally.
        create("benchmark") {
            initWith(getByName("release"))
            signingConfig = signingConfigs.getByName("debug")
            isMinifyEnabled = false
            matchingFallbacks += listOf("release")

            // Macrobenchmark needs a long History list to scroll; this is the only variant allowed
            // to honor :benchmark's EXTRA_SEED intent extra.
            buildConfigField("boolean", "SEED_HOOK_ENABLED", "true")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}
