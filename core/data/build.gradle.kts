import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("cadence.kmp.library")
}

kotlin {
    androidLibrary {
        namespace = "dev.cadence.data"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
        compilerOptions {
            jvmTarget = JvmTarget.JVM_11
        }
        withHostTest {}
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.domain)   // implements the repository interfaces
            implementation(projects.core.database) // AppDatabase, DAOs, entities, importer, asset reader
            implementation(projects.core.network)  // WgerApi (MuscleImageProvider)
            implementation(projects.core.model)    // mappers produce domain models
            implementation(projects.core.common)   // UuidGenerator
            implementation(libs.room.runtime)        // useWriterConnection / immediateTransaction
            implementation(libs.androidx.paging.common) // Pager / PagingData.map
            implementation(libs.kotlinx.coroutines.core)
        }
        iosTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.androidx.sqlite.bundled) // BundledSQLiteDriver for in-memory test DBs
        }
    }
}
