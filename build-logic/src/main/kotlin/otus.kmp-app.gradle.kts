import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

// An app's shared module: its common code as the static Shared framework for the iOS app, and an
// Android library for androidApp. The root project's name says which app: mail is com.otusfoss.mail.
plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("com.android.kotlin.multiplatform.library")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
}

otusMultiplatform(
    namespace = "com.otusfoss.${rootProject.name.removePrefix("otus-")}.shared",
    androidResources = true,
    hostTestResources = false,
)

extensions.configure<KotlinMultiplatformExtension> {
    listOf(iosArm64(), iosSimulatorArm64()).forEach { target ->
        target.binaries.framework {
            baseName = "Shared"
            isStatic = true
        }
    }
}
