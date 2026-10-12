# Using the libraries from an app

The apps get these libraries in two ways. CI and release builds download a pinned version from
GitHub Packages. Local development can build against a sibling checkout instead, so a library
change can be tried in an app before it is released. Nothing in an app's build requires the
sibling checkout. Calendar has not adopted Meta packages yet.

## Released versions

Each module is a Maven artifact `com.otus:<module>` in this repository's GitHub Packages registry.
Add the registry to the app's `settings.gradle.kts` as exclusive content for the `com.otus`
group. No other dependency resolves from it, and `com.otus` resolves only from it:

```kotlin
dependencyResolutionManagement {
    repositories {
        // existing google() and mavenCentral() entries
        exclusiveContent {
            forRepository {
                maven("https://maven.pkg.github.com/reazndev/otus-meta") {
                    name = "otusMeta"
                    // Gradle checks credentials before every build; without a token only -Potus.meta.path works.
                    if (providers.gradleProperty("otusMetaPassword").isPresent) credentials(PasswordCredentials::class)
                }
            }
            filter { includeGroup("com.otus") }
        }
    }
}
```

`mavenContent { includeGroup(...) }` alone only keeps other groups out of this registry, so a
`com.otus` artifact that someone else published to Maven Central could still win; `exclusiveContent`
makes this registry the only source for the group.

Pin one version for every module in the app's `gradle/libs.versions.toml`:

```toml
[versions]
otus-meta = "0.1.0"

[libraries]
otus-jmap = { module = "com.otus:jmap", version.ref = "otus-meta" }
```

Then depend on it from the app's `shared` module, for example `api(libs.otus.jmap)`.

### Credentials

GitHub Packages needs a token for every download, even from a public repository. Create a
classic personal access token with the `read:packages` scope.

`credentials(PasswordCredentials::class)` with the repository name `otusMeta` reads the Gradle
properties `otusMetaUsername` and `otusMetaPassword`. Declare credentials only when the token
property is present, so tasks that do not download packages can configure without credentials.

On a development machine, add these to `~/.gradle/gradle.properties`, never to the repository:

```properties
otusMetaUsername=<GitHub username>
otusMetaPassword=<GitHub token with read:packages>
```

In each consuming app's Actions settings, store the token as the `OTUS_META_PACKAGES_TOKEN`
secret and pass it to Gradle:

```yaml
env:
  ORG_GRADLE_PROJECT_otusMetaUsername: ${{ github.repository_owner }}
  ORG_GRADLE_PROJECT_otusMetaPassword: ${{ secrets.OTUS_META_PACKAGES_TOKEN }}
```

A token grants access to packages; it does not publish missing artifacts. Versions 0.3.1 to
0.5.0 are not on GitHub Packages. See [releasing](releasing.md).

## Local development against a checkout

Add an opt-in composite build to the app's `settings.gradle.kts`:

```kotlin
providers.gradleProperty("otus.meta.path").orNull?.let { includeBuild(it) }
```

Enable it for a single build with `./gradlew checkAll -Potus.meta.path=../otus-meta`, or for every
build by adding `otus.meta.path=../otus-meta` to `~/.gradle/gradle.properties`. The path resolves
from each app's root, so one entry works for both apps when they sit next to otus-meta. Gradle
replaces `com.otus:<module>` with the matching project from the checkout, regardless of the
pinned version. CI never sets this property.

Before merging an app change that needs a library change, release the library and update the
pinned version. An app's main branch must always build from a released version.

## iOS

The app's `shared` framework links the library klibs. The Xcode project does not change and no
Swift package is involved. Add `export(libs.otus.<module>)` to the framework configuration only
when Swift code must call library types directly.

## Compose resources

The design-system module publishes its fonts and icons as Compose Multiplatform resources under
`com.otus.designsystem.resources`. The apps keep their own resource packages for app-specific
assets. Check both Android and iOS app builds after moving resources, because resource packaging
differs per platform.
