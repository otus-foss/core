import org.jetbrains.kotlin.gradle.dsl.JvmTarget

// The Otus design system: theme tokens, elements, and components driven by values and callbacks.
// Published as com.otus:designsystem.
plugins {
    `maven-publish`
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    explicitApi()
    compilerOptions { allWarningsAsErrors.set(true) }
    iosArm64()
    iosSimulatorArm64()
    android {
        namespace = "com.otus.designsystem"
        compileSdk =
            libs.versions.android.compileSdk
                .get()
                .toInt()
        minSdk =
            libs.versions.android.minSdk
                .get()
                .toInt()
        compilerOptions { jvmTarget = JvmTarget.JVM_11 }
        androidResources { enable = true }
        withHostTest { isIncludeAndroidResources = true }
        lint { ignoreTestSources = true }
    }
    sourceSets {
        commonMain.dependencies {
            api(libs.compose.runtime)
            api(libs.compose.foundation)
            api(libs.compose.material)
            api(libs.compose.ui)
            api(libs.compose.resources)
            api(projects.dateformat)
            api(libs.kotlinx.datetime)
            implementation(libs.navigation.event.compose)
            // Only the editor's foundation parts are used. Its Material 3 variants would put a second
            // design library on the apps' classpath, which Android lint rejects beside Material.
            implementation("${libs.richeditor.get().module}:${libs.versions.richeditor.get()}") {
                exclude(group = "org.jetbrains.compose.material3")
            }
            implementation(libs.kotlinx.coroutines.core)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotlinx.coroutines.test)
        }
        getByName("androidHostTest").dependencies {
            implementation(libs.robolectric)
            implementation(libs.androidx.compose.ui.test)
            implementation(libs.androidx.compose.ui.test.manifest)
            implementation(libs.roborazzi)
            implementation(libs.roborazzi.compose)
        }
    }
}

compose.resources {
    publicResClass = true
    packageOfResClass = "com.otus.designsystem.resources"
}

// Screenshot tests compare against the PNGs in src/androidHostTest/screenshots. Pass
// -Proborazzi.record to write new ones instead; see docs/designsystem.md#screenshot-tests.
val screenshots = layout.projectDirectory.dir("src/androidHostTest/screenshots")
val recordScreenshots = providers.gradleProperty("roborazzi.record").isPresent
tasks.withType<Test>().configureEach {
    inputs.dir(screenshots).withPropertyName("screenshots").optional()
    systemProperty(if (recordScreenshots) "roborazzi.test.record" else "roborazzi.test.verify", "true")
}

// The published iOS metadata sees JetBrains' and Google's Compose klibs together and warns about
// duplicate unique names. Every iOS target still compiles these sources with warnings as errors.
// See docs/decisions/0006-native-metadata-warnings.md.
tasks
    .matching { it.name == "compileIosMainKotlinMetadata" }
    .configureEach {
        (this as org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask<*>)
            .compilerOptions
            .allWarningsAsErrors
            .set(false)
    }
