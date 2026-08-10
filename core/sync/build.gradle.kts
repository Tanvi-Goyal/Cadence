import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("mindset.kmp.library")
}

kotlin {
    androidLibrary {
        namespace = "com.mindset.sync"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
        compilerOptions {
            jvmTarget = JvmTarget.JVM_11
        }
        withHostTest {}
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.domain) // Syncer interface + SyncOutcome
            implementation(projects.core.database) // AppDatabase, DAOs, entities, SyncMeta
            implementation(projects.core.network) // SyncApi transport
            implementation(projects.contracts) // wire DTOs
            implementation(libs.room.runtime) // useWriterConnection / immediateTransaction
            implementation(libs.kotlinx.coroutines.core)
            // syncModule (DI): the SyncEngine binding.
            implementation(project.dependencies.platform(libs.koin.bom))
            implementation(libs.koin.core)
        }
        iosTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.androidx.sqlite.bundled) // BundledSQLiteDriver for the in-memory test DB
            implementation(projects.core.data) // SessionRepositoryImpl drives the engine end-to-end
            implementation(projects.core.common) // UuidV7Generator seam
            implementation(projects.core.model) // PlannedSession + domain SessionType in the round-trip test
        }
    }
}
