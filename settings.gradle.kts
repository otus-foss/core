rootProject.name = "otus-meta"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

// Gradle resolves the convention plugins' own dependencies from these repositories, so every Otus
// build lists them here; otus.settings adds the ones for libraries.
pluginManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
    // Convention plugins shared with Mail and Calendar; see docs/quality.md#build-logic.
    includeBuild("build-logic")
}

plugins {
    id("otus.settings")
}

// Library modules; see docs/architecture.md.
include(":jmap")
include(":dateformat")
include(":avatar")
include(":designsystem")
include(":settings")
include(":accounts")
include(":jmap-push")
include(":jscontact")
include(":jmap-contacts")
