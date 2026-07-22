import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("cadence.kmp.library")
}

kotlin {
    androidLibrary {
        namespace = "dev.cadence.sync"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
        compilerOptions {
            jvmTarget = JvmTarget.JVM_11
        }
        withHostTest {}
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.database) // AppDatabase, DAOs, entities, SyncMeta
            implementation(projects.core.network)  // SyncApi transport
            implementation(projects.contracts)     // wire DTOs
            implementation(libs.room.runtime)        // useWriterConnection / immediateTransaction
            implementation(libs.kotlinx.coroutines.core)
        }
        iosTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.androidx.sqlite.bundled) // BundledSQLiteDriver for the in-memory test DB
            implementation(projects.core.data)   // SessionRepositoryImpl drives the engine end-to-end
            implementation(projects.core.common) // UuidV7Generator seam
            implementation(projects.core.model)  // PlannedSession + domain SessionType in the round-trip test
        }
    }
}
