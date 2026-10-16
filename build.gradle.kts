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
    description = "Analyze all hand-written Kotlin source with zero allowed findings."
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
        "**/build/**",
        "--report",
        "html:build/reports/detekt.html",
    )
}

tasks.register<Exec>("architectureCheck") {
    group = "verification"
    commandLine("python3", "tools/check_architecture.py")
}

val qualityPython =
    providers.provider {
        val localPython = layout.projectDirectory.file(".venv/bin/python").asFile
        if (localPython.exists()) localPython.absolutePath else "python3"
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

// Coverage is measured, not enforced; see docs/quality.md for the baseline.
dependencies { subprojects.forEach { kover(it) } }

subprojects {
    apply(
        plugin =
            rootProject.libs.plugins.kover
                .get()
                .pluginId,
    )
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
