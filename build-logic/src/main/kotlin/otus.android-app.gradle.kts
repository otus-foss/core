import com.android.build.api.dsl.ApplicationExtension
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension

// An app's Android host. Its namespace and application id come from the root project's name, so
// otus-mail is com.otus.mail.
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
}

val appId = "com.otus.${rootProject.name.removePrefix("otus-")}"
val catalog = libs

extensions.configure<ApplicationExtension> {
    namespace = appId
    compileSdk = catalog.sdk("compileSdk")
    buildToolsVersion = catalog.version("android-buildTools")
    defaultConfig {
        applicationId = appId
        minSdk = catalog.sdk("minSdk")
        targetSdk = catalog.sdk("targetSdk")
    }
    testOptions.unitTests.isIncludeAndroidResources = true
    lint {
        abortOnError = true
        warningsAsErrors = true
        checkDependencies = true
        // ktlint and detekt cover test code; Android lint on tests took a third of its time.
        ignoreTestSources = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

extensions.configure<KotlinAndroidProjectExtension> {
    compilerOptions {
        allWarningsAsErrors.set(true)
        jvmTarget.set(JvmTarget.JVM_11)
    }
}
