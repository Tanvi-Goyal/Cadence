// Standalone included build for Cadence's Gradle convention plugins. Kept separate from the main
// build so the plugins compile once and the 15+ module build files stay DRY.
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    versionCatalogs {
        // Reuse the app's single source of version truth inside the convention plugins.
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
}

rootProject.name = "build-logic"
include(":convention")
