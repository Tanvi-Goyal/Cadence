import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.ksp)
    alias(libs.plugins.room)
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
            implementation(projects.contracts)
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
        // Room's MigrationTestHelper drives the v7→v8 fixture test. It lives in iosTest: the Android
        // actual of MigrationTestHelper is instrumentation-only (no JVM host-test path), whereas the
        // Native actual is driver-based — same reason the other Room tests run on the iOS simulator.
        iosTest.dependencies {
            implementation(libs.room.testing)
        }
    }
}

// Room-KMP (3.0, androidx.room3): schema export location shared across all KSP targets.
room3 {
    schemaDirectory("$projectDir/schemas")
}

// The migration fixture test (MigrationTest, iosTest) reads the exported schema JSON from disk at
// runtime. Pass the absolute path into the simulated test process — env vars only cross into the iOS
// simulator when prefixed with `SIMCTL_CHILD_`, so the test reads `CADENCE_SCHEMA_DIR`.
tasks.withType<org.jetbrains.kotlin.gradle.targets.native.tasks.KotlinNativeSimulatorTest>().configureEach {
    environment("SIMCTL_CHILD_CADENCE_SCHEMA_DIR", "$projectDir/schemas")
}

// Room's compiler is a KSP processor declared PER target — commonMain @Entity/@Dao/@Database
// annotations get their actual code generated separately for android, iosArm64, iosSimulatorArm64.
dependencies {
    add("kspAndroid", libs.room.compiler)
    add("kspIosArm64", libs.room.compiler)
    add("kspIosSimulatorArm64", libs.room.compiler)
}
