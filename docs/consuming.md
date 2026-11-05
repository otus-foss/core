# Using Meta from an app

Mail and Calendar build Meta from source. Each app includes an otus-meta checkout as a Gradle
composite build, so `com.otus:<module>` resolves to the matching Meta project, and each app's
build takes its convention plugins, version catalog, tools, and detekt configuration from the
same checkout. No app downloads Meta from GitHub Packages or pins a Meta version. See
[decision 0005](decisions/0005-shared-library-repository.md) for the 2026-11-06 amendment.

## The checkout

By default an app expects Meta next to it:

```text
otus-meta/
otus-mail/
otus-calendar/
```

The `otus.meta.path` Gradle property names another checkout, such as a worktree. It resolves from
the app's root, so `-Potus.meta.path=../meta-worktree` works for one build and
`otus.meta.path=/path/to/otus-meta` in `~/.gradle/gradle.properties` works for every build. Without
a checkout at that path, the app's build stops at once and says where it looked. The app's
`commit-msg` hook finds Meta the same way, through `ORG_GRADLE_PROJECT_otus.meta.path`,
`~/.gradle/gradle.properties`, the app's `gradle.properties`, or `../otus-meta`.

## What an app's settings file does

```kotlin
pluginManagement {
    val otusMeta = file(providers.gradleProperty("otus.meta.path").getOrElse("../otus-meta"))
    repositories { /* google(), mavenCentral(), gradlePluginPortal() */ }
    includeBuild(otusMeta.resolve("build-logic").path)
}

plugins {
    id("otus.settings")
}

val otusMeta = file(providers.gradleProperty("otus.meta.path").getOrElse("../otus-meta"))
includeBuild(otusMeta) { name = "otus-meta" }

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") { from(files(otusMeta.resolve("gradle/libs.versions.toml"))) }
        create("appLibs") { from(files("gradle/app.versions.toml")) }
    }
}
```

`pluginManagement` cannot read values from the rest of the file, so the path appears twice.
The included build is named `otus-meta` whatever its directory is called, so its task paths are
always `:otus-meta:<module>:<task>`, and the convention plugins find the checkout by that name.

- `libs` is Meta's `gradle/libs.versions.toml`, the suite catalog. It holds every version that
  more than one repository uses, and version-free `otus-<module>` entries for Meta's modules,
  such as `api(libs.otus.jmap)`.
- `appLibs` is the app's own `gradle/app.versions.toml`, with only what no other repository uses,
  such as SQLDelight in Mail. Calendar has none, so it has no `appLibs`.
- The convention plugins are described in [the quality guide](quality.md#build-logic).

## Changing Meta and an app together

Give the Meta branch and the app branch the same name. The app's CI checks out otus-meta beside
the app and switches to the branch with the app's branch name when one exists, or stays on Meta's
`main`. Mail and Calendar label such a pull request `needs meta`. Merge the Meta pull request
first, then the app's, so the app's `main` always builds against Meta's `main`.

## iOS

The app's `shared` framework links the library klibs. The Xcode project does not change and no
Swift package is involved. Its Kotlin build phase runs Gradle, which finds Meta the same way. Add
`export(libs.otus.<module>)` to the framework configuration only when Swift code must call
library types directly.

## Compose resources

The design system publishes its fonts and icons as Compose Multiplatform resources under
`com.otus.designsystem.resources`, and the avatar module bundles the Public Suffix List under
`com.otus.avatar.resources`. Apps keep resource packages only for their own assets. Check both
Android and iOS app builds after moving resources, because resource packaging differs per
platform.
