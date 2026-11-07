plugins {
    id("otus.quality")
    alias(libs.plugins.kotlinSerialization) apply false
}

// CI runs each part of checkAll as its own parallel job.
tasks.register("checkDesignSystem") {
    group = "verification"
    dependsOn(":designsystem:check")
}

tasks.register("checkLibraries") {
    group = "verification"
    dependsOn(subprojects.filter { it.path != ":designsystem" }.map { "${it.path}:check" })
}

tasks.register("checkAll") {
    group = "verification"
    description = "Run portable quality checks and every library module's checks."
    dependsOn("qualityCheck", "checkDesignSystem", "checkLibraries")
}
