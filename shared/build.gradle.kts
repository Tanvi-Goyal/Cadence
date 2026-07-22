import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.ksp)
    alias(libs.plugins.koinCompiler)
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    // Room-KMP generates `actual` objects for @ConstructedBy; the expect/actual-classes feature
    // is still Beta in Kotlin, so opt in explicitly to silence the per-target warning.
    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }

    androidLibrary {
        namespace = "dev.cadence.shared"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()

        compilerOptions {
            jvmTarget = JvmTarget.JVM_11
        }
        withHostTest {}
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "Shared"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            // `api` so consumers (composeApp today, the iOS umbrella later) see these leaf modules
            // transitively — the moved packages (`dev.cadence.model`/`common`) keep their names, so
            // no import in :shared or :composeApp changes.
            api(projects.core.model)
            api(projects.core.common)
            // `api`: repo interfaces + domain value types (UserPreferences/WeightUnit/ThemeMode) are
            // referenced by the ViewModels here and by composeApp's theme/profile UI.
            api(projects.core.domain)
            // `api` (not implementation) is transitional: composeApp still references some entity
            // types (ExerciseMetric/Session/Exercise/VolumePoint/PlannedSession) — a UI→database leak
            // cleaned up when features are extracted (B11) and those types move to :core:model.
            api(projects.core.database)
            implementation(projects.core.network)
            implementation(projects.contracts)
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
            api(project.dependencies.platform(libs.koin.bom))
            api(libs.koin.core)
            api(libs.koin.core.viewmodel)
        }
        androidMain.dependencies {
            implementation(libs.koin.android)
            implementation(libs.ktor.client.okhttp)
        }
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}
