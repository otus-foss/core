# Using the libraries from an app

The apps get these libraries in two ways. CI and release builds download a pinned version from
GitHub Packages. Local development can build against a sibling checkout instead, so a library
change can be tried in an app before it is released. Nothing in an app's build requires the
sibling checkout.

## Released versions

Each module is a Maven artifact `com.otus:<module>` in the GitHub Packages registry of this
repository. Add the registry to the app's `settings.gradle.kts`, limited to the `com.otus` group
so that no other dependency resolves from it:

```kotlin
dependencyResolutionManagement {
    repositories {
        // existing google() and mavenCentral() entries
        maven("https://maven.pkg.github.com/reazndev/otus-meta") {
            name = "otusMeta"
            credentials(PasswordCredentials::class)
            mavenContent { includeGroup("com.otus") }
        }
    }
}
```

Pin one version for every module in the app's `gradle/libs.versions.toml`:

```toml
[versions]
otus-meta = "0.1.0"

[libraries]
otus-jmap = { module = "com.otus:jmap", version.ref = "otus-meta" }
```

Then depend on it from the app's `shared` module, for example `api(libs.otus.jmap)`.

### Credentials

GitHub Packages requires a token to download Maven packages, even for read access.
`credentials(PasswordCredentials::class)` with the name `otusMeta` reads the Gradle properties
`otusMetaUsername` and `otusMetaPassword`.

On a development machine, add them to `~/.gradle/gradle.properties`, never to the repository:

```properties
otusMetaUsername=<GitHub username>
otusMetaPassword=<classic token with read:packages>
```

The Maven registry scopes package permissions to the repository that published them. A workflow
in otus-mail cannot read otus-meta packages with its own `GITHUB_TOKEN`. Store a classic token
with `read:packages` as the secret `OTUS_META_PACKAGES_TOKEN` in each app repository and pass it
to Gradle:

```yaml
env:
  ORG_GRADLE_PROJECT_otusMetaUsername: ${{ github.repository_owner }}
  ORG_GRADLE_PROJECT_otusMetaPassword: ${{ secrets.OTUS_META_PACKAGES_TOKEN }}
```

Verify this against the current GitHub Packages documentation when setting it up. If GitHub adds
per-package access for the Maven registry, prefer it over a personal token.

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
