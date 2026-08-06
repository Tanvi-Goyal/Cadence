import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("mindset.kmp.library")
    alias(libs.plugins.room)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlinSerialization) // Exercise stores list columns as JSON
}

kotlin {
    androidLibrary {
        namespace = "com.mindset.database"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
        compilerOptions {
            jvmTarget = JvmTarget.JVM_11
        }
        withHostTest {}
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.model) // ExerciseImporter classifies into Modality/MetricType
            implementation(libs.room.runtime)
            implementation(libs.room.paging)
            implementation(libs.androidx.paging.common)
            implementation(libs.androidx.sqlite.bundled)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)
            // databaseModule + databasePlatformModule (DI).
            implementation(project.dependencies.platform(libs.koin.bom))
            implementation(libs.koin.core)
        }
        androidMain.dependencies {
            implementation(libs.koin.android) // databasePlatformModule uses androidContext()
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }
        iosTest.dependencies {
            implementation(libs.room.testing) // MigrationTestHelper (Native actual is driver-based)
        }
    }
}

// Room-KMP: schema export location shared across all KSP targets.
room3 {
    schemaDirectory("$projectDir/schemas")
}

// MigrationTest reads the exported schema JSON at runtime; env vars only cross into the iOS
// simulator when prefixed with `SIMCTL_CHILD_`, so the test reads `MINDSET_SCHEMA_DIR`.
tasks.withType<org.jetbrains.kotlin.gradle.targets.native.tasks.KotlinNativeSimulatorTest>().configureEach {
    environment("SIMCTL_CHILD_MINDSET_SCHEMA_DIR", "$projectDir/schemas")
}

// Room's compiler runs as a KSP processor per target (android + both iOS).
dependencies {
    add("kspAndroid", libs.room.compiler)
    add("kspIosArm64", libs.room.compiler)
    add("kspIosSimulatorArm64", libs.room.compiler)
}
