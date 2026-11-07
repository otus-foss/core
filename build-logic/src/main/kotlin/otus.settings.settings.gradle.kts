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

// An app builds otus-meta from source: the sibling checkout ../otus-meta, or the one the
// otus.meta.path Gradle property names. Its build is named otus-meta whatever the directory is
// called, so the convention plugins find it by that name, and its catalog is the app's `libs`.
// Meta is the build that holds build-logic; the plugins block runs before rootProject.name is set.
if (!file("build-logic").isDirectory) {
    val otusMeta = file(providers.gradleProperty("otus.meta.path").getOrElse("../otus-meta"))
    includeBuild(otusMeta) { name = "otus-meta" }
    dependencyResolutionManagement.versionCatalogs.create("libs") {
        from(files(otusMeta.resolve("gradle/libs.versions.toml")))
    }
}
