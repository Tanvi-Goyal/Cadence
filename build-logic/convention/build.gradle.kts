plugins {
    `kotlin-dsl`
}

group = "com.mindset.buildlogic"

dependencies {
    // Plugin implementations the convention plugins apply/configure need on their compile classpath.
    // Referenced by catalog version so there's still one source of version truth.
    compileOnly("com.android.tools.build:gradle:${libs.versions.agp.get()}")
    compileOnly("org.jetbrains.kotlin:kotlin-gradle-plugin:${libs.versions.kotlin.get()}")
}

gradlePlugin {
    plugins {
        register("mindsetKmpLibrary") {
            id = "mindset.kmp.library"
            implementationClass = "com.mindset.buildlogic.MindSetKmpLibraryConventionPlugin"
        }
    }
}
