# Using the libraries from an app

The apps get these libraries in two ways. CI and release builds download a pinned version from
Forgejo Packages. Local development can build against a sibling checkout instead, so a library
change can be tried in an app before it is released. Nothing in an app's build requires the
sibling checkout.

Forgejo is the selected Git and package host. Mail's package migration change sets the Forgejo
registry as its default. Authenticated availability of the pinned release is still unverified;
see [hosting and CI](hosting.md). Moving Git repositories does not transfer Maven artifacts.
Calendar has not adopted Meta packages yet.

## Released versions

Each module is a Maven artifact `com.otus:<module>` in the Forgejo Packages registry of this
repository. Add the registry to the app's `settings.gradle.kts`, limited to the `com.otus` group
so that no other dependency resolves from it:

```kotlin
dependencyResolutionManagement {
    repositories {
        // existing google() and mavenCentral() entries
        maven("https://git.hilfikers.com/api/packages/florian.ruby/maven") {
            name = "otusMeta"
            // Gradle checks credentials before every build; without a token only -Potus.meta.path works.
            if (providers.gradleProperty("otusMetaPassword").isPresent) credentials(PasswordCredentials::class)
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

Create a Forgejo token with `read:package`, using an account that can access the packages owned
by `florian.ruby`. Package scopes require a token with access to the package owner, rather than
one restricted to specific repositories. See
[Forgejo token scopes](https://forgejo.org/docs/latest/user/authentication/token-scope/).

`credentials(PasswordCredentials::class)` with the repository name `otusMeta` reads the Gradle
properties `otusMetaUsername` and `otusMetaPassword`. Declare credentials only when the token
property is present, so tasks that do not download packages can configure without credentials.

On a development machine, add these to `~/.gradle/gradle.properties`, never to the repository:

```properties
otusMetaUsername=florian.ruby
otusMetaPassword=<Forgejo package-read token>
```

Mail defaults to the Forgejo URL. `otusMetaRegistry` in private Gradle properties can override it;
clear an old GitHub override when moving to Forgejo. In each consuming app's Actions settings,
store the Forgejo read token as `OTUS_META_PACKAGES_TOKEN` and configure
`OTUS_META_PACKAGES_USERNAME` with the token's Forgejo username. Pass them to Gradle:

```yaml
env:
  ORG_GRADLE_PROJECT_otusMetaRegistry: https://git.hilfikers.com/api/packages/florian.ruby/maven
  ORG_GRADLE_PROJECT_otusMetaUsername: ${{ vars.OTUS_META_PACKAGES_USERNAME || 'florian.ruby' }}
  ORG_GRADLE_PROJECT_otusMetaPassword: ${{ secrets.OTUS_META_PACKAGES_TOKEN }}
```

`OTUS_META_REGISTRY_URL` is an optional Actions variable for Mail's registry override. Clear an
old GitHub URL. All root and platform artifacts for the pinned release must exist on Forgejo.
A token grants access to packages; it does not publish missing artifacts. See
[releasing](releasing.md).

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
