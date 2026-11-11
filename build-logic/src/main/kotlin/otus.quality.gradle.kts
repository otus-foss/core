import kotlinx.kover.gradle.plugin.dsl.KoverProjectExtension

// The quality checks of every Otus repository, applied to its root project. The tools and the
// detekt configuration come from the core checkout, so each repository runs the same ones;
// see Core's docs/quality.md.
plugins {
    id("com.diffplug.spotless")
    id("org.jetbrains.kotlinx.kover")
}

val tools = otusCoreDir.resolve("tools")
val detektConfig = otusCoreDir.resolve("config/detekt.yml")
val ktlint = libs.version("ktlint")

spotless {
    kotlin {
        target("**/src/**/*.kt")
        targetExclude("**/build/**")
        ktlint(ktlint)
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
        ktlint(ktlint)
    }
}

val detektTool = configurations.create("detektTool")
dependencies { add(detektTool.name, libs.findLibrary("detekt-cli").get()) }
val moduleSources = subprojects.map { "${it.projectDir.relativeTo(rootDir)}/src" }

tasks.register<JavaExec>("detekt") {
    group = "verification"
    description = "Analyze hand-written main Kotlin source with zero allowed findings. Tests get ktlint only."
    classpath = detektTool
    mainClass.set("io.gitlab.arturbosch.detekt.cli.Main")
    args(
        "--input",
        moduleSources.joinToString(","),
        "--build-upon-default-config",
        "--excludes",
        "**/build/**,**/src/test/**,**/src/*Test/**",
        "--report",
        "html:build/reports/detekt.html",
    )
    // The config's place on disk stays out of the task's inputs, so another checkout can reuse the cache.
    val configPath = detektConfig.path
    argumentProviders.add(CommandLineArgumentProvider { listOf("--config", configPath) })
    // Declared inputs and outputs let an unchanged tree skip detekt or restore it from the build cache.
    inputs
        .files(moduleSources.map { fileTree(it) { exclude("test/**", "*Test/**") } })
        .withPathSensitivity(PathSensitivity.RELATIVE)
    inputs.file(detektConfig).withPathSensitivity(PathSensitivity.NAME_ONLY)
    outputs.file("build/reports/detekt.html")
    outputs.cacheIf { true }
}

tasks.register<Exec>("architectureCheck") {
    group = "verification"
    description = "Check source layout, dependency rules, and the shared configuration files."
    commandLine("python3", tools.resolve("check_architecture.py"))
}

// The repository's own Python tests, such as Core's tests of the shared tools.
val hasToolTests = rootDir.resolve("tools/tests").isDirectory
if (hasToolTests) {
    tasks.register<Exec>("architectureTest") {
        group = "verification"
        environment("PYTHONPATH", tools.path)
        commandLine("python3", "-m", "unittest", "discover", "-s", "tools/tests")
    }
}

tasks.register<Exec>("ruffCheck") {
    group = "verification"
    commandLine(qualityPython.get(), "-m", "ruff", "check", ".")
}

tasks.register<Exec>("ruffFormatCheck") {
    group = "verification"
    commandLine(qualityPython.get(), "-m", "ruff", "format", "--check", ".")
}

tasks.register<Exec>("ruffFix") {
    group = "formatting"
    commandLine(qualityPython.get(), "-m", "ruff", "check", "--fix", ".")
}

tasks.register<Exec>("ruffFormat") {
    group = "formatting"
    dependsOn("ruffFix")
    commandLine(qualityPython.get(), "-m", "ruff", "format", ".")
}

tasks.register("formatAll") {
    group = "formatting"
    description = "Format Kotlin, project text, and Python tooling."
    dependsOn("spotlessApply", "ruffFormat")
}

tasks.register("qualityCheck") {
    group = "verification"
    dependsOn("spotlessCheck", "detekt", "architectureCheck", "ruffCheck", "ruffFormatCheck")
    if (hasToolTests) dependsOn("architectureTest")
}

// CI splits the tests across jobs with -Potus.testShard=<index>/<count>. A test class runs in the
// shard its name hashes to, so each class runs in exactly one job.
val testShard =
    providers
        .gradleProperty("otus.testShard")
        .orNull
        ?.split('/')
        ?.map(String::toInt)

// Coverage is measured, not enforced. Instrumentation slows every test run, so collect coverage only
// when a Kover task runs. The coverage variant runs each test suite once: host tests and an app's
// debug unit tests, which hold its Robolectric screenshot tests. The total variant also runs an
// app's tests for every other build type.
val coverageRequested = gradle.startParameter.taskNames.any { "kover" in it }
dependencies { subprojects.forEach { kover(it) } }
kover { currentProject { createVariant("coverage") {} } }

subprojects {
    apply(plugin = "org.jetbrains.kotlinx.kover")
    extensions.configure<KoverProjectExtension> {
        currentProject {
            createVariant("coverage") { add("android", "debug", optional = true) }
            if (!coverageRequested) instrumentation { disabledForAll.set(true) }
        }
    }
    tasks.matching { it.name == "check" }.configureEach {
        dependsOn(rootProject.tasks.named("qualityCheck"))
    }
    if (testShard != null) {
        val (index, count) = testShard
        tasks.withType<Test>().configureEach {
            exclude { file ->
                val testClass = file.relativePath.pathString.substringBefore('$')
                !file.isDirectory && testClass.hashCode().mod(count) != index - 1
            }
        }
    }
}
