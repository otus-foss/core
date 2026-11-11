# Using Core from an app

Mail and Calendar build Core from source. Each app includes an otus-core checkout as a Gradle
composite build, so `com.otusfoss.core:<module>` resolves to the matching Core project, and each app's
build takes its convention plugins, version catalog, tools, and detekt configuration from the
same checkout. No app downloads Core from GitHub Packages or pins a Core version. See
[decision 0005](decisions/0005-shared-library-repository.md) for the 2026-11-06 amendment.

## The checkout

By default an app expects Core next to it:

```text
otus-core/
otus-mail/
otus-calendar/
```

The `otus.core.path` Gradle property names another checkout, such as a worktree. It resolves from
the app's root, so `-Potus.core.path=../core-worktree` works for one build and
`otus.core.path=/path/to/otus-core` in `~/.gradle/gradle.properties` works for every build. Without
a checkout at that path, the app's build stops at once and says where it looked. The app's
`commit-msg` hook finds Core the same way, through `ORG_GRADLE_PROJECT_otus.core.path`,
`~/.gradle/gradle.properties`, the app's `gradle.properties`, or `../otus-core`.

## What an app's settings file does

```kotlin
pluginManagement {
    val otusCore = file(providers.gradleProperty("otus.core.path").getOrElse("../otus-core"))
    repositories { /* google(), mavenCentral(), gradlePluginPortal() */ }
    includeBuild(otusCore.resolve("build-logic").path)
}

plugins {
    id("otus.settings")
}

dependencyResolutionManagement {
    versionCatalogs {
        create("appLibs") { from(files("gradle/app.versions.toml")) }
    }
}
```

`pluginManagement` runs before any plugin, so it reads the path itself to find the convention
plugins. The `otus.settings` plugin then reads it again with the same rule, includes the checkout,
and loads its catalog as `libs`. The included build is named `otus-core` whatever its directory is
called, so its task paths are always `:otus-core:<module>:<task>`, and the convention plugins find
the checkout by that name.

- `libs` is Core's `gradle/libs.versions.toml`, the suite catalog. It holds every version that
  more than one repository uses, and version-free `otus-<module>` entries for Core's modules,
  such as `api(libs.otus.jmap)`.
- `appLibs` is the app's own `gradle/app.versions.toml`, with only what no other repository uses,
  such as SQLDelight in Mail. Calendar has none, so it has no `appLibs`.
- The convention plugins are described in [the quality guide](quality.md#build-logic).

## Changing Core and an app together

Give the Core branch and the app branch the same name. The app's CI checks out otus-core beside
the app and switches to the branch with the app's branch name when one exists, or stays on Core's
`main`. Every job does this before it runs a Core action, so the gate, the build, and the
pull-request facts all use the Core that lands with the change, and an app pull request runs
before its Core partner merges. Core's `quality` workflow does the same the other way round: its
`apps` job compiles Mail and Calendar at the branch with Core's branch name, or at their `main`,
against the Core pull request.

Mail and Calendar label such a pull request `needs core`. Merge the Core pull request first, then
the app's, so the app's `main` always builds against Core's `main`.

## iOS

The app's `shared` framework links the library klibs. The Xcode project does not change and no
Swift package is involved. Its Kotlin build phase runs Gradle, which finds Core the same way. Add
`export(libs.otus.<module>)` to the framework configuration only when Swift code must call
library types directly.

## Compose resources

The design system publishes its fonts and icons as Compose Multiplatform resources under
`com.otusfoss.core.designsystem.resources`, and the avatar module bundles the Public Suffix List under
`com.otusfoss.core.avatar.resources`. Apps keep resource packages only for their own assets. Check both
Android and iOS app builds after moving resources, because resource packaging differs per
platform.
