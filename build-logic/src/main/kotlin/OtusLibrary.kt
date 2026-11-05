import org.gradle.api.Project
import org.gradle.api.artifacts.repositories.PasswordCredentials
import org.gradle.api.publish.PublishingExtension
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.credentials
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/**
 * A Meta library module. Its package and artifact follow its directory, so `jmap-push` is
 * `com.otus.jmap.push` and `com.otus:jmap-push`. Every public declaration is explicit.
 */
internal fun Project.otusLibrary(compose: Boolean) {
    otusMultiplatform(
        namespace = "com.otus.${name.replace('-', '.')}",
        androidResources = compose,
        hostTestResources = compose,
    )
    extensions.configure<KotlinMultiplatformExtension> { explicitApi() }
    // The repository name selects the GitHubPackagesUsername and GitHubPackagesPassword properties.
    extensions.configure<PublishingExtension> {
        repositories {
            maven {
                name = "GitHubPackages"
                url = uri("https://maven.pkg.github.com/reazndev/otus-meta")
                credentials(PasswordCredentials::class)
            }
        }
    }
}
