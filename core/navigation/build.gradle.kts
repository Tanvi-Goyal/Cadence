import org.jetbrains.kotlin.gradle.dsl.JvmTarget

/*
 * :core:navigation — the type-safe route vocabulary (@Serializable route classes). Pure data types,
 * no Compose or navigation-compose dependency: the typed nav *APIs* (composable<T>, toRoute, navigate)
 * live in the NavHost (composeApp / :app). Features (B11) depend on this for inter-feature navigation.
 */
plugins {
    // AGP is already on the shared plugin classpath (build-logic), so apply by id without a version.
    // AGP 9 provides built-in Kotlin. Serialization plugin drives the @Serializable route classes.
    id("com.android.library")
    alias(libs.plugins.kotlinSerialization)
}

android {
    namespace = "com.mindset.navigation"
    compileSdk =
        libs.versions.android.compileSdk
            .get()
            .toInt()

    defaultConfig {
        minSdk =
            libs.versions.android.minSdk
                .get()
                .toInt()
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_11
    }
}

dependencies {
    // api: the route classes are @Serializable, and composeApp needs the serialization runtime on its
    // compile classpath to resolve serializer<T>() for the reified typed-nav APIs.
    api(libs.kotlinx.serialization.json)
}
