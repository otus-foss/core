// The library repositories of every Otus build. Google's repository serves only the Android groups.
dependencyResolutionManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
    }
}

// An app builds core from source: the sibling checkout ../core, or the one the
// otus.core.path Gradle property names. Its build is named core whatever the directory is
// called, so the convention plugins find it by that name, and its catalog is the app's `libs`.
// Core is the build that holds build-logic; the plugins block runs before rootProject.name is set.
if (!file("build-logic").isDirectory) {
    val otusCore = file(providers.gradleProperty("otus.core.path").getOrElse("../core"))
    includeBuild(otusCore) { name = "core" }
    dependencyResolutionManagement.versionCatalogs.create("libs") {
        from(files(otusCore.resolve("gradle/libs.versions.toml")))
    }
}
