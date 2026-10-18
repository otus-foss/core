import kotlinx.kover.gradle.plugin.dsl.KoverProjectExtension

plugins {
    id("com.diffplug.spotless") version "8.10.3"
    alias(libs.plugins.androidMultiplatformLibrary) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.kotlinSerialization) apply false
    alias(libs.plugins.kover)
}

spotless {
    kotlin {
        target("**/src/**/*.kt")
        targetExclude("**/build/**")
        ktlint("1.8.0")
    }
    format("projectText") {
        target("**/*.md", "**/*.yml", "**/*.yaml", "**/*.json", "**/*.xml", "**/*.plist", "**/*.toml")
        targetExclude("**/build/**", "**/.gradle/**", "**/.venv/**", "**/.git/**", "**/.idea/**")
        trimTrailingWhitespace()
        endWithNewline()
    }
    kotlinGradle {
        target("*.gradle.kts", "**/*.gradle.kts")
        targetExclude("**/build/**", "**/.gradle/**")
        ktlint("1.8.0")
    }
}

val detektTool = configurations.create("detektTool")
repositories { mavenCentral() }
dependencies { add(detektTool.name, "io.gitlab.arturbosch.detekt:detekt-cli:1.23.8") }

// Every library module's sources; empty until the first module moves in.
val detektInputs = subprojects.map { "${it.name}/src" }.joinToString(",")

tasks.register<JavaExec>("detekt") {
    group = "verification"
    description = "Analyze hand-written main Kotlin source with zero allowed findings. Tests get ktlint only."
    enabled = detektInputs.isNotEmpty()
    classpath = detektTool
    mainClass.set("io.gitlab.arturbosch.detekt.cli.Main")
    args(
        "--input",
        detektInputs,
        "--config",
        "config/detekt.yml",
        "--build-upon-default-config",
        "--excludes",
        "**/build/**,**/src/test/**,**/src/*Test/**",
        "--report",
        "html:build/reports/detekt.html",
    )
    // Declared inputs and outputs let an unchanged tree skip detekt or restore it from the build cache.
    inputs
        .files(subprojects.map { fileTree("${it.name}/src") { exclude("test/**", "*Test/**") } })
        .withPathSensitivity(PathSensitivity.RELATIVE)
    inputs.file("config/detekt.yml").withPathSensitivity(PathSensitivity.RELATIVE)
    outputs.file("build/reports/detekt.html")
    outputs.cacheIf { true }
}

tasks.register<Exec>("architectureCheck") {
    group = "verification"
    commandLine("python3", "tools/check_architecture.py")
}

// A linked worktree has no .venv of its own, so fall back to the main checkout's.
val qualityPython =
    providers.provider {
        val root = layout.projectDirectory.asFile
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

tasks.register<Exec>("architectureTest") {
    group = "verification"
    commandLine("python3", "-m", "unittest", "discover", "-s", "tools/tests")
}

tasks.register<Exec>("ruffCheck") {
    group = "verification"
    commandLine(qualityPython.get(), "-m", "ruff", "check", "tools")
}

tasks.register<Exec>("ruffFormatCheck") {
    group = "verification"
    commandLine(qualityPython.get(), "-m", "ruff", "format", "--check", "tools")
}

tasks.register<Exec>("ruffFix") {
    group = "formatting"
    commandLine(qualityPython.get(), "-m", "ruff", "check", "--fix", "tools")
}

tasks.register<Exec>("ruffFormat") {
    group = "formatting"
    dependsOn("ruffFix")
    commandLine(qualityPython.get(), "-m", "ruff", "format", "tools")
}

tasks.register("formatAll") {
    group = "formatting"
    description = "Format Kotlin, project text, and Python tooling."
    dependsOn("spotlessApply", "ruffFormat")
}

tasks.register("qualityCheck") {
    group = "verification"
    dependsOn("spotlessCheck", "detekt", "architectureCheck", "architectureTest", "ruffCheck", "ruffFormatCheck")
}

tasks.register("checkAll") {
    group = "verification"
    description = "Run portable quality checks and every library module's checks."
    dependsOn("qualityCheck")
    dependsOn(subprojects.map { "${it.path}:check" })
}

// Coverage is measured, not enforced; see docs/quality.md for the baseline. Instrumentation slows
// every test run, so collect coverage only when a Kover task runs.
dependencies { subprojects.forEach { kover(it) } }
val coverageRequested = gradle.startParameter.taskNames.any { "kover" in it }

subprojects {
    apply(
        plugin =
            rootProject.libs.plugins.kover
                .get()
                .pluginId,
    )
    if (!coverageRequested) {
        extensions.configure<KoverProjectExtension> { currentProject { instrumentation { disabledForAll.set(true) } } }
    }
    // The repository name selects the GitHubPackagesUsername and GitHubPackagesPassword properties.
    plugins.withId("maven-publish") {
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
    tasks.matching { it.name == "check" }.configureEach {
        dependsOn(rootProject.tasks.named("qualityCheck"))
    }
}
