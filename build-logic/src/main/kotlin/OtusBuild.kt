import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.getByType
import java.io.File

/** The suite catalog, core's gradle/libs.versions.toml, which every Otus build loads as `libs`. */
internal val Project.libs: VersionCatalog
    get() = extensions.getByType<VersionCatalogsExtension>().named("libs")

internal fun VersionCatalog.version(name: String): String = findVersion(name).get().requiredVersion

internal fun VersionCatalog.sdk(name: String): Int = version("android-$name").toInt()

/**
 * The core checkout that holds the shared tools and configuration: the build an app includes as
 * `core`, or this build itself when it is Core.
 */
val Project.otusCoreDir: File
    get() = gradle.includedBuilds.find { it.name == "core" }?.projectDir ?: rootDir

/** Python with the pinned quality tools. A linked worktree has no .venv of its own, so it uses the main checkout's. */
val Project.qualityPython: Provider<String>
    get() =
        providers.provider {
            val root = rootDir
            val mainCheckout =
                root
                    .resolve(".git")
                    .takeIf { it.isFile }
                    ?.readText()
                    ?.substringAfter("gitdir:")
                    ?.trim()
                    ?.let { File(it).parentFile.parentFile.parentFile }
            listOfNotNull(root, mainCheckout)
                .map { it.resolve(".venv/bin/python") }
                .firstOrNull { it.exists() }
                ?.absolutePath ?: "python3"
        }
