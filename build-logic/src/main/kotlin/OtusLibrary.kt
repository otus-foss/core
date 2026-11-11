import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/**
 * A Core library module. Its package and coordinates follow its directory, so `jmap-push` is
 * `com.otusfoss.core.jmap.push` and `com.otusfoss.core:jmap-push`. Every public declaration is explicit.
 */
internal fun Project.otusLibrary(compose: Boolean) {
    otusMultiplatform(
        namespace = "com.otusfoss.core.${name.replace('-', '.')}",
        androidResources = compose,
        hostTestResources = compose,
    )
    extensions.configure<KotlinMultiplatformExtension> { explicitApi() }
}
