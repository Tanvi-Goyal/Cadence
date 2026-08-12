import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.kotzilla)
}

kotzilla {
    versionName = "1.0"
}

kotlin {
    // Room-KMP generates `actual` objects for @ConstructedBy; the expect/actual-classes feature
    // is still Beta in Kotlin, so opt in explicitly to silence the per-target warning.
    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }

    androidLibrary {
        namespace = "com.mindset.shared"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()

        compilerOptions {
            jvmTarget = JvmTarget.JVM_11
        }
        withHostTest {}
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "Shared"
            isStatic = true
            // Export the modules whose types cross the Swift boundary so they enter Shared.framework's
            // Obj-C header (KoinIos returns the VMs; Swift holds them + casts their UI-state types).
            export(projects.core.model) // Exercise, MuscleDiagram
            export(projects.core.common) // FlowSubscription
            export(projects.core.domain) // UserPreferences
            export(projects.feature.home)
            export(projects.feature.logging)
            export(projects.feature.templates)
            export(projects.feature.exercises)
            export(projects.feature.history)
            export(projects.feature.stations)
            export(projects.feature.profile)
        }
    }

    sourceSets {
        commonMain.dependencies {
            // `api` so consumers (composeApp today, the iOS umbrella later) see these leaf modules
            // transitively — the moved packages (`com.mindset.model`/`common`) keep their names, so
            // no import in :shared or :composeApp changes.
            api(projects.core.model)
            api(projects.core.common)
            // `api`: repo interfaces + domain value types (UserPreferences/WeightUnit/ThemeMode) are
            // referenced by the ViewModels here and by composeApp's theme/profile UI.
            api(projects.core.domain)
            // The repository impls + MuscleImageProvider (bound in DI here); package unchanged.
            implementation(projects.core.data)
            // dataStorePlatformModule (device-local settings seam), aggregated in Modules.kt below.
            implementation(projects.core.datastore)
            // `api` (not implementation) is transitional: composeApp still references some entity
            // types (ExerciseMetric/Session/Exercise/VolumePoint/PlannedSession) — a UI→database leak
            // cleaned up when features are extracted (B11) and those types move to :core:model.
            api(projects.core.database)
            implementation(projects.core.network)
            implementation(projects.core.sync) // SyncEngine, consumed by HomeViewModel + bound in Koin
            implementation(projects.contracts)
            // Feature modules — `api` so their ViewModels stay exported in Shared.framework for Swift (B11).
            api(projects.feature.profile)
            api(projects.feature.stations)
            api(projects.feature.history)
            api(projects.feature.exercises)
            api(projects.feature.logging)
            api(projects.feature.templates)
            api(projects.feature.home)
            // Onboarding VM graph. `api` for Koin aggregation in Modules.kt; not exported to the iOS
            // framework (Android-first per the port-later decision) — add export(...) when iOS parity lands.
            api(projects.feature.onboarding)
            // Room RUNTIME stays: the repository + sync engine + iosTest still call useWriterConnection /
            // Room.inMemoryDatabaseBuilder. Only the Room *plugin*/KSP/schemas moved to :core:database.
            implementation(libs.room.runtime)
            implementation(libs.room.paging)
            implementation(libs.androidx.paging.common)
            implementation(libs.androidx.sqlite.bundled)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.contentNegotiation)
            implementation(libs.ktor.serialization.kotlinxJson)
            implementation(libs.kotzilla.sdk)
            api(project.dependencies.platform(libs.koin.bom))
            api(libs.koin.core)
            api(libs.koin.core.viewmodel)
        }
        androidMain.dependencies {
            // Koin + Ktor engine bindings moved to :core:database / :core:network platform seams (B7).
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}
